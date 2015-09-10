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

		try (NexusIndex ni = new NexusIndex(ar.nexusIndex)) {
			log.info("Header byte: %d", ni.headb);
			log.info("Creation Date: %s", ni.creationDate);

			MavenRecord mr = new MavenRecord();

			while (ni.hasNext()) {
				NexusRecord nr = ni.next();
				mr.check(nr);
			}

			log.info("Number of Records: %,d", ni.recordCount);
			log.info("Number of Artifacts: %,d", mr.artCount);
			log.info("Number of Deleted Artifacts: %,d", mr.delCount);
			log.info("Number of DESCRIPTOR: %,d", mr.descriptorCount);
			log.info("Number of allGroups: %,d", mr.allGroupsCount);
			log.info("Number of rootGroups: %,d", mr.rootGroupsCount);
		}
	}
}
