package ch.usi.inf.mavends.pom;

final class Dependency {

	String groupId;
	String artifactId;
	String version;
	String scope;

	@Override
	public String toString() {
		return String.format("g: %s, a: %s v: %s s: %s", groupId, artifactId, version, scope);
	}
}
