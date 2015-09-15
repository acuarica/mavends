package ch.usi.inf.mavends.pom;

final class Dependency {
	
	public String groupId;
	public String artifactId;
	public String version;
	public String scope;

	@Override
	public String toString() {
		return String.format("g: %s, a: %s v: %s s: %s", groupId, artifactId, version, scope);
	}
}
