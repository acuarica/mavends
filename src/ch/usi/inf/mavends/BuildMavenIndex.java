package ch.usi.inf.mavends;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.index.MavenIndexBuilder;
import ch.usi.inf.mavends.log.Log;

public class BuildMavenIndex {

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

		String sql = getResourceContent("mavenindexdb.sql");
		log.info("Maven Index SQL Schema: %s ", sql);

		Class.forName("org.sqlite.JDBC");

		Connection c = DriverManager.getConnection("jdbc:sqlite:"
				+ ar.mavenIndexPath);

		Statement stmt = c.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();

		c.setAutoCommit(false);
		MavenIndexBuilder.build(ar.nexusIndexPath, c);
		c.commit();
	}

	private static String getResourceContent(String path) throws IOException {
		ClassLoader cl = BuildMavenIndex.class.getClassLoader();
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
