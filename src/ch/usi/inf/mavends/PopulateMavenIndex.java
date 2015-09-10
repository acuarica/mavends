package ch.usi.inf.mavends;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

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

	private static ArrayList<byte[]> split(byte[] value) {
		ArrayList<byte[]> res = new ArrayList<byte[]>();

		int prev = 0;
		for (int i = 0; i < value.length; i++) {
			if (value[i] == BAR) {
				res.add(Arrays.copyOfRange(value, prev, i));
				prev = i + 1;
			}
		}

		res.add(Arrays.copyOfRange(value, prev, value.length));

		return res;
	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException,
			ClassNotFoundException, SQLException {
		Args ar = ArgsParser.parse(args, new Args());

		try (Db db = new Db(ar.mavenIndex); NexusIndex nip = new NexusIndex(ar.nexusIndex)) {
			Inserter artins = db
					.createInserter("insert into artifact (mdate, sha, groupid, artifactid, version, classifier, packaging, idate, size, is3, is4, is5, extension) values (date(?, 'unixepoch' ), ?, ?, ?, ?, ?, ?, date(?, 'unixepoch' ), ?, ?, ?, ?, ?)");

			Inserter delins = db
					.createInserter("insert into del (groupid, artifactid, version, classifier, packaging, mdate) values (?, ?, ?, ?, ?, date(?, 'unixepoch'))");

			Inserter allins = db.createInserter("insert into allgroups (groupid) values (?)");

			Inserter rootins = db.createInserter("insert into rootgroups (groupid) values (?)");

			Inserter headerins = db
					.createInserter("insert into header (headb, creationdate) values (?, date(?, 'unixepoch' ))");

			Inserter descins = db.createInserter("insert into descriptor (descriptor, idxinfo) values (?, ?)");

			log.info("Inserting header...");
			headerins.insert(nip.headb, new Date(nip.headl / 1000));

			while (nip.hasNext()) {
				NexusRecord nr = nip.next();

				// if (ndocs % 100000 == 0) {
				// log.info("ndocs: %,d", ndocs);
				// }

				byte[] m;
				byte[] u;
				byte[] i;
				//byte[] del;
				//byte[] sha;
				// byte[] n;
				// byte[] d;
				//byte[] descriptor;

				if ((m = nr.get(M)) != null) {
					if ((u = nr.get(U)) != null) {
						i = nr.get(I);
						// sha = nr.get(SHA);
						// n = nr.get(N);
						// d = nr.get(D);

						byte[][] us = MavenRecord.split(u, 5);

						byte[] groupid = us[0];
						byte[] artifactid = us[1];
						byte[] version = us[2];
						byte[] classifier = Arrays.equals(us[3], NA) ? null : us[3];

						byte[][] is = MavenRecord.split(i, 7);

						byte[] packaging = is[0];
						byte[] idate = is[1];
						byte[] size = is[2];
						// byte[] is3 = is[3];
						// byte[] is4 = is[4];
						// byte[] is5 = is[5];
						byte[] extension = is[6];

						artins.insert(m, groupid, artifactid, version, classifier, packaging, idate, size, extension);
					} 
//					else if ((del = nr.get(DEL)) != null) {
//						byte[][] dels = MavenRecord.split(del, 5);
//
//						byte[] groupid = dels[0];
//						byte[] artifactid = dels[1];
//						byte[] version = dels[2];
//						byte[] classifier = Arrays.equals(dels[3], NA) ? null : dels[3];
//						byte[] packaging = dels[3] != null ? null : dels[4];
//
//						// delins.insert(groupid, artifactid, version,
//						// classifier, packaging, m);
//					}
//				} else if ((descriptor = nr.get(DESCRIPTOR)) != null) {
//					log.info("Inserting descriptor...");
//
//					byte[] idxinfo = nr.get(IDXINFO);
//
//					// descins.insert(descriptor, idxinfo);
//				} else if (nr.get(ALL_GROUPS) != null) {
//					log.info("Inserting allGroups...");
//
//					byte[] allGroupsList = nr.get(ALL_GROUPS_LIST);
//
//					for (byte[] groupid : split(allGroupsList)) {
//						// allins.insert(groupid);
//					}
//				} else if (nr.get(ROOT_GROUPS) != null) {
//					log.info("Inserting rootGroups...");
//
//					byte[] rootGroupsList = nr.get(ROOT_GROUPS_LIST);
//
//					for (byte[] groupid : split(rootGroupsList)) {
//						// rootins.insert(groupid);
//					}
				}
			}

			log.info("ndocs: %,d", nip.nrecs);

			db.conn.commit();
		}
	}
}
