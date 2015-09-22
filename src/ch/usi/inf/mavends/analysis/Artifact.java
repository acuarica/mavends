package ch.usi.inf.mavends.analysis;

public final class Artifact {

	public final long coordid;
	public final String path;
	public final String groupid;
	public final String artifactid;
	public final String version;

	Artifact(long coordid, String groupid, String artifactid, String version, String path) {
		this.coordid = coordid;
		this.groupid = groupid;
		this.artifactid = artifactid;
		this.version = version;
		this.path = path;
	}
}
