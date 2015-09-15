package ch.usi.inf.mavends.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.log.Log;

public final class Main {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index DB.")
		public String mavenIndex;

		@Arg(key = "query", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String query;

		@Arg(key = "maveninode", name = "Maven FS DB path", desc = "Specifies the path of the output db file.")
		public String mavenInode;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException {
		final Args ar = ArgsParser.parse(args, new Args());

		try (final Db dbi = new Db(ar.mavenIndex); final Db db = new Db(ar.mavenInode)) {
			final ResultSet rs = dbi.select(ar.query);

			int n = 0;
long total=0;
			while (rs.next()) {
				final long coordid = rs.getLong("coordid");

				final ResultSet fs = db.select("select * from file where coordid = ?", coordid);

				n++;

				while (fs.next()) {
					final String filename = fs.getString("filename");
					final byte[] data = fs.getBytes("data");
					total += data.length;

					// ins.insert(coordid, ze.getName(), ze.getSize(),
					// ze.getCompressedSize(), ze.getCrc(), sha1,
					// cdata);
				}
			}

			log.info("No. jar files: %d", n);
			log.info("Total: %,d", total);
		}
	}
}
