package ch.usi.inf.mavends.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Inserter;
import ch.usi.inf.mavends.util.log.Log;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Main {

	private static final String INSERT_HEADER = "insert into header (headb, creationdate) values (?, date(?/1000, 'unixepoch' ))";
	private static final String INSERT_ARTIFACT = "insert into artifact (groupid, artifactid, version, classifier, packaging, idate, size, is3, is4, is5, extension, mdate, sha1, artifactname, artifactdesc) values (?, ?, ?, ?, ?, date(?/1000, 'unixepoch' ), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_DEL = "insert into del (groupid, artifactid, version, classifier, packaging, mdate) values (?, ?, ?, ?, ?, date(?/1000, 'unixepoch' ))";
	private static final String INSERT_DESC = "insert into descriptor (descriptor, idxinfo) values (?, ?)";
	private static final String INSERT_ALL = "insert into allgroups (groupid) values (?)";
	private static final String INSERT_ROOT = "insert into rootgroups (groupid) values (?)";

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndex;

		@Arg(key = "mavenindex", name = "Output directory", desc = "Specifies the output path of the Maven Index DB file.")
		public String mavenIndex;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException,
			FileNotFoundException, IOException, ParseException, SQLException {
		final Args ar = ArgsParser.parse(args, new Args());

		try (final NexusIndex ni = new NexusIndex(ar.nexusIndex);
				final Db db = new Db(ar.mavenIndex);
				final Inserter artins = db.createInserter(INSERT_ARTIFACT);
				final Inserter delins = db.createInserter(INSERT_DEL)) {

			log.info("Inserting header...");

			try (final Inserter headerins = db.createInserter(INSERT_HEADER)) {
				headerins.insert(ni.headb, ni.creationDate);
			}

			while (ni.hasNext()) {
				NexusRecord nr = ni.next();
				MavenRecord mr = new MavenRecord(nr);

				if (mr.u != null) {
					artins.insert(mr.groupid, mr.artifactid, mr.version, mr.classifier, mr.packaging, mr.idate,
							mr.size, mr.is3, mr.is4, mr.is5, mr.extension, mr.mdate, mr.sha1, mr.artifactName,
							mr.artifactDesc);
				} else if (mr.del != null) {
					delins.insert(mr.groupid, mr.artifactid, mr.version, mr.classifier, mr.packaging, mr.mdate);
				} else if (mr.descriptor != null) {
					try (final Inserter descins = db.createInserter(INSERT_DESC)) {
						descins.insert(mr.descriptor, mr.idxinfo);
					}
				} else if (mr.allGroupsList != null) {
					try (final Inserter allins = db.createInserter(INSERT_ALL)) {
						for (String groupid : mr.allGroupsList) {
							allins.insert(groupid);
						}
					}
				} else if (mr.rootGroupsList != null) {
					try (final Inserter rootins = db.createInserter(INSERT_ROOT)) {
						for (String groupid : mr.rootGroupsList) {
							rootins.insert(groupid);
						}
					}
				}
			}

			db.commit();
		}
	}
}
