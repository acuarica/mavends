package ch.usi.inf.mavends.analysis;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

	private static class Artifact {
		final long coordid;
		final String path;

		Artifact(long coordid, String path) {
			this.coordid = coordid;
			this.path = path;
		}
	}

	public static void main(String[] args) throws Exception {
		final Args ar = ArgsParser.parse(args, new Args());

		Class<?> cls = Class.forName(ar.mavenVisitor);

		final List<Artifact> queue = new LinkedList<Artifact>();

		try (final Db db = new Db(ar.mavenIndex)) {
			final ResultSet rs = db.select(ar.query);
			int n = 0;

			while (rs.next()) {
				final long coordid = rs.getLong("coordid");
				final String path = rs.getString("path");

				queue.add(new Artifact(coordid, path));

				n++;
			}

			log.info("No. jar files: %d", n);
		}

		new Thread() {
			@Override
			public void run() {
				do {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					log.info("Remaining Jars: %,d", queue.size());
				} while (queue.size() > 0);
			};
		}.start();

		try (MavenVisitor mv = (MavenVisitor) cls.newInstance()) {
			for (Iterator<Artifact> it = queue.iterator(); it.hasNext();) {
				final Artifact artifact = it.next();
				new JarReader(ar.repo) {

					@Override
					void processEntry(String filename, byte[] classFile) {
						try {
							ClassReader cr = new ClassReader(classFile);
							ClassVisitor v = mv.visitClass();
							cr.accept(v, 0);
						} catch (Exception e) {
							log.info("Exception: %s", e);
							// e.printStackTrace();
						}
					}
				}.process(artifact.path);

				it.remove();
			}
		}
	}
}
