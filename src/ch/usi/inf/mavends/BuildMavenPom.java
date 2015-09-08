package ch.usi.inf.mavends;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ch.usi.inf.mavends.analysis.DepsManager;
import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.PomDependency;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Inserter;

public class BuildMavenPom {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index.")
		public String mavenIndexPath;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "Filter query", desc = "Specifies the path of the Maven repository.")
		public String query;

		@Arg(key = "mavenpom", name = "Maven Pom path", desc = "Specifies the path of the output db file.")
		public String mavenPomPath;

	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, ClassNotFoundException, SQLException {
		Args ar = ArgsParser.parse(args, Args.class);

		try (Db dbi = new Db(ar.mavenIndexPath);
				ResultSet rs = dbi
						.select("select a.groupid as groupid, a.artifactid as artifactid, a.version as version from ("
								+ ar.query + ") t inner join artifact a on a.coorid = t.coorid");
				Db db = new Db(ar.mavenPomPath);
				Inserter ins = db
						.createInserter("insert into dep (gid, aid, ver, dgid, daid, dver, dscope) values (?, ?, ?, ?, ?, ?, ?)")) {

			int n = 0;
			while (rs.next()) {
				String groupid = rs.getString("groupid");
				String artifactid = rs.getString("artifactid");
				String version = rs.getString("version");

				String path = MavenRecord.getPath(groupid, artifactid, version, null, "pom");

				List<PomDependency> deps;
				try {
					deps = DepsManager.extractDeps(ar.repoDir + "/" + path);

					for (PomDependency dep : deps) {
						ins.insert(groupid, artifactid, version, dep.groupId, dep.artifactId, dep.version, dep.scope);
					}

					db.conn.commit();
				} catch (SAXException | IOException | ParserConfigurationException e) {
					log.info("Exception in %s (# %d): %s", path, n, e);
				}

				n++;
			}

			log.info("No. pom files: %d", n);
		}
	}
}
