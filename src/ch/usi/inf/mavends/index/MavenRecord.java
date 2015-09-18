package ch.usi.inf.mavends.index;

import java.text.ParseException;
import java.util.Date;

/**
 * Represents a Maven Record.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
final class MavenRecord {

	final Date mdate;
	final String u;
	final String i;
	final String del;
	final String sha1;
	final String artifactName;
	final String artifactDesc;

	final String descriptor;
	final String idxinfo;
	final String[] allGroupsList;
	final String[] rootGroupsList;

	final String groupid;
	final String artifactid;
	final String version;
	final String classifier;
	final String packaging;

	final Date idate;
	final long size;
	final int is3;
	final int is4;
	final int is5;
	final String extension;

	/**
	 * It checks if all invariants for a MavenRecord hold from the NexusRecord.
	 * 
	 * @param nr
	 *            The NexusRecord taken from the Nexus Index.
	 * @throws ParseException
	 */
	MavenRecord(NexusRecord nr) throws ParseException {
		String m = nr.get("m");

		i = nr.get("i");
		u = nr.get("u");
		del = nr.get("del");
		sha1 = nr.get("1");
		artifactName = nr.get("n");
		artifactDesc = nr.get("d");
		descriptor = nr.get("DESCRIPTOR");
		idxinfo = nr.get("IDXINFO");

		String allGroups = nr.get("allGroups");
		String allGroupsList = nr.get("allGroupsList");
		String rootGroups = nr.get("rootGroups");
		String rootGroupsList = nr.get("rootGroupsList");

		if (m != null) {
			mdate = checkDate(m, nr);

			check(descriptor == null && idxinfo == null && allGroups == null && allGroupsList == null
					&& rootGroups == null && rootGroupsList == null, nr, "Descriptor/all groups/root groups doc");

			if (u != null) {
				check(del == null && i != null, nr, "u and i");

				String[] us = u.split("\\|");

				check(us.length == 4 || us.length == 5, nr, "Invalid value for u field");

				groupid = us[0];
				artifactid = us[1];
				version = us[2];
				classifier = "NA".equals(us[3]) ? null : us[3];

				check((us.length == 4) == isMain(classifier), nr, "Expected NA/Main classifier");

				String[] is = i.split("\\|");
				check(is.length == 7, nr, "Invalid i");

				packaging = is[0];
				idate = checkDate(is[1], nr);
				size = checkSignedLong(is[2], nr);
				is3 = checkDigit(is[3], nr);
				is4 = checkDigit(is[4], nr);
				is5 = checkDigit(is[5], nr);
				extension = is[6];

				check(isMain(classifier) || us[4].equals(packaging), nr, "us4 and is0");

				check(size >= -1, nr, "size-1");

				String extension = is[6];

				if (packaging.equals("null")) {
					check(isMain(classifier) && size == -1 && "pom".equals(extension), nr, "size-1=pom");
				}

				if (size == -1) {
					check("pom".equals(extension), nr, "size-1=pom");
				}
			} else if (del != null) {
				check(u == null && i == null && sha1 == null && artifactName == null && artifactDesc == null, nr,
						"u and m");

				String[] dels = del.split("\\|");

				check(dels.length == 4 || dels.length == 5, nr, "Invalid value for del field: %s");

				groupid = dels[0];
				artifactid = dels[1];
				version = dels[2];
				classifier = dels[3].equals("NA") ? null : dels[3];
				packaging = dels.length == 4 ? null : dels[4];

				idate = null;
				size = -1;
				is3 = 0;
				is4 = 0;
				is5 = 0;
				extension = null;
			} else {
				throw new ParseException("Invalid record type: " + nr, 0);
			}

			this.allGroupsList = null;
			this.rootGroupsList = null;
		} else {
			check(u == null && i == null && del == null && sha1 == null && artifactName == null && artifactDesc == null,
					nr, "u, i, del, sha, n, d / m");

			check((allGroups == null) == (allGroupsList == null), nr, "Invalid all groups doc");
			check((rootGroups == null) == (rootGroupsList == null), nr, "Invalid root groups doc");
			check((descriptor == null) == (idxinfo == null), nr, "Invalid description/idxinfo doc");

			if (descriptor != null) {
				check(allGroups == null && rootGroups == null, nr, "all/root");
				check(descriptor.equals("NexusIndex"), nr, "NexusIndex");

				this.allGroupsList = null;
				this.rootGroupsList = null;
			} else if (allGroups != null) {
				check(rootGroups == null && descriptor == null, nr, "root/desc");
				check(allGroups.equals("allGroups"), nr, "allGroups");

				this.allGroupsList = allGroupsList.split("\\|");
				this.rootGroupsList = null;
			} else if (rootGroups != null) {
				check(allGroups == null && descriptor == null, nr, "null m");
				check(rootGroups.equals("rootGroups"), nr, "rootGroups");

				this.allGroupsList = null;
				this.rootGroupsList = rootGroupsList.split("\\|");
			} else {
				throw new ParseException("Invalid record type: " + nr, 0);
			}

			groupid = null;
			artifactid = null;
			version = null;
			classifier = null;
			packaging = null;

			idate = null;
			size = -1;
			is3 = 0;
			is4 = 0;
			is5 = 0;
			extension = null;
			mdate = null;
		}
	}

	private static boolean isMain(String classifier) {
		return classifier == null;
	}

	private static int checkDigit(String s, NexusRecord nr) throws ParseException {
		check(s.matches("[0-9]"), nr, "Invalid digit: %s", s);
		return Integer.parseInt(s);
	}

	private static long checkSignedLong(String s, NexusRecord nr) throws ParseException {
		check(s.matches("-?[0-9]+"), nr, "Invalid digit: %s", s);
		return Long.parseLong(s);
	}

	private static Date checkDate(String s, NexusRecord nr) throws ParseException {
		check(s.matches("[0-9]{13}"), nr, "Invalid date: %s", s);
		return new Date(Long.parseLong(s));
	}

	private static void check(boolean cond, NexusRecord nr, String message, Object... args) throws ParseException {
		if (!cond) {
			throw new ParseException(String.format(message, args) + " @ " + nr, 0);
		}
	}
}
