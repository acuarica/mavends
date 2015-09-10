package ch.usi.inf.mavends;

import java.io.IOException;

import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.NexusIndex;
import ch.usi.inf.mavends.index.NexusRecord;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class CheckNexusIndex {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index", desc = "Specifies the Nexus Index file (input path).")
		public String nexusIndex;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException {
		Args ar = ArgsParser.parse(args, new Args());

		try (NexusIndex nip = new NexusIndex(ar.nexusIndex)) {
			while (nip.hasNext()) {
				NexusRecord nr = nip.next();
				MavenRecord.check(nr);
			}

			log.info("Number of Records: %,d", nip.nrecs);
		}
	}
}
