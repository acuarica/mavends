package ch.usi.inf.mavends.index;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;

import ch.usi.inf.mavends.log.Log;

public class MavenIndexBuilder {

	private static final Log log = new Log(System.out);

	public static void build(String indexPath, Connection c) throws Exception {
		Inserter artins = new Inserter(
				c,
				"insert into artifact (mdate, sha, gid, aid, ver, sat, is0, idate, size, is3, is4, is5, ext, gdesc, adesc, path, inrepo) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		Inserter allins = new Inserter(c,
				"insert into allgroups (value) values (?)");

		Inserter rootins = new Inserter(c,
				"insert into rootgroups (value) values (?)");

		Inserter propins = new Inserter(
				c,
				"insert into properties (descriptor, idxinfo, headb, creationdate) values (?, ?, ?, ?)");

		long ndoc = 0;
		long nallgroups = 0;
		long nrootgroups = 0;
		long ndesc = 0;
		long nart = 0;

		String descriptor = null;
		String idxinfo = null;

		try (final NexusIndexParser nip = new NexusIndexParser(indexPath)) {
			for (final NexusRecord nr : nip) {
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
					nart++;

					artins.insert(mr.mdate, mr.sha, mr.gid, mr.aid, mr.ver,
							mr.plugin, mr.packaging, mr.idate, mr.size, mr.is3,
							mr.is4, mr.is5, mr.ext, mr.gdesc, mr.adesc,
							getPath(mr.gid, mr.aid, mr.ver, mr.plugin, mr.ext),
							false);

					if (mr.plugin == null) {
						nart++;

						artins.insert(mr.mdate, mr.sha, mr.gid, mr.aid, mr.ver,
								null, "*pom*", mr.idate, -2, mr.is3, mr.is4,
								mr.is5, "pom", mr.gdesc, mr.adesc,
								getPath(mr.gid, mr.aid, mr.ver, "", "pom"),
								false);
					}
				}

				if (ndoc % 100000 == 0) {
					System.out.printf("docs: %,d\n", ndoc);
				}
			}

			log.info("Inserting properties...");
			propins.insert(descriptor, idxinfo, nip.headb, new Date(
					nip.headl / 1000));
		}

		System.out.println();
		System.out
				.printf("docs: %,d, allgroups: %,d, rootgroups: %,d, descriptor: %,d, artifacts: %,d",
						ndoc, nallgroups, nrootgroups, ndesc, nart);
		System.out.println();
	}

	private static String getPath(String gid, String aid, String ver,
			String plugin, String ext) {
		plugin = plugin == null || "".equals(plugin) ? "" : "-" + plugin;

		return gid.replace('.', '/') + "/" + aid + "/" + ver + "/" + aid + "-"
				+ ver + plugin + "." + ext;
	}
}
