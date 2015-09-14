package ch.usi.inf.mavends;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.index.NexusConstants;
import ch.usi.inf.mavends.index.NexusIndex;
import ch.usi.inf.mavends.index.NexusRecord;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class ExportNexusIndex extends NexusConstants {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndex;

		@Arg(key = "out", name = "Output directory", desc = "Specifies the output path of the Maven Index DB file.")
		public String out;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException,
			FileNotFoundException, IOException, ParseException {
		Args ar = ArgsParser.parse(args, new Args());

		try (BufferedOutputStream uos = new BufferedOutputStream(new FileOutputStream(ar.out + "/nexus-us.csv"),
				BUFFER_SIZE);
				BufferedOutputStream delos = new BufferedOutputStream(new FileOutputStream(ar.out + "/nexus-dels.csv"),
						BUFFER_SIZE);
				BufferedOutputStream descos = new BufferedOutputStream(
						new FileOutputStream(ar.out + "/nexus-desc.csv"), BUFFER_SIZE);
				BufferedOutputStream allos = new BufferedOutputStream(new FileOutputStream(ar.out + "/nexus-all.csv"),
						BUFFER_SIZE);
				BufferedOutputStream rootos = new BufferedOutputStream(
						new FileOutputStream(ar.out + "/nexus-root.csv"), BUFFER_SIZE);
				NexusIndex ni = new NexusIndex(ar.nexusIndex)) {

			try (FileOutputStream hfos = new FileOutputStream(ar.out + "/nexus-header.csv");
					BufferedOutputStream hos = new BufferedOutputStream(hfos, BUFFER_SIZE)) {
				log.info("Header byte: %d", ni.headb);
				log.info("Creation Date: %s", ni.creationDate);

				write(hos, (ni.headb + "").getBytes(), ni.creationDate.toString().getBytes());
			}

			while (ni.hasNext()) {
				NexusRecord nr = ni.next();
				MavenRecord mr = new MavenRecord(nr);

				if (mr.u != null) {
					write(uos, mr.groupid, mr.artifactid, mr.version, mr.classifier == null ? new byte[0]
							: mr.classifier, mr.packaging, mr.idate, mr.size, mr.is3, mr.is4, mr.is5, mr.extension,
							mr.m);
				} else if (mr.del != null) {
					write(delos, mr.groupid, mr.artifactid, mr.version, mr.classifier == null ? new byte[0]
							: mr.classifier, mr.packaging == null ? new byte[0] : mr.packaging, mr.m);
				} else if (mr.descriptor != null) {
					write(descos, mr.descriptor, mr.idxinfo);
				} else if (mr.allGroups != null) {
					for (byte[] groupid : split(mr.allGroupsList)) {
						write(allos, groupid);
					}
				} else if (mr.rootGroups != null) {
					for (byte[] groupid : split(mr.rootGroupsList)) {
						write(rootos, groupid);
					}
				}
			}

			log.info("Number of Records: %,d", ni.recordCount);
			log.info("Number of Artifacts: %,d", MavenRecord.artCount);
			log.info("Number of Deleted Artifacts: %,d", MavenRecord.delCount);
			log.info("Number of DESCRIPTOR: %,d", MavenRecord.descriptorCount);
			log.info("Number of allGroups: %,d", MavenRecord.allGroupsCount);
			log.info("Number of rootGroups: %,d", MavenRecord.rootGroupsCount);
		}
	}
}
