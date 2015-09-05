package ch.usi.inf.mavends;

import java.util.Arrays;
import java.util.Date;

import ch.usi.inf.mavends.args.Arg;
import ch.usi.inf.mavends.args.ArgsParser;
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
public class PopulateMavenIndex {

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

		db.conn.setAutoCommit(false);

		Inserter artins = db
				.createInserter("insert into artifact (mdate, sha, groupid, artifactid, version, classifier, packaging, idate, size, is3, is4, is5, extension, artifactname, artifactdesc) values (date(?, 'unixepoch' ), ?, ?, ?, ?, ?, ?, date(?, 'unixepoch' ), ?, ?, ?, ?, ?, ?, ?)");

		Inserter delins = db
				.createInserter("insert into del (groupid, artifactid, version, classifier, packaging, mdate) values (?, ?, ?, ?, ?, date(?, 'unixepoch'))");

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

				MavenRecord mr = new MavenRecord(nr);

				if (mr.i != null) {
					artins.insert(mr.mdate, mr.sha, mr.groupid, mr.artifactid,
							mr.version, mr.classifier, mr.packaging, mr.idate,
							mr.size, mr.is3, mr.is4, mr.is5, mr.extension,
							mr.artifactname, mr.artifactdesc);
				} else if (mr.del != null) {
					delins.insert(mr.groupid, mr.artifactid, mr.version,
							mr.classifier, mr.packaging, mr.mdate);
				} else if (mr.allGroups != null) {
					log.info("Inserting allGroups...");

					for (String g : Arrays
							.asList(mr.allGroupsList.split("\\|"))) {
						allins.insert(g);
					}
				} else if (mr.rootGroups != null) {

					log.info("Inserting rootGroups...");

					for (String g : Arrays.asList(mr.rootGroupsList
							.split("\\|"))) {
						rootins.insert(g);
					}
				} else if (mr.descriptor != null) {
					log.info("Inserting descriptor...");

					descins.insert(mr.descriptor, mr.idxinfo);
				}
			}
		}

		log.info("ndocs: %,d", ndocs);

		db.conn.commit();
	}
}
