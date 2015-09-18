package ch.usi.inf.mavends.analysis;

import java.io.InputStream;
import java.sql.ResultSet;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.log.Log;

public final class Main {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index DB.")
		public String mavenIndex;

		@Arg(key = "repo", name = "Maven Inode DB path", desc = "Specifies the path of the output db file.")
		public String repo;

		@Arg(key = "query", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String query;

		@Arg(key = "mavenvisitor", name = "Maven Visitor", desc = "Specifies the path of the output db file.")
		public String mavenVisitor;

	}

	public static void main(String[] args) throws Exception {
		final Args ar = ArgsParser.parse(args, new Args());

		Class<?> cls = Class.forName(ar.mavenVisitor);

		int numberOfProcessors = Runtime.getRuntime().availableProcessors();

		final ArtifactQueue[] queues = new ArtifactQueue[numberOfProcessors];
		for (int i = 0; i < queues.length; i++) {
			queues[i] = new ArtifactQueue();
		}

		try (final Db db = new Db(ar.mavenIndex); final ResultSet rs = db.select(ar.query)) {
			int n = 0;

			while (rs.next()) {
				final long coordid = rs.getLong("coordid");
				final String path = rs.getString("path");

				queues[n % queues.length].add(coordid, path);

				n++;
			}

			log.info("No. jar files: %,d", n);
		}

		try (final MavenVisitor mv = (MavenVisitor) cls.newInstance()) {
			for (final ArtifactQueue queue : queues) {
				new JarReader(ar.repo, queue) {
					@Override
					synchronized void processEntry(String filename, InputStream classFileStream) {
						try {
							final ClassReader cr = new ClassReader(classFileStream);
							final ClassVisitor v = mv.visitClass();
							cr.accept(v, 0);
						} catch (Exception e) {
							log.info("Exception: %s", e);
						}
					}
				}.start();
			}

			long items;
			do {
				Thread.sleep(30 * 1000);

				items = 0;

				for (final ArtifactQueue queue : queues) {
					items += queue.size();
				}

				log.info("Remaining Jars: %,d", items);
			} while (items > 0);
		}
	}
}
