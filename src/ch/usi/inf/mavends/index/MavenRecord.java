package ch.usi.inf.mavends.index;

import java.text.ParseException;
import java.util.Arrays;

/**
 * Represents a Maven Record.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public final class MavenRecord extends NexusConstants {

	private static final byte[] ALL_GROUPS = "allGroups".getBytes();
	private static final byte[] ALL_GROUPS_LIST = "allGroupsList".getBytes();
	private static final byte[] ROOT_GROUPS = "rootGroups".getBytes();
	private static final byte[] ROOT_GROUPS_LIST = "rootGroupsList".getBytes();
	private static final byte[] DESCRIPTOR = "DESCRIPTOR".getBytes();
	private static final byte[] IDXINFO = "IDXINFO".getBytes();
	private static final byte[] NEXUS_INDEX = "NexusIndex".getBytes();
	private static final byte[] SHA = "1".getBytes();
	private static final byte[] M = "m".getBytes();
	private static final byte[] U = "u".getBytes();
	private static final byte[] I = "i".getBytes();
	private static final byte[] DEL = "del".getBytes();
	private static final byte[] N = "n".getBytes();
	private static final byte[] D = "d".getBytes();
	private static final byte[] NA = "NA".getBytes();

	private static final byte[] NULL = "null".getBytes();
	private static final byte[] POM = "pom".getBytes();

	public static long artCount = 0;
	public static long delCount = 0;
	public static long descriptorCount = 0;
	public static long allGroupsCount = 0;
	public static long rootGroupsCount = 0;

	public final byte[] m;
	public final byte[] u;
	public final byte[] i;
	public final byte[] del;
	public final byte[] sha;
	public final byte[] n;
	public final byte[] d;

	public final byte[] descriptor;
	public final byte[] idxinfo;
	public final byte[] allGroups;
	public final byte[] allGroupsList;
	public final byte[] rootGroups;
	public final byte[] rootGroupsList;

	public final byte[] groupid;
	public final byte[] artifactid;
	public final byte[] version;
	public final byte[] classifier;
	public final byte[] packaging;

	public final byte[] idate;
	public final byte[] size;
	public final byte[] is3;
	public final byte[] is4;
	public final byte[] is5;
	public final byte[] extension;

	/**
	 * It checks if all invariants for a MavenRecord hold from the NexusRecord.
	 * 
	 * @param nr
	 *            The NexusRecord taken from the Nexus Index.
	 * @throws ParseException
	 */
	public MavenRecord(NexusRecord nr) throws ParseException {
		m = nr.get(M);
		u = nr.get(U);
		i = nr.get(I);
		del = nr.get(DEL);
		sha = nr.get(SHA);
		n = nr.get(N);
		d = nr.get(D);

		descriptor = nr.get(DESCRIPTOR);
		idxinfo = nr.get(IDXINFO);
		allGroups = nr.get(ALL_GROUPS);
		allGroupsList = nr.get(ALL_GROUPS_LIST);
		rootGroups = nr.get(ROOT_GROUPS);
		rootGroupsList = nr.get(ROOT_GROUPS_LIST);

		if (m != null) {
			checkDate(m);

			check(descriptor == null && idxinfo == null && allGroups == null && allGroupsList == null
					&& rootGroups == null && rootGroupsList == null, nr, "Descriptor/all groups/root groups doc");

			if (u != null) {
				artCount++;

				check(del == null && i != null, nr, "u and i");

				byte[][] us = split(u, 5);

				check(us[3] != null, nr, "Invalid value for u field");

				groupid = us[0];
				artifactid = us[1];
				version = us[2];
				classifier = Arrays.equals(us[3], NA) ? null : us[3];

				check((us[4] == null) == isMain(classifier), nr, "Expected NA/Main classifier");

				byte[][] is = split(i, 7);
				check(is.length == 7, nr, "Invalid i");

				packaging = is[0];
				idate = is[1];
				size = is[2];
				is3 = is[3];
				is4 = is[4];
				is5 = is[5];
				extension = is[6];

				check(isMain(classifier) || Arrays.equals(us[4], packaging), nr, "us4 and is0");

				checkDate(is[1]);
				long lsize = checkSignedLong(size);
				check(lsize >= -1, nr, "size-1");

				checkDigit(is[3]);
				checkDigit(is[4]);
				checkDigit(is[5]);

				byte[] extension = is[6];

				if (Arrays.equals(packaging, NULL)) {
					check(isMain(classifier) && lsize == -1 && Arrays.equals(extension, POM), nr, "size-1=pom");
				}

				if (lsize == -1) {
					check(Arrays.equals(extension, POM), nr, "size-1=pom");
				}
			} else if (del != null) {
				delCount++;

				check(u == null && i == null && sha == null && n == null && d == null, nr, "u and m");

				byte[][] dels = split(del, 5);

				check(dels[3] != null, nr, "Invalid value for del field: %s");

				groupid = dels[0];
				artifactid = dels[1];
				version = dels[2];
				classifier = Arrays.equals(dels[3], NA) ? new byte[0] : dels[3];
				packaging = dels[4] == null ? null : dels[4];

				idate = null;
				size = null;
				is3 = null;
				is4 = null;
				is5 = null;
				extension = null;
			} else {
				throw new ParseException("Invalid record type: " + nr, 0);
			}
		} else {
			check(u == null && i == null && del == null && sha == null && n == null && d == null, nr,
					"u, i, del, sha, n, d / m");

			check((allGroups == null) == (allGroupsList == null), nr, "Invalid all groups doc");
			check((rootGroups == null) == (rootGroupsList == null), nr, "Invalid root groups doc");
			check((descriptor == null) == (idxinfo == null), nr, "Invalid description/idxinfo doc");

			if (descriptor != null) {
				descriptorCount++;

				check(allGroups == null && rootGroups == null, nr, "all/root");
				check(Arrays.equals(descriptor, NEXUS_INDEX), nr, "NexusIndex");
			} else if (allGroups != null) {
				allGroupsCount++;

				check(rootGroups == null && descriptor == null, nr, "root/desc");
				check(Arrays.equals(allGroups, ALL_GROUPS), nr, "allGroups");
			} else if (rootGroups != null) {
				rootGroupsCount++;

				check(allGroups == null && descriptor == null, nr, "null m");
				check(Arrays.equals(rootGroups, ROOT_GROUPS), nr, "rootGroups");
			} else {
				check(false, "Invalid record type");
			}

			groupid = null;
			artifactid = null;
			version = null;
			classifier = null;
			packaging = null;

			idate = null;
			size = null;
			is3 = null;
			is4 = null;
			is5 = null;
			extension = null;
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
		classifier = classifier == null ? "" : "-" + classifier;

		return gid.replace('.', '/') + "/" + aid + "/" + ver + "/" + aid + "-" + ver + classifier + "." + ext;
	}

	private static boolean isMain(byte[] classifier) {
		return classifier == null;
	}

	private static boolean isDigit(byte d) {
		return d >= '0' || d <= '9';
	}

	private static boolean isDate(byte[] s) {
		if (s.length != 13) {
			return false;
		}

		for (int i = 0; i < 13; i++) {
			if (!isDigit(s[i]))
				return false;
		}

		return true;
	}

	private static int checkDigit(byte[] s) throws ParseException {
		check(s.length == 1 && isDigit(s[0]), "Invalid digit: %s", s);
		return s[0] - '0';
	}

	private static long checkSignedLong(byte[] s) throws ParseException {
		long res = 0;
		boolean neg = false;
		int i = 0;
		int len = s.length;
		if (s[0] == '-') {
			neg = true;
			i++;
		}

		while (i < len) {
			byte d = s[i++];

			if (!isDigit(d)) {
				check(false, "Not a digit: %s", new String(s));
			}

			int digit = d - '0';
			res *= 10;
			res -= digit;
			i++;
		}

		return neg ? res : -res;
	}

	private static void checkDate(byte[] s) throws ParseException {
		check(isDate(s), "Invalid date: %s", s);
	}

	private static void check(boolean cond, String message, Object... args) throws ParseException {
		if (!cond) {
			throw new ParseException(String.format(message, args), 0);
		}
	}

	private static void check(boolean cond, NexusRecord nr, String message) throws ParseException {
		if (!cond) {
			throw new ParseException(message + ": " + nr, 0);
		}
	}
}
