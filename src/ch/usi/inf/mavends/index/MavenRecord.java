package ch.usi.inf.mavends.index;

import java.util.Arrays;

/**
 * Represents a Maven Record.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public final class MavenRecord implements NexusConstants {

	public long artCount = 0;
	public long delCount = 0;
	public long descriptorCount = 0;
	public long allGroupsCount = 0;
	public long rootGroupsCount = 0;

	/**
	 * It checks if all invariants for a MavenRecord hold from the NexusRecord.
	 * 
	 * @param nr
	 *            The NexusRecord taken from the Nexus Index.
	 */
	public void check(NexusRecord nr) {
		byte[] m = nr.get(M);
		byte[] u = nr.get(U);
		byte[] i = nr.get(I);
		byte[] del = nr.get(DEL);
		byte[] sha = nr.get(SHA);
		byte[] n = nr.get(N);
		byte[] d = nr.get(D);

		byte[] descriptor = nr.get(DESCRIPTOR);
		byte[] idxinfo = nr.get(IDXINFO);
		byte[] allGroups = nr.get(ALL_GROUPS);
		byte[] allGroupsList = nr.get(ALL_GROUPS_LIST);
		byte[] rootGroups = nr.get(ROOT_GROUPS);
		byte[] rootGroupsList = nr.get(ROOT_GROUPS_LIST);

		if (m != null) {
			checkDate(m);

			check(descriptor == null && idxinfo == null && allGroups == null && allGroupsList == null
					&& rootGroups == null && rootGroupsList == null, "Descriptor/all groups/root groups doc: %s");

			if (u != null) {
				artCount++;

				check(del == null && i != null, "u and i: %s", nr);

				byte[][] us = split(u, 5);

				check(us[3] != null, "Invalid value for u field: %s", nr);

				byte[] classifier = Arrays.equals(us[3], NA) ? null : us[3];

				check((us[4] == null) == isMain(classifier), "Expected NA/Main classifier");

				byte[][] is = split(i, 7);
				check(is.length == 7, "Invalid i: %s", nr);

				byte[] packaging = is[0];

				check(isMain(classifier) || Arrays.equals(us[4], packaging), "us4 and is0: %s", nr);

				checkDate(is[1]);
				long size = checkSignedLong(is[2]);
				check(size >= -1, "size-1: %s", nr);

				checkDigit(is[3]);
				checkDigit(is[4]);
				checkDigit(is[5]);

				byte[] extension = is[6];

				if (Arrays.equals(packaging, NULL)) {
					check(isMain(classifier) && size == -1 && Arrays.equals(extension, POM), "size-1=pom: %s", nr);
				}

				if (size == -1) {
					check(Arrays.equals(extension, POM), "size-1=pom: %s", nr);
				}

			} else if (del != null) {
				delCount++;

				check(u == null && i == null && sha == null && n == null && d == null, "u and m: %s", nr);

				byte[][] dels = split(del, 5);

				check(dels[3] != null, "Invalid value for del field: %s", nr);
			} else {
				check(false, "Invalid record type");
			}
		} else {
			check(u == null && i == null && del == null && sha == null && n == null && d == null,
					"u, i, del, sha, n, d / m: " + nr);

			check((allGroups == null) == (allGroupsList == null), "Invalid all groups doc: %s", nr);
			check((rootGroups == null) == (rootGroupsList == null), "Invalid root groups doc: %s", nr);
			check((descriptor == null) == (idxinfo == null), "Invalid description/idxinfo doc: %s", nr);

			if (descriptor != null) {
				descriptorCount++;

				check(allGroups == null && rootGroups == null, "%s", nr);
				check(Arrays.equals(descriptor, NEXUS_INDEX), "NexusIndex: %s", nr);
			} else if (allGroups != null) {
				allGroupsCount++;

				check(rootGroups == null && descriptor == null, "%s", nr);
				check(Arrays.equals(allGroups, ALL_GROUPS), "allGroups: %s", nr);
			} else if (rootGroups != null) {
				rootGroupsCount++;

				check(allGroups == null && descriptor == null, "null m: " + nr);
				check(Arrays.equals(rootGroups, ROOT_GROUPS), "rootGroups: %s", nr);
			} else {
				check(false, "Invalid record type");
			}
		}
	}

	public static byte[][] split(byte[] value, int length) {
		byte[][] res = new byte[length][];

		int prev = 0;
		int index = 0;
		for (int i = 0; i < value.length; i++) {
			if (value[i] == BAR) {
				res[index] = Arrays.copyOfRange(value, prev, i);
				index++;
				prev = i + 1;
			}
		}

		res[index] = Arrays.copyOfRange(value, prev, value.length);

		return res;
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
		classifier = isMain(classifier.getBytes()) ? "" : "-" + classifier;

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

	private static int checkDigit(byte[] s) {
		check(s.length == 1 && isDigit(s[0]), "Invalid digit: %s", s);
		return s[0] - '0';
	}

	private static long checkSignedLong(byte[] s) {
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

	private static void checkDate(byte[] s) {
		check(isDate(s), "Invalid date: %s", s);
	}

	private static void check(boolean cond, String message, Object... args) {
		if (!cond) {
			throw new RuntimeException(String.format(message, args));
		}
	}
}
