package ch.usi.inf.mavends.index;

import java.util.Date;

/**
 * Represents a Maven Record.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class MavenRecord implements NexusConstants {

	/**
	 * Constructs a MavenRecord from a NexusRecord. It checks if all the
	 * invariants for a MavenRecord hold.
	 * 
	 * @param nr
	 *            The NexusRecord taken from the Nexus Index.
	 */
	public static void check(NexusRecord nr) {
		byte[] allGroups = nr.get(ALL_GROUPS);
		byte[] allGroupsList = nr.get(ALL_GROUPS_LIST);
		byte[] rootGroups = nr.get(ROOT_GROUPS);
		byte[] rootGroupsList = nr.get(ROOT_GROUPS_LIST);
		byte[] descriptor = nr.get(DESCRIPTOR);
		byte[] idxinfo = nr.get(IDXINFO);
		byte[] sha = nr.get(SHA);
		byte[] m = nr.get(M);
		byte[] u = nr.get(U);
		byte[] i = nr.get(I);
		byte[] del = nr.get(DEL);
		byte[] artifactname = nr.get(N);
		byte[] artifactdesc = nr.get(D);

		check((allGroups == null) == (allGroupsList == null), "Invalid all groups doc: " + nr);

		check((rootGroups == null) == (rootGroupsList == null), "Invalid root groups doc: " + nr);

		check((descriptor == null) == (idxinfo == null), "Invalid description/idxinfo doc: " + nr);

		check(((allGroups == null) && (rootGroups == null) && (descriptor == null)) == (m != null), "null m: " + nr);

		check((u == null) || (m != null), "u and m: " + nr);
		check((i == null) || (m != null), "i and m: " + nr);
		check((del == null) || (m != null), "del and m: " + nr);
		check((sha == null) || (m != null), "one and m: " + nr);

		check((u != null) == (i != null), "u and i: " + nr);
		check((sha == null) || (u != null), "u and sha: " + nr);
		check((u == null) || (del == null), "u and del: " + nr);

		check((artifactname == null) || (i != null), "n and i: " + nr);
		check((artifactdesc == null) || (i != null), "n and i: " + nr);

		if (allGroups != null) {
			check(new String(allGroups).equals("allGroups"), "allGroups: %s", nr);
		}

		if (rootGroups != null) {
			check(new String(rootGroups).equals("rootGroups"), "rootGroups: %s", nr);
		}

		if (descriptor != null) {
			check(new String(descriptor).equals("NexusIndex"), "NexusIndex: %s", nr);
		}

		if (m != null) {
			checkDate(new String(m));
		}

		if (i != null) {
			String[] us = new String(u).split("\\|");

			check(us.length == 4 || us.length == 5, "Invalid value for u field: %s", nr);

			String classifier = "NA".equals(us[3]) ? null : us[3];

			check(us.length != 4 || isMain(classifier), "Expected NA/Main classifier");

			String[] is = new String(i).split("\\|");
			check(is.length == 7, "Invalid i: %s", nr);

			String packaging = is[0];

			check(us.length == 4 || us[4].equals(packaging), "us4 and is0: %s", nr);

			checkDate(is[1]);
			long size = checkSignedLong(is[2]);
			check(size >= -1, "Size more negative: %s", nr);

			checkDigit(is[3]);
			checkDigit(is[4]);
			checkDigit(is[5]);

			String extension = is[6];

			check(!packaging.equals("null") || (size == -1 && extension.equals("pom")), "size/no jar and null: %s", nr);
		} else if (del != null) {
			String[] dels = new String(del).split("\\|");

			check(dels.length == 4 || dels.length == 5, "Invalid value for del field: %s", nr);
		}
	}

	/**
	 * Gets the path of a given artifact.
	 * 
	 * @param gid
	 *            The group id
	 * @param aid
	 *            The artifact id
	 * @param ver
	 *            The version of the artifact
	 * @param classifier
	 *            The classifier, if any. Null, empty string or "NA" to request
	 *            the main artifact.
	 * @param ext
	 *            The extension to be requested.
	 * @return The relative path of this artifact
	 */
	public static String getPath(String gid, String aid, String ver, String classifier, String ext) {
		classifier = isMain(classifier) ? "" : "-" + classifier;

		return gid.replace('.', '/') + "/" + aid + "/" + ver + "/" + aid + "-" + ver + classifier + "." + ext;
	}

	private static boolean isMain(String classifier) {
		return classifier == null;
	}

	private static int checkDigit(String s) {
		check(s.matches("[0-9]"), "Invalid digit: %s", s);
		return Integer.parseInt(s);
	}

	private static long checkSignedLong(String s) {
		check(s.matches("-?[0-9]+"), "Invalid signed long: %s", s);
		return Long.parseLong(s);
	}

	private static Date checkDate(String s) {
		check(s.matches("[0-9]{13}"), "Invalid date: %s", s);
		return new Date(Long.parseLong(s) / 1000);
	}

	private static void check(boolean cond, String message, Object... args) {
		if (!cond) {
			throw new RuntimeException(String.format(message, args));
		}
	}
}
