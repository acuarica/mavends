package ch.usi.inf.mavends;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.index.MavenIndex;
import ch.usi.inf.mavends.index.MavenIndexBuilder;
import ch.usi.inf.mavends.log.Log;

public class BuildMavenIndexDb {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(shortkey = "i", longkey = "nexusindex", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndexPath;

		@Arg(shortkey = "o", longkey = "mavenindex", desc = "Specifies the output path of the Maven Index file (SQLite Database).")
		public String mavenIndexPath;

	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		log.info("Nexus Index path: %s", ar.nexusIndexPath);
		log.info("Maven Index path: %s ", ar.mavenIndexPath);

		MavenIndex mi = new MavenIndex(ar.mavenIndexPath);

		sendSql(mi, "mavenindexdb.sql", "Maven Index SQL Schema");

		mi.conn.setAutoCommit(false);
		MavenIndexBuilder.build(ar.nexusIndexPath, mi.conn);
		mi.conn.commit();
		mi.conn.setAutoCommit(true);
	}

	private static void sendSql(MavenIndex mi, String path, String message)
			throws IOException, SQLException {
		String sql = getResourceContent(path);
		log.info(message + ": " + sql);
		mi.execute(sql);
	}

	public static String getResourceContent(String path) throws IOException {
		ClassLoader cl = BuildMavenIndexDb.class.getClassLoader();
		InputStream in = cl.getResourceAsStream(path);

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null) {
			out.append(line + "\n");
		}

		return out.toString();
	}
}
