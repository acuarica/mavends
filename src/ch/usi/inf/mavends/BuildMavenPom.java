package ch.usi.inf.mavends;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.db.Inserter;
import ch.usi.inf.mavends.extract.DepsManager;
import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.PomDependency;
import ch.usi.inf.mavends.util.Log;

public class BuildMavenPomDb {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index.")
		public String mavenIndexDbPath;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "Filter query", desc = "Specifies the path of the Maven repository.")
		public String query;

		@Arg(key = "mavenpom", name = "Maven Pom path", desc = "Specifies the path of the output db file.")
		public String mavenPomDbPath;

	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		Db db = new Db(ar.mavenPomDbPath);

		db.send("mavenpomdb.sql", "SQL");

		db.conn.setAutoCommit(false);

		Inserter ins = db
				.createInserter("insert into dep (gid, aid, ver, dgid, daid, dver, dscope) values (?, ?, ?, ?, ?, ?, ?)");

		ResultSet rs = new Db(ar.mavenIndexDbPath).select(ar.query);

		int n = 0;
		while (rs.next()) {
			String gid = rs.getString("gid");
			String aid = rs.getString("aid");
			String ver = rs.getString("ver");
			String path = MavenRecord.getPath(gid, aid, ver, null, "pom");

			List<PomDependency> deps;
			try {
				deps = DepsManager.extractDeps(ar.repoDir + "/" + path);
				try {

					for (PomDependency dep : deps) {
						ins.insert(gid, aid, ver, dep.groupId, dep.artifactId,
								dep.version, dep.scope);
					}
				} catch (RuntimeException e) {
					System.out.println(deps);
					log.info("SQL Exception in %s (# %d): %s", path, n, e);
					throw e;
				}
			} catch (SAXException | IOException | ParserConfigurationException e) {
				log.info("Exception in %s (# %d): %s", path, n, e);
			}

			n++;
		}

		log.info("No. pom files: %d", n);

		db.conn.commit();
	}
}
