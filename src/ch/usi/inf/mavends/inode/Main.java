package ch.usi.inf.mavends.inode;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Inserter;
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

		int numberOfProcessors = Runtime.getRuntime().availableProcessors();

		log.info("Number of processors: %d", numberOfProcessors);
		InodeWorker[] ws = new InodeWorker[numberOfProcessors];

		for (int i = 0; i < ws.length; i++) {
			ws[i] = new InodeWorker(ar.repoDir);
		}

		try (final Db db = new Db(ar.mavenIndex)) {
			final ResultSet rs = db.select(ar.query);

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

		for (final InodeWorker w : ws) {
			w.join();
		}

		//
		// try (Db db = new Db(ar.mavenInode)) {
		// // Inserter ins = db
		// //
		// .createInserter("insert into file (coordid, filename, originalsize, compressedsize, crc32, sha1, cdata) values (?,?,?,?,?,?,?)");
		//
		//
		// int items;
		// do {
		// items = 0;
		//
		// List<InodeThread.ReadyEntry> ls = new
		// ArrayList<InodeThread.ReadyEntry>();
		// for (InodeThread t : ts) {
		// items += t.queue.size();
		//
		// synchronized (t.ready) {
		// items += t.ready.size();
		//
		// Iterator<InodeThread.ReadyEntry> it = t.ready.iterator();
		//
		// while (it.hasNext()) {
		// InodeThread.ReadyEntry e = it.next();
		// ls.add(e);
		// it.remove();
		// }
		// }
		// }
		//
		// // for (InodeThread.ReadyEntry e : ls) {
		// // ins.insert(e.coordid, e.filename, e.size, e.compressedSize, e.crc,
		// e.sha1, e.cdata);
		// // }
		// // db.commit();
		// } while (items > 0);
		// }
		System.out.println("main.end");
	}
}
