package ch.usi.inf.mavends.uri;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.usi.inf.mavends.NexusConstants;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.log.Log;

public final class Main {

	private static final Log log = new Log(System.out);

	public static final class Args {

		@Arg(key = "mavenindex", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String mavenIndex;

		@Arg(key = "urilist", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String uriList;

		@Arg(key = "query", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String query;

		@Arg(key = "mirrors", name = "mirrors", desc = "Comma separated list of mirrors.")
		public String[] mirrors;

	}

	private static void emit(String path, String[] mirrors, PrintStream os) throws IOException {
		for (String mirror : mirrors) {
			os.format("%s/%s\t", mirror, path);
		}

		os.println();
		os.format("\tout=%s", path);
		os.println();
	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException,
			SQLException {
		Args ar = ArgsParser.parse(args, new Args());

		try (Db db = new Db(ar.mavenIndex); PrintStream out = new PrintStream(ar.uriList)) {
			ResultSet rs = db.select(ar.query);

			int n = 0;

			while (rs.next()) {
				final String groupid = rs.getString("groupid");
				final String artifactid = rs.getString("artifactid");
				final String version = rs.getString("version");
				final String classifier = rs.getString("classifier");
				final String extension = rs.getString("extension");

				n++;
				emit(NexusConstants.getPath(groupid, artifactid, version, classifier, extension), ar.mirrors, out);

				if (classifier == null) {
					n++;
					emit(NexusConstants.getPath(groupid, artifactid, version, classifier, "pom"), ar.mirrors, out);
				}
			}

			log.info("No. emitted fetch files: %,d", n);
		}
	}
}
