package ch.usi.inf.mavends;

import java.io.PrintStream;
import java.sql.ResultSet;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.index.MavenIndex;
import ch.usi.inf.mavends.log.Log;

public class BuildUriList {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(shortkey = "i", longkey = "mavenindex", desc = "Specifies the path of the Maven Index (SQLite DB).")
		public String mavenIndexPath;

		@Arg(shortkey = "u", longkey = "urilist", desc = "Specifies the output uri list file(aria2 format).")
		public String uriListPath;

		@Arg(shortkey = "q", longkey = "query", desc = "Specifies the SQL query of artifacts to download.")
		public String query;

		@Arg(shortkey = "m", longkey = "mirrors", desc = "Comma separated list of mirrors.")
		public String[] mirrors;

	}

	private static void emitDownloadFile(String path, String[] mirrors,
			PrintStream out) {
		for (String mirror : mirrors) {
			out.format("%s/%s\t", mirror, path);
		}

		out.println();
		out.format("\tout=%s\n", path);
	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		log.info("Maven Index: %s", ar.mavenIndexPath);
		log.info("URI List: %s", ar.uriListPath);
		log.info("SQL Query to download: %s", ar.query);

		log.info("Using %d mirrors:", ar.mirrors.length);
		for (String mirror : ar.mirrors) {
			log.info("  * %s", mirror);
		}

		MavenIndex mi = new MavenIndex(ar.mavenIndexPath);

		try (PrintStream out = new PrintStream(ar.uriListPath)) {
			log.info("Using artifacts from: %s", ar.query);

			ResultSet rs = mi.select(ar.query);

			int n = 0;
			while (rs.next()) {
				String path = rs.getString("path");

				n++;

				emitDownloadFile(path, ar.mirrors, out);
			}

			log.info("No. emitted download files: %d", n);
		}
	}
}
