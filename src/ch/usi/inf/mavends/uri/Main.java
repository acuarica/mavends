package ch.usi.inf.mavends.uri;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.log.Log;

public final class Main {

	private static final Log log = new Log(System.out);

	public static final class Args {

		@Arg(key = "mavenindex", name = "Maven Index", desc = "Specifies the output uri list file (*aria2* format).")
		public String mavenIndex;

		@Arg(key = "urilist", name = "URI List", desc = "Specifies the output uri list file (*aria2* format).")
		public String uriList;

		@Arg(key = "query", name = "Query Filter", desc = "Specifies the output uri list file (*aria2* format).")
		public String query;

		@Arg(key = "mirrors", name = "Mirrors", desc = "Comma separated list of mirrors.")
		public String[] mirrors;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException,
			SQLException {
		final Args ar = ArgsParser.parse(args, new Args());

		try (final Db db = new Db(ar.mavenIndex); final UriList uri = new UriList(ar.uriList, ar.mirrors)) {
			ResultSet rs = db.select(ar.query);

			int n = 0;

			while (rs.next()) {
				final String path = rs.getString("path");
				final String pompath = rs.getString("pompath");

				n += 2;
				uri.emit(path);
				uri.emit(pompath);
			}

			log.info("No. emitted fetch files: %,d", n);
		}
	}
}
