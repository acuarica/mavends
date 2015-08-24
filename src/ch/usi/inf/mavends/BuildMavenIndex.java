package ch.usi.inf.mavends;

import java.util.Arrays;
import java.util.Date;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.db.Inserter;
import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.NexusIndexParser;
import ch.usi.inf.mavends.index.NexusRecord;
import ch.usi.inf.mavends.util.Log;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class BuildMavenIndexDb {

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

		db.send("mavenindexdb.sql", "Maven Index SQL Schema");

		db.conn.setAutoCommit(false);

		Inserter pomins = db
				.createInserter("insert into pom (mdate, sha, gid, aid, ver, packaging, idate, size, is3, is4, is5, ext, gdesc, adesc, path) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		Inserter artins = db
				.createInserter("insert into art (mdate, sha, gid, aid, ver, packaging, idate, size, is3, is4, is5, ext, gdesc, adesc, path) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		Inserter secins = db
				.createInserter("insert into sec (mdate, sha, gid, aid, ver, classifier, packaging, idate, size, is3, is4, is5, ext, gdesc, adesc, path) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		Inserter allins = db
				.createInserter("insert into allgroups (value) values (?)");

		Inserter rootins = db
				.createInserter("insert into rootgroups (value) values (?)");

		Inserter propins = db
				.createInserter("insert into properties (descriptor, idxinfo, headb, creationdate) values (?, ?, ?, ?)");

		long ndoc = 0;
		long nallgroups = 0;
		long nrootgroups = 0;
		long ndesc = 0;
		long nart = 0;

		String descriptor = null;
		String idxinfo = null;

		try (NexusIndexParser nip = new NexusIndexParser(ar.nexusIndexPath)) {
			for (NexusRecord nr : nip) {
				ndoc++;

				MavenRecord mr = new MavenRecord(nr);

				if (mr.allGroups != null) {
					nallgroups++;

					log.info("Inserting allGroups...");

					for (String g : Arrays
							.asList(mr.allGroupsList.split("\\|"))) {
						allins.insert(g);
					}

				} else if (mr.rootGroups != null) {
					nrootgroups++;

					log.info("Inserting rootGroups...");

					for (String g : Arrays.asList(mr.rootGroupsList
							.split("\\|"))) {
						rootins.insert(g);
					}
				} else if (mr.descriptor != null) {
					ndesc++;

					descriptor = mr.descriptor;
					idxinfo = mr.idxinfo;

				} else if (mr.i != null) {
					if (mr.classifier == null) {
						nart += 2;

						pomins.insert(mr.mdate, mr.sha, mr.gid, mr.aid, mr.ver,
								mr.packaging, mr.idate, mr.size, mr.is3,
								mr.is4, mr.is5, mr.ext, mr.gdesc, mr.adesc,
								MavenRecord.getPath(mr.gid, mr.aid, mr.ver,
										null, "pom"));

						artins.insert(mr.mdate, mr.sha, mr.gid, mr.aid, mr.ver,
								mr.packaging, mr.idate, mr.size, mr.is3,
								mr.is4, mr.is5, mr.ext, mr.gdesc, mr.adesc,
								MavenRecord.getPath(mr.gid, mr.aid, mr.ver,
										null, mr.ext));
					} else {
						nart++;

						secins.insert(mr.mdate, mr.sha, mr.gid, mr.aid, mr.ver,
								mr.classifier, mr.packaging, mr.idate, mr.size,
								mr.is3, mr.is4, mr.is5, mr.ext, mr.gdesc,
								mr.adesc, MavenRecord.getPath(mr.gid, mr.aid,
										mr.ver, mr.classifier, mr.ext));
					}
				}

				if (ndoc % 100000 == 0) {
					log.info("docs: %,d", ndoc);
				}
			}

			log.info("Inserting properties...");
			propins.insert(descriptor, idxinfo, nip.headb, new Date(
					nip.headl / 1000));
		}

		log.info(
				"docs: %,d, allgroups: %,d, rootgroups: %,d, descriptor: %,d, artifacts: %,d",
				ndoc, nallgroups, nrootgroups, ndesc, nart);

		db.conn.commit();
	}
}
