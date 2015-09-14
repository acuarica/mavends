package ch.usi.inf.mavends;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.NexusConstants;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;

public class BuildUriList implements NexusConstants {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "urilist", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String uriList;

		@Arg(key = "mirrors", name = "mirrors", desc = "Comma separated list of mirrors.")
		public String[] mirrors;

	}

	private static void emit(String path, String[] mirrors, BufferedOutputStream os) throws IOException {
		for (String mirror : mirrors) {
			os.write(mirror.getBytes());
			os.write("/".getBytes());
			os.write(path.getBytes());
			os.write("\t".getBytes());
		}

		os.write(CRLF);
		os.write("\tout=".getBytes());
		os.write(path.getBytes());
		os.write(CRLF);
	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException {
		Args ar = ArgsParser.parse(args, new Args());

		final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(ar.uriList), BUFFER_SIZE)) {
			int n = 0;

			String line;
			while ((line = stdin.readLine()) != null) {
				final String[] parts = line.split("\\|");
				final String groupid = parts[1];
				final String artifactid = parts[2];
				final String version = parts[3];
				final String classifier = parts[4].equals("") ? null : parts[4];
				final String extension = parts[5];

				n++;
				emit(MavenRecord.getPath(groupid, artifactid, version, classifier, extension), ar.mirrors, os);

				if (classifier == null) {
					n++;
					emit(MavenRecord.getPath(groupid, artifactid, version, classifier, "pom"), ar.mirrors, os);
				}
			}

			log.info("No. emitted fetch files: %,d", n);
		}
	}
}
