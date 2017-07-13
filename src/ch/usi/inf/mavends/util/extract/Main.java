package ch.usi.inf.mavends.util.extract;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

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

		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		final Class<?> cls = Class.forName(ar.mavenVisitor);

//		final int numberOfProcessors = Runtime.getRuntime().availableProcessors();

//		final ArtifactQueue[] queues = new ArtifactQueue[numberOfProcessors];
//		for (int i = 0; i < queues.length; i++) {
//			queues[i] = new ArtifactQueue();
//
		try (final Db db = new Db(ar.mavenIndex); final ResultSet rs = db.select(ar.query); final MavenVisitor mv = (MavenVisitor) cls.newInstance() ) {
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    log.info("Shutdown hook ...");

                    try {
                        mv.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            JarReader jr = new JarReader(ar.repo) {
                @Override
                void processEntry(Artifact artifact, String fileName, byte[] fileData) {
                    try {
                        mv.visitFileEntry(artifact, fileName, fileData);
                    } catch (Exception e) {
                        log.info("Exception: %s", e);
                    }
                }

                @Override
				void processFileNotFound() {
                	mv.filesNotFound++;
				}

            };

			int n = 0;

			while (rs.next()) {
				final long coordid = rs.getLong("coordid");
				final String groupid = rs.getString("groupid");
				final String artifactid = rs.getString("artifactid");
				final String version = rs.getString("version");
				final Date idate = df.parse(rs.getString("idate"));
				final Date mdate = df.parse(rs.getString("mdate"));
				final String path = rs.getString("path");

				final Artifact art = new Artifact(coordid, groupid, artifactid, version, idate, mdate, path);
//				queues[n % queues.length].add(art);
                jr.process(art, art.path);

				n++;

				if (n % 1000 == 0 ) {
                    log.info("Remaining Jars: %,d", n);
                }
			}

			log.info("No. jar files: %,d", n);
		}

//		try (final MavenVisitor mv = (MavenVisitor) cls.newInstance()) {
//			for (final ArtifactQueue queue : queues) {
//				new JarReader(ar.repo, queue) {
//					@Override
//					synchronized void processEntry(Artifact artifact, String fileName, byte[] fileData) {
//						try {
//							mv.visitFileEntry(artifact, fileName, fileData);
//						} catch (Exception e) {
//							log.info("Exception: %s", e);
//						}
//					}
//				}.start();
//			}
//
//			long items;
//			do {
//				Thread.sleep(30 * 1000);
//
//				items = 0;
//
//				for (final ArtifactQueue queue : queues) {
//					items += queue.size();
//				}
//
//				log.info("Remaining Jars: %,d", items);
//			} while (items > 0);
//		}
	}
}
