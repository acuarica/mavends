package ch.usi.inf.mavends;

import java.sql.ResultSet;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.extract.ClassAnalysis;
import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.util.Log;

public class BuildMavenClass {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index.")
		public String mavenIndexPath;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "Filter query", desc = "Specifies the path of the Maven repository.")
		public String query;

		@Arg(key = "mavenbytecode", name = "Maven Bytecode DB path", desc = "Specifies the path of the output db file.")
		public String mavenBytecodePath;

	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		Db db = new Db(ar.mavenBytecodePath);

		db.send("mavenbytecode.sql", "Creating database with");

		db.conn.setAutoCommit(false);

		ResultSet rs = new Db(ar.mavenIndexPath).select(ar.query);

		int n = 0;
		while (rs.next()) {
			// String gid = rs.getString("gid");
			// String aid = rs.getString("aid");
			// String ver = rs.getString("ver");
			String pid = rs.getString("pid");
			String path = rs.getString("path");

			ClassAnalysis.searchJarFile(ar.repoDir + "/" + path, db, pid);

			n++;
		}

		log.info("No. jar files: %d", n);

		db.conn.commit();
	}
}
