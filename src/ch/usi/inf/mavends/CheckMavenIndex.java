package ch.usi.inf.mavends;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.index.MavenRecordChecker;
import ch.usi.inf.mavends.index.NexusIndexParser;
import ch.usi.inf.mavends.index.NexusRecord;
import ch.usi.inf.mavends.util.Log;

public class CheckMavenIndex {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index path", desc = "Specifies the input path of the Nexus Index file to check.")
		public String nexusIndexPath;

	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		long ndocs = 0;

		try (NexusIndexParser nip = new NexusIndexParser(ar.nexusIndexPath)) {
			for (NexusRecord nr : nip) {
				ndocs++;

				if (ndocs % 100000 == 0) {
					log.info("docs: %,d", ndocs);
				}

				new MavenRecordChecker(nr);
			}
		}

		log.info("Nexus Index checked");
		log.info("ndocs: %,d", ndocs);
	}
}
