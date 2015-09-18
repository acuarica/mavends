package ch.usi.inf.mavends.analysis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

final class ArtifactQueue implements Iterable<Artifact> {

	private final List<Artifact> queue = new LinkedList<Artifact>();

	void add(long coordid, String path) {
		queue.add(new Artifact(coordid, path));
	}

	int size() {
		return queue.size();
	}

	@Override
	public Iterator<Artifact> iterator() {
		return queue.iterator();
	}
}