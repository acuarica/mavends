package ch.usi.inf.mavends;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.usi.inf.mavends.index.NexusConstants;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;

public class BuildUriList implements NexusConstants {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index DB.")
		public String mavenIndex;

		@Arg(key = "urilist", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String uriList;

		@Arg(key = "mirrors", name = "mirrors", desc = "Comma separated list of mirrors.")
		public String[] mirrors;

	}

	private static void emitFetchFile(String path, String[] mirrors, BufferedOutputStream os) throws IOException {
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

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(ar.uriList), BUFFER_SIZE)) {
			int n = 0;

			String line;
			while ((line = stdin.readLine()) != null) {
				String path = line.split("\\|")[1];
				n++;
				emitFetchFile(path, ar.mirrors, os);
			}

			log.info("No. emitted fetch files: %,d", n);
		}
	}
}
