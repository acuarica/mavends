package ch.usi.inf.mavends.analysis.visitors;

import ch.usi.inf.mavends.util.extract.Artifact;
import ch.usi.inf.mavends.util.extract.MavenVisitor;
import ch.usi.inf.mavends.util.log.Log;

public class SizeStatsVisitor extends MavenVisitor {

	private static final Log log = new Log(System.out);

	private long size = 0;

	@Override
	public void visitFileEntry(Artifact artifact, String fileName, byte[] fileData) {
		size += fileData.length;
	}

	@Override
	public void close() throws Exception {
		log.info("Total uncompressed size: %,d MB", size / (1024 * 1024));
	}
}
