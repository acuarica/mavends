package ch.usi.inf.mavends;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.extract.MavenIndexBuilder;
import ch.usi.inf.mavends.util.Db;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class BuildMavenIndexDb {

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index path", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndexPath;

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the output path of the Maven Index file (SQLite Database).")
		public String mavenIndexDbPath;

	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		Db db = new Db(ar.mavenIndexDbPath);

		db.send("mavenindexdb.sql", "Maven Index SQL Schema");

		db.conn.setAutoCommit(false);
		MavenIndexBuilder.build(ar.nexusIndexPath, db);
		db.conn.commit();
	}
}
