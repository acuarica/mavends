package ch.usi.inf.mavends.inode;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Statement;
import ch.usi.inf.mavends.util.log.Log;

public final class Main {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index DB.")
		public String mavenIndex;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String query;

		@Arg(key = "maveninode", name = "Maven FS DB path", desc = "Specifies the path of the output db file.")
		public String mavenInode;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException,
			NoSuchAlgorithmException, InterruptedException, IOException {
		final Args ar = ArgsParser.parse(args, new Args());

		final int numberOfProcessors = Runtime.getRuntime().availableProcessors();

		log.info("Number of processors: %d", numberOfProcessors);
		InodeWorker[] ws = new InodeWorker[numberOfProcessors];

		try (final Db db = new Db(ar.mavenInode)) {

			final Statement inodeins = db
					.createStatement("insert into inode (originalsize, compressedsize, crc32, sha1, cdata) values (?,?,?,?,?)");

			final Statement ifileins = db
					.createStatement("insert into ifile (coordid, filename, inodeid) values (?,?,(select inodeid from inode where sha1=?))");

			for (int i = 0; i < ws.length; i++) {
				ws[i] = new InodeWorker(ar.repoDir) {

					@Override
					void processEntry(long coordid, String filename, long size, long compressedSize, long crc,
							String sha1, byte[] cdata) throws IOException, SQLException {
						synchronized (db) {
							inodeins.execute(size, compressedSize, crc, sha1, cdata);
							ifileins.execute(coordid, filename, sha1);
						}
					}

					@Override
					void processJar() throws SQLException {
						synchronized (db) {
							db.commit();
						}
					}
				};
			}

			try (final Db dbi = new Db(ar.mavenIndex)) {
				final ResultSet rs = dbi.select(ar.query);

				int numberOfZipFiles = 0;
				while (rs.next()) {
					final long coordid = rs.getLong("coordid");
					final String path = rs.getString("path");

					ws[numberOfZipFiles % ws.length].add(coordid, path);
					numberOfZipFiles++;
				}

				log.info("Number of ZIP files: %,d", numberOfZipFiles);
			}

			for (final InodeWorker w : ws) {
				log.info("Starting Worker %s with size: %,d", w, w.size());
				w.start();
			}

			long items;
			do {
				Thread.sleep(30 * 1000);

				items = 0;
				for (final InodeWorker w : ws) {
					items += w.size();
				}

				log.info("Remaining ZIP files to process: %,d", items);
			} while (items > 0);
		}
	}
}
