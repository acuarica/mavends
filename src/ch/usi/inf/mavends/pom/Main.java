package ch.usi.inf.mavends.pom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ch.usi.inf.mavends.NexusConstants;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Inserter;
import ch.usi.inf.mavends.util.log.Log;

public final class Main {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Pom path", desc = "Specifies the path of the output file.")
		public String mavenIndex;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String query;

		@Arg(key = "mavenpom", name = "Maven Pom path", desc = "Specifies the path of the output file.")
		public String mavenPom;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException,
			FileNotFoundException, IOException, SQLException {
		Args ar = ArgsParser.parse(args, new Args());

		try (Db dbi = new Db(ar.mavenIndex); Db db = new Db(ar.mavenPom)) {
			ResultSet rs = dbi.select(ar.query);

			int n = 0;

			Inserter ins = db
					.createInserter("insert into dep (gid, aid, ver, dgid, daid, dver, dscope) values (?,?,?,?,?,?,?)");

			while (rs.next()) {
				final String groupid = rs.getString("groupid");
				final String artifactid = rs.getString("artifactid");
				final String version = rs.getString("version");

				String path = NexusConstants.getPath(groupid, artifactid, version, null, "pom");

				try {
					List<Dependency> deps = DepsParser.extractDeps(ar.repoDir + "/" + path);

					for (Dependency dep : deps) {
						ins.insert(groupid, artifactid, version, dep.groupId, dep.artifactId, dep.version, dep.scope);
					}
				} catch (SAXException | IOException | ParserConfigurationException e) {
					log.info("Exception in %s (# %d): %s", path, n, e);
				}

				n++;
			}

			log.info("No. pom files: %d", n);
		}
	}
}
