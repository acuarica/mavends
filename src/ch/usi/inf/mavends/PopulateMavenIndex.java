package ch.usi.inf.mavends;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import ch.usi.inf.mavends.index.NexusConstants;
import ch.usi.inf.mavends.index.NexusIndexParser;
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
		public String nexusIndexPath;

		@Arg(key = "mavenindex", name = "Maven Index", desc = "Specifies the output path of the Maven Index DB file.")
		public String mavenIndexDbPath;

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

	private static byte[][] split(byte[] value, int length) {
		byte[][] res = new byte[length][];

		int prev = 0;
		int index = 0;
		for (int i = 0; i < value.length; i++) {
			if (value[i] == BAR) {
				res[index] = Arrays.copyOfRange(value, prev, i);
				index++;
				prev = i + 1;
			}
		}

		res[index] = Arrays.copyOfRange(value, prev, value.length);

		return res;
	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, ClassNotFoundException, SQLException, IOException {
		Args ar = ArgsParser.parse(args, Args.class);

		try (Db db = new Db(ar.mavenIndexDbPath); NexusIndexParser nip = new NexusIndexParser(ar.nexusIndexPath)) {
			Inserter artins = db
					.createInserter("insert into artifact (mdate, sha, groupid, artifactid, version, classifier, packaging, idate, size, is3, is4, is5, extension, artifactname, artifactdesc) values (date(?/1000, 'unixepoch' ), ?, ?, ?, ?, ?, ?, date(?/1000, 'unixepoch' ), ?, ?, ?, ?, ?, ?, ?)");

			Inserter delins = db
					.createInserter("insert into del (groupid, artifactid, version, classifier, packaging, mdate) values (?, ?, ?, ?, ?, date(?/1000, 'unixepoch'))");

			Inserter allins = db.createInserter("insert into allgroups (groupid) values (?)");

			Inserter rootins = db.createInserter("insert into rootgroups (groupid) values (?)");

			Inserter headerins = db
					.createInserter("insert into header (headb, creationdate) values (?, date(?/1000, 'unixepoch' ))");

			Inserter descins = db.createInserter("insert into descriptor (descriptor, idxinfo) values (?, ?)");

			long ndocs = 0;

			log.info("Inserting header...");
			headerins.insert(nip.headb, nip.headl);

			while (nip.hasNext()) {
				final NexusRecord nr = nip.next();

				ndocs++;

				if (ndocs % 100000 == 0) {
					log.info("ndocs: %,d", ndocs);
				}

				byte[] i;
				byte[] del;
				byte[] descriptor;

				if ((i = nr.get(I)) != null) {
					byte[] m = nr.get(M);
					byte[] sha = nr.get(SHA);
					byte[] u = nr.get(U);
					byte[] artifactname = nr.get(N);
					byte[] artifactdesc = nr.get(D);

					byte[] mdate = m;

					byte[][] us = split(u, 5);

					byte[] groupid = us[0];
					byte[] artifactid = us[1];
					byte[] version = us[2];
					byte[] classifier = us[3];
					classifier = Arrays.equals(NA, classifier) ? null : classifier;

					byte[][] is = split(i, 7);

					byte[] packaging = is[0];

					byte[] idate = is[1];
					byte[] size = is[2];

					byte[] is3 = is[3];
					byte[] is4 = is[4];
					byte[] is5 = is[5];

					byte[] extension = is[6];

					artins.insert(mdate, sha, groupid, artifactid, version, classifier, packaging, idate, size, is3,
							is4, is5, extension, artifactname, artifactdesc);
				} else if ((del = nr.get(DEL)) != null) {
					byte[] m = nr.get(M);

					byte[] mdate = m;

					byte[][] dels = split(del, 5);

					byte[] groupid = dels[0];
					byte[] artifactid = dels[1];
					byte[] version = dels[2];
					byte[] classifier = dels[3];
					classifier = Arrays.equals(NA, classifier) ? null : classifier;

					byte[] packaging = dels.length == 4 ? null : dels[4];

					delins.insert(groupid, artifactid, version, classifier, packaging, mdate);
				} else if (nr.get(ALL_GROUPS) != null) {
					log.info("Inserting allGroups...");

					byte[] allGroupsList = nr.get(ALL_GROUPS_LIST);

					for (byte[] groupid : split(allGroupsList)) {
						allins.insert(groupid);
					}
				} else if (nr.get(ROOT_GROUPS) != null) {
					log.info("Inserting rootGroups...");

					byte[] rootGroupsList = nr.get(ROOT_GROUPS_LIST);

					for (byte[] groupid : split(rootGroupsList)) {
						rootins.insert(groupid);
					}

				} else if ((descriptor = nr.get(DESCRIPTOR)) != null) {
					log.info("Inserting descriptor...");

					byte[] idxinfo = nr.get(IDXINFO);

					descins.insert(descriptor, idxinfo);
				}
			}
			log.info("ndocs: %,d", ndocs);

			db.conn.commit();
		}
	}
}
