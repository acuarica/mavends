package ch.usi.inf.mavends.analysis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

final class ArtifactQueue implements Iterable<Artifact> {

	private final List<Artifact> queue = new LinkedList<Artifact>();

	void add(Artifact art) {
		queue.add(art);
	}

	int size() {
		return queue.size();
	}

	@Override
	public Iterator<Artifact> iterator() {
		return queue.iterator();
	}
}
