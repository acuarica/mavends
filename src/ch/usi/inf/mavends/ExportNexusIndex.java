package ch.usi.inf.mavends;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
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

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class ExportNexusIndex implements NexusConstants {

	private static final int BUFFER_SIZE = 1024 * 16;
	private static final byte[] CRLF = "\n".getBytes();

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndex;

		@Arg(key = "out", name = "Output directory", desc = "Specifies the output path of the Maven Index DB file.")
		public String out;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException,
			IOException {
		Args ar = ArgsParser.parse(args, new Args());

		try (FileOutputStream ufos = new FileOutputStream(ar.out + "/nexus-us.csv");
				BufferedOutputStream uos = new BufferedOutputStream(ufos, BUFFER_SIZE);
				FileOutputStream delfos = new FileOutputStream(ar.out + "/nexus-dels.csv");
				BufferedOutputStream delos = new BufferedOutputStream(delfos, BUFFER_SIZE);
				NexusIndex ni = new NexusIndex(ar.nexusIndex)) {

			while (ni.hasNext()) {
				NexusRecord nr = ni.next();

				byte[] u;
				byte[] del;
				byte[] descriptor;

				if ((u = nr.get(U)) != null) {
					byte[] mdate = nr.get(M);
					byte[] i = nr.get(I);

					byte[][] us = MavenRecord.split(u, 5);

					byte[] groupid = us[0];
					byte[] artifactid = us[1];
					byte[] version = us[2];
					byte[] classifier = Arrays.equals(us[3], NA) ? new byte[0] : us[3];

					byte[][] is = MavenRecord.split(i, 7);

					byte[] packaging = is[0];
					byte[] idate = is[1];
					byte[] size = is[2];
					byte[] extension = is[6];

					uos.write(groupid);
					uos.write(BAR);
					uos.write(artifactid);
					uos.write(BAR);
					uos.write(version);
					uos.write(BAR);
					uos.write(classifier);
					uos.write(BAR);
					uos.write(packaging);
					uos.write(BAR);
					uos.write(idate);
					uos.write(BAR);
					uos.write(size);
					uos.write(BAR);
					uos.write(extension);
					uos.write(BAR);
					uos.write(mdate);
					uos.write(CRLF);
				} else if ((del = nr.get(DEL)) != null) {
					byte[] mdate = nr.get(M);

					byte[][] dels = MavenRecord.split(del, 5);

					byte[] groupid = dels[0];
					byte[] artifactid = dels[1];
					byte[] version = dels[2];
					byte[] classifier = Arrays.equals(dels[3], NA) ? new byte[0] : dels[3];
					byte[] packaging = dels[4] == null ? new byte[0] : dels[4];

					delos.write(groupid);
					delos.write(BAR);
					delos.write(artifactid);
					delos.write(BAR);
					delos.write(version);
					delos.write(BAR);
					delos.write(classifier);
					delos.write(BAR);
					delos.write(packaging);
					delos.write(BAR);
					delos.write(mdate);
					delos.write(CRLF);
				} else if ((descriptor = nr.get(DESCRIPTOR)) != null) {

				} else if (nr.get(ALL_GROUPS) != null) {
				} else if (nr.get(ROOT_GROUPS) != null) {
				}

			}

			log.info("Number of Records: %,d", ni.recordCount);
		}
	}
}
