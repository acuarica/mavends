package ch.usi.inf.mavends;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.NexusConstants;
import ch.usi.inf.mavends.index.NexusIndex;
import ch.usi.inf.mavends.index.NexusRecord;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Inserter;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class PopulateMavenIndex implements NexusConstants {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndex;

		@Arg(key = "mavenindex", name = "Maven Index", desc = "Specifies the output path of the Maven Index DB file.")
		public String mavenIndex;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException,
			IOException {
		Args ar = ArgsParser.parse(args, new Args());

		try (Db db = new Db(ar.mavenIndex); NexusIndex nip = new NexusIndex(ar.nexusIndex)) {
			Inserter artins = db
					.createInserter("insert into artifact (mdate, groupid, artifactid, version, classifier, packaging, idate, size, extension) values (date(?/1000, 'unixepoch' ), ?, ?, ?, ?, ?, date(?/1000, 'unixepoch' ), ?, ?)");

			Inserter headerins = db
					.createInserter("insert into header (headb, creationdate) values (?, date(?/1000, 'unixepoch' ))");

			log.info("Inserting header...");
			headerins.insert(nip.headb, nip.headl);

			while (nip.hasNext()) {
				NexusRecord nr = nip.next();

				byte[] u;

				if ((u = nr.get(U)) != null) {
					byte[] m = nr.get(M);
					byte[] i = nr.get(I);

					byte[][] us = MavenRecord.split(u, 5);

					String groupid = new String(us[0]);
					String artifactid = new String(us[1]);
					String version = new String(us[2]);
					String classifier = Arrays.equals(us[3], NA) ? null : new String(us[3]);

					byte[][] is = MavenRecord.split(i, 7);

					String packaging = new String(is[0]);
					byte[] idate = is[1];
					byte[] size = is[2];
					String extension = new String(is[6]);

					artins.insert(m, groupid, artifactid, version, classifier, packaging, idate, size, extension);
				}
			}

			log.info("Number of Records: %,d", nip.nrecs);

			db.conn.commit();
		}
	}
}
