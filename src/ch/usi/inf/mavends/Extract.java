package ch.usi.inf.mavends;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.extract.ClassAnalysis;
import ch.usi.inf.mavends.log.Log;

public class Extract {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(shortkey = "j", longkey = "jar", desc = "Specifies the path of the jar file to analyse.")
		public String jarPath;

		@Arg(shortkey = "d", longkey = "db", desc = "Specifies the path of the db output file.")
		public String dbPath;

	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		log.info("Using Index: %s", ar.jarPath);
		log.info("Output: %s", ar.dbPath);

		Class.forName("org.sqlite.JDBC");

		Connection conn = DriverManager.getConnection("jdbc:sqlite:"
				+ ar.dbPath);

		String sql = BuildMavenIndex.getResourceContent("cls.sql");
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();

		ClassAnalysis.searchJarFile(ar.jarPath, conn);
	}
}
