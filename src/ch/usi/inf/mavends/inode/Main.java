package ch.usi.inf.mavends.inode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.usi.inf.mavends.util.Resource;
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
		final InodeWorker[] ws = new InodeWorker[numberOfProcessors];
		final Db[] dbs = new Db[ws.length];

		for (int i = 0; i < ws.length; i++) {
			final String dbp = ar.mavenInode + "-" + i + ".ifile.sqlite3";

			Files.deleteIfExists(Paths.get(dbp));

			dbs[i] = new Db(dbp);
			final Db dbw = dbs[i];

			dbw.execute(Resource.get("maveninode.sql"));
			dbw.commit();

			final Statement inodeStmt = dbw
					.createStatement("insert into inode (originalsize, compressedsize, crc32, sha1, cdata) values (?,?,?,?,?)");

			final Statement ifileStmt = dbw
					.createStatement("insert into ifile (coordid, filename, inodeid, sha1) values (?,?,?,?)");

			ws[i] = new InodeWorker(ar.repoDir) {

				@Override
				void processEntry(long coordid, String filename, long size, long compressedSize, long crc, String sha1,
						byte[] cdata) throws IOException, SQLException {
					inodeStmt.execute(size, compressedSize, crc, sha1, cdata);
					ifileStmt.execute(coordid, filename, -1, sha1);
				}

				@Override
				void processJar() throws SQLException {
					dbw.commit();
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

		try (final Db db = new Db(ar.mavenInode)) {
			for (int i = 0; i < ws.length; i++) {
				log.info("Importing from worker %d", i);

				dbs[i].close();

				db.attach(dbs[i].databasePath, "dbw");
				db.execute("insert into inode (originalsize, compressedsize, crc32, sha1, cdata) select originalsize, compressedsize, crc32, sha1, cdata from dbw.inode");
				db.execute("insert into ifile (coordid, filename, inodeid, sha1) select coordid, filename, (select inodeid from inode where inode.sha1=sha1), sha1 from dbw.ifile");
				db.commit();
				db.detach("dbw");
			}
		}
	}
}
