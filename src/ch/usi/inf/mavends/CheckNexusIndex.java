package ch.usi.inf.mavends;

import java.io.IOException;
import java.sql.SQLException;

import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.NexusIndexParser;
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

		@Arg(key = "nexusindex", name = "Nexus Index", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndexPath;

	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, ClassNotFoundException, SQLException, IOException {
		Args ar = ArgsParser.parse(args, Args.class);

		try (NexusIndexParser nip = new NexusIndexParser(ar.nexusIndexPath)) {
			long ndocs = 0;

			while (nip.hasNext()) {
				NexusRecord nr = nip.next();

				ndocs++;

				if (ndocs % 100000 == 0) {
					log.info("ndocs: %,d", ndocs);
				}

				MavenRecord.check(nr);
			}

			log.info("ndocs: %,d", ndocs);
		}
	}
}
