package ch.usi.inf.mavends;

import java.sql.ResultSet;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.extract.ClassAnalysis;
import ch.usi.inf.mavends.extract.MavenIndexBuilder;
import ch.usi.inf.mavends.util.Db;
import ch.usi.inf.mavends.util.Log;

public class BuildMavenBytecodeDb {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index.")
		public String mavenIndexDbPath;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "Filter query", desc = "Specifies the path of the Maven repository.")
		public String query;

		@Arg(key = "mavenbytecodedb", name = "Maven Bytecode DB path", desc = "Specifies the path of the output db file.")
		public String mavenBytecodeDbPath;

	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		Db db = new Db(ar.mavenBytecodeDbPath);

		db.send("mavenbytecodedb.sql", "Creating database with");

		db.conn.setAutoCommit(false);

		ResultSet rs = new Db(ar.mavenIndexDbPath).select(ar.query);

		int n = 0;
		while (rs.next()) {
			String gid = rs.getString("gid");
			String aid = rs.getString("aid");
			String ver = rs.getString("ver");
			String path = MavenIndexBuilder.getPath(gid, aid, ver, "", "jar");

			ClassAnalysis.searchJarFile(ar.repoDir + "/" + path, db, gid, aid,
					ver);

			n++;
		}

		log.info("No. jar files: %d", n);

		db.conn.commit();
	}
}
