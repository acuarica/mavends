package ch.usi.inf.mavends;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;

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

	private static void emitFetchFile(String path, String[] mirrors, PrintStream out) {
		for (String mirror : mirrors) {
			out.format("%s/%s\t", mirror, path);
		}

		out.println();
		out.format("\tout=%s\n", path);
	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException,
			FileNotFoundException, SQLException {
		Args ar = ArgsParser.parse(args, new Args());

		try (Db db = new Db(ar.mavenIndexPath); PrintStream out = new PrintStream(ar.uriListPath)) {
			ResultSet rs = db
					.select("select a.groupid as groupid, a.artifactid as artifactid, a.version as version, a.classifier as classifier, a.extension as extension from ("
							+ ar.query + ") t inner join artifact a on a.coorid = t.coorid");

			int n = 0;
			while (rs.next()) {
				String groupid = rs.getString("groupid");
				String artifactid = rs.getString("artifactid");
				String version = rs.getString("version");
				String classifier = rs.getString("classifier");
				String extension = rs.getString("extension");

				String path = MavenRecord.getPath(groupid, artifactid, version, classifier, extension);

				n++;
				emitFetchFile(path, ar.mirrors, out);

				if (classifier == null) {
					n++;

					path = MavenRecord.getPath(groupid, artifactid, version, null, "pom");

					emitFetchFile(path, ar.mirrors, out);
				}
			}

			log.info("No. emitted fetch files: %,d", n);
		}
	}
}
