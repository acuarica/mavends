package ch.usi.inf.mavends.util.extract;

import java.util.Date;

public final class Artifact {

	public final long coordid;
	public final String path;
	public final String groupid;
	public final String artifactid;
	public final String version;
	public final Date idate;
	public final Date mdate;

	public Artifact(long coordid, String groupid, String artifactid, String version, Date idate, Date mdate, String path) {
		this.coordid = coordid;
		this.groupid = groupid;
		this.artifactid = artifactid;
		this.version = version;
		this.idate = idate;
		this.mdate = mdate;
		this.path = path;
	}
}
