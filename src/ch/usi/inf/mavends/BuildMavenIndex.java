package ch.usi.inf.mavends;

import java.util.Arrays;
import java.util.Date;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.db.Inserter;
import ch.usi.inf.mavends.index.NexusIndexParser;
import ch.usi.inf.mavends.index.NexusRecord;
import ch.usi.inf.mavends.util.Log;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class BuildMavenIndex {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index path", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndexPath;

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the output path of the Maven Index DB file.")
		public String mavenIndexDbPath;

	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		Db db = new Db(ar.mavenIndexDbPath);

		db.send("mavenindex-tables.sql", "Maven Index SQL Schema");

		db.conn.setAutoCommit(false);

		Inserter artins = db
				.createInserter("insert into artifact (mdate, sha, groupid, artifactid, version, classifier, packaging, idate, size, is3, is4, is5, extension, artifactname, artifactdesc) values (date(?, 'unixepoch' ), ?, ?, ?, ?, ?, ?, date(?, 'unixepoch' ), ?, ?, ?, ?, ?, ?, ?)");

		Inserter allins = db
				.createInserter("insert into allgroups (groupid) values (?)");

		Inserter rootins = db
				.createInserter("insert into rootgroups (groupid) values (?)");

		Inserter headerins = db
				.createInserter("insert into header (headb, creationdate) values (?, date(?, 'unixepoch' ))");

		Inserter descins = db
				.createInserter("insert into descriptor (descriptor, idxinfo) values (?, ?)");

		long ndocs = 0;

		try (NexusIndexParser nip = new NexusIndexParser(ar.nexusIndexPath)) {
			log.info("Inserting header...");
			headerins.insert(nip.headb, new Date(nip.headl / 1000));

			for (NexusRecord nr : nip) {
				ndocs++;

				if (ndocs % 100000 == 0) {
					log.info("ndocs: %,d", ndocs);
				}

				String i = nr.get("i");

				if (i != null) {
					String sha = nr.get("1");
					String m = nr.get("m");
					String u = nr.get("u");
					String artifactname = nr.get("n");
					String artifactdesc = nr.get("d");

					long mdate = m != null ? Long.parseLong(m) / 1000 : 0;

					String[] us = u.split("\\|");

					String groupid = us[0];
					String artifactid = us[1];
					String version = us[2];
					String classifier = "NA".equals(us[3]) ? null : us[3];

					String[] is = i.split("\\|");

					String packaging = is[0];

					long idate = Long.parseLong(is[1]) / 1000;
					String size = is[2];
					String is3 = is[3];
					String is4 = is[4];
					String is5 = is[5];

					String extension = is[6];

					artins.insert(mdate, sha, groupid, artifactid, version,
							classifier, packaging, idate, size, is3, is4, is5,
							extension, artifactname, artifactdesc);
				} else {
					String allGroups = nr.get("allGroups");
					String allGroupsList = nr.get("allGroupsList");
					String rootGroups = nr.get("rootGroups");
					String rootGroupsList = nr.get("rootGroupsList");
					String descriptor = nr.get("DESCRIPTOR");
					String idxinfo = nr.get("IDXINFO");

					if (allGroups != null) {
						log.info("Inserting allGroups...");

						for (String g : Arrays.asList(allGroupsList
								.split("\\|"))) {
							allins.insert(g);
						}
					} else if (rootGroups != null) {

						log.info("Inserting rootGroups...");

						for (String g : Arrays.asList(rootGroupsList
								.split("\\|"))) {
							rootins.insert(g);
						}
					} else if (descriptor != null) {
						log.info("Inserting descriptor...");

						descins.insert(descriptor, idxinfo);
					}
				}
			}
		}

		db.send("mavenindex-views.sql", "Post setup");

		log.info("ndocs: %,d", ndocs);

		db.conn.commit();
	}
}
