package ch.usi.inf.mavends;

import java.io.PrintStream;
import java.sql.ResultSet;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.util.Log;

public class BuildUriList {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index DB.")
		public String mavenIndexPath;

		@Arg(key = "urilist", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String uriListPath;

		@Arg(key = "query", name = "Filter query", desc = "Specifies the SQL filter query of artifacts to download.")
		public String query;

		@Arg(key = "mirrors", name = "mirrors", desc = "Comma separated list of mirrors.")
		public String[] mirrors;

	}

	private static void emitFetchFile(String path, String[] mirrors,
			PrintStream out) {
		for (String mirror : mirrors) {
			out.format("%s/%s\t", mirror, path);
		}

		out.println();
		out.format("\tout=%s\n", path);
	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		Db db = new Db(ar.mavenIndexPath);

		try (PrintStream out = new PrintStream(ar.uriListPath)) {
			ResultSet rs = db.select(ar.query);

			int n = 0;
			while (rs.next()) {
				String path = rs.getString("path");

				n++;
				emitFetchFile(path, ar.mirrors, out);
			}

			log.info("No. emitted fetch files: %,d", n);
		}
	}
}
