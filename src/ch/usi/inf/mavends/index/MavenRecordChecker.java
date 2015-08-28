package ch.usi.inf.mavends.index;

import java.util.Date;

/**
 * Represents a Maven Record.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class MavenRecordChecker {

	public final String allGroups;
	public final String allGroupsList;
	public final String rootGroups;
	public final String rootGroupsList;
	public final String descriptor;
	public final String idxinfo;
	public final String sha;
	public final String m;
	public final String u;
	public final String i;
	public final String del;
	public final String artifactname;
	public final String artifactdesc;
	public final Date mdate;
	public final String groupid;
	public final String artifactid;
	public final String version;
	public final String classifier;
	public final String packaging;
	public final Date idate;
	public final long size;
	public final int is3;
	public final int is4;
	public final int is5;
	public final String extension;

	/**
	 * 
	 * @param doc
	 */
	public MavenRecordChecker(NexusRecord doc) {
		allGroups = doc.get("allGroups");
		allGroupsList = doc.get("allGroupsList");
		rootGroups = doc.get("rootGroups");
		rootGroupsList = doc.get("rootGroupsList");
		descriptor = doc.get("DESCRIPTOR");
		idxinfo = doc.get("IDXINFO");
		sha = doc.get("1");
		m = doc.get("m");
		u = doc.get("u");
		i = doc.get("i");
		del = doc.get("del");
		artifactname = doc.get("n");
		artifactdesc = doc.get("d");

		check((allGroups == null) == (allGroupsList == null),
				"Invalid all groups doc: " + doc);

		check((rootGroups == null) == (rootGroupsList == null),
				"Invalid root groups doc: " + doc);

		check((descriptor == null) == (idxinfo == null),
				"Invalid description/idxinfo doc: " + doc);

		check(((allGroups == null) && (rootGroups == null) && (descriptor == null)) == (m != null),
				"null m: " + doc);

		check((u == null) || (m != null), "u and m: " + doc);
		check((i == null) || (m != null), "i and m: " + doc);
		check((u != null) == (i != null), "u and i: " + doc);

		check((del == null) || (m != null), "del and m: " + doc);
		check((sha == null) || (m != null), "one and m: " + doc);

		if (allGroups != null) {
			check(allGroups.equals("allGroups"), "allGroups: %s", doc);
		}

		if (rootGroups != null) {
			check(rootGroups.equals("rootGroups"), "rootGroups: %s", doc);
		}

		if (descriptor != null) {
			check(descriptor.equals("NexusIndex"), "NexusIndex: %s", doc);
		}

		mdate = m != null ? checkDate(m) : null;

		if (i != null) {
			String[] us = u.split("\\|");

			check(us.length == 4 || us.length == 5,
					"Invalid value for u field: %s", doc);

			groupid = us[0];
			artifactid = us[1];
			version = us[2];
			classifier = "NA".equals(us[3]) ? null : us[3];

			check(us.length != 4 || isMain(classifier),
					"Expected NA/Main classifier");

			String[] is = i.split("\\|");
			check(is.length == 7, "Invalid i: %s", doc);

			packaging = is[0];

			check(us.length == 4 || us[4].equals(packaging), "us4 and is0: %s",
					doc);

			idate = checkDate(is[1]);
			size = checkSignedLong(is[2]);
			check(size >= -1, "Size more negative: %s", doc);

			is3 = checkDigit(is[3]);
			is4 = checkDigit(is[4]);
			is5 = checkDigit(is[5]);

			extension = is[6];

			check(!packaging.equals("null")
					|| (size == -1 && extension.equals("pom")),
					"size/no jar and null: %s", doc);
		} else {
			groupid = null;
			artifactid = null;
			version = null;
			classifier = null;
			packaging = null;
			idate = null;
			size = 0;
			is3 = 0;
			is4 = 0;
			is5 = 0;
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
	public static String getPath(String gid, String aid, String ver,
			String classifier, String ext) {
		classifier = isMain(classifier) ? "" : "-" + classifier;

		return gid.replace('.', '/') + "/" + aid + "/" + ver + "/" + aid + "-"
				+ ver + classifier + "." + ext;
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
