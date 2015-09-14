package ch.usi.inf.mavends;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ch.usi.inf.mavends.analysis.DepsManager;
import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.NexusConstants;
import ch.usi.inf.mavends.index.PomDependency;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;

public class BuildMavenPom extends NexusConstants {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "mavenpom", name = "Maven Pom path", desc = "Specifies the path of the output file.")
		public String mavenPom;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException,
			FileNotFoundException, IOException {
		Args ar = ArgsParser.parse(args, new Args());

		final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(ar.mavenPom), BUFFER_SIZE)) {
			int n = 0;
			String line;
			while ((line = stdin.readLine()) != null) {
				final String[] parts = line.split("\\|");
				final String groupid = parts[1];
				final String artifactid = parts[2];
				final String version = parts[3];

				String path = MavenRecord.getPath(groupid, artifactid, version, null, "pom");

				try {
					List<PomDependency> deps = DepsManager.extractDeps(ar.repoDir + "/" + path);

					for (PomDependency dep : deps) {
						write(os, groupid, artifactid, version, dep.groupId, dep.artifactId, dep.version, dep.scope);
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
