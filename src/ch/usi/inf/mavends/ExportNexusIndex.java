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

	private static final byte[] CRLF = "\n".getBytes();

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "nexusindex", name = "Nexus Index", desc = "Specifies the input path of the Nexus Index file.")
		public String nexusIndex;

		@Arg(key = "artscsv", name = "Artifacts CSV file", desc = "Specifies the output path of the Maven Index DB file.")
		public String artsCsv;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException,
			IOException {
		Args ar = ArgsParser.parse(args, new Args());

		try (FileOutputStream fos = new FileOutputStream(ar.artsCsv);
				BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 16);
				NexusIndex ni = new NexusIndex(ar.nexusIndex)) {

			while (ni.hasNext()) {
				NexusRecord nr = ni.next();

				byte[] u;

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

					bos.write(groupid);
					bos.write(BAR);
					bos.write(artifactid);
					bos.write(BAR);
					bos.write(version);
					bos.write(BAR);
					bos.write(classifier);
					bos.write(BAR);
					bos.write(packaging);
					bos.write(BAR);
					bos.write(idate);
					bos.write(BAR);
					bos.write(size);
					bos.write(BAR);
					bos.write(extension);
					bos.write(BAR);
					bos.write(mdate);
					bos.write(CRLF);
				}
			}

			log.info("Number of Records: %,d", ni.recordCount);
		}
	}
}
