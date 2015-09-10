package ch.usi.inf.mavends;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

	private static ArrayList<byte[]> split(byte[] value) {
		ArrayList<byte[]> res = new ArrayList<byte[]>(128);

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

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException,
			IOException {
		Args ar = ArgsParser.parse(args, new Args());

		try (FileOutputStream ufos = new FileOutputStream(ar.out + "/nexus-us.csv");
				FileOutputStream delfos = new FileOutputStream(ar.out + "/nexus-dels.csv");
				FileOutputStream descfos = new FileOutputStream(ar.out + "/nexus-desc.csv");
				FileOutputStream allfos = new FileOutputStream(ar.out + "/nexus-all.csv");
				FileOutputStream rootfos = new FileOutputStream(ar.out + "/nexus-root.csv");
				BufferedOutputStream uos = new BufferedOutputStream(ufos, BUFFER_SIZE);
				BufferedOutputStream delos = new BufferedOutputStream(delfos, BUFFER_SIZE);
				BufferedOutputStream descos = new BufferedOutputStream(descfos, BUFFER_SIZE);
				BufferedOutputStream allos = new BufferedOutputStream(allfos, BUFFER_SIZE);
				BufferedOutputStream rootos = new BufferedOutputStream(rootfos, BUFFER_SIZE);
				NexusIndex ni = new NexusIndex(ar.nexusIndex)) {

			try (FileOutputStream hfos = new FileOutputStream(ar.out + "/nexus-header.csv");
					BufferedOutputStream hos = new BufferedOutputStream(hfos, BUFFER_SIZE)) {
				log.info("Header byte: %d", ni.headb);
				log.info("Creation Date: %s", ni.creationDate);

				hos.write((ni.headb + "").getBytes());
				hos.write(BAR);
				hos.write(ni.creationDate.toString().getBytes());
				hos.write(CRLF);
			}

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
					uos.write(is[3]);
					uos.write(BAR);
					uos.write(is[4]);
					uos.write(BAR);
					uos.write(is[5]);
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
					byte[] idxinfo = nr.get(IDXINFO);

					descos.write(descriptor);
					descos.write(BAR);
					descos.write(idxinfo);
					descos.write(CRLF);
				} else if (nr.get(ALL_GROUPS) != null) {

					byte[] allGroupsList = nr.get(ALL_GROUPS_LIST);

					for (byte[] groupid : split(allGroupsList)) {
						allos.write(groupid);
						allos.write(CRLF);
					}
				} else if (nr.get(ROOT_GROUPS) != null) {
					byte[] rootGroupsList = nr.get(ROOT_GROUPS_LIST);

					for (byte[] groupid : split(rootGroupsList)) {
						rootos.write(groupid);
						rootos.write(CRLF);
					}
				}

			}

			log.info("Number of Records: %,d", ni.recordCount);
		}
	}
}
