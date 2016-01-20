package ch.usi.inf.mavends.util.extract;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.util.log.Log;

abstract class JarReader extends Thread {

	private static final Log log = new Log(System.out);

	private final byte[] buffer = new byte[2 * 8192];

	private final ByteArrayOutputStream stream = new ByteArrayOutputStream(buffer.length);

	private final String repoDir;

	private final ArtifactQueue queue;

	public JarReader(String repoDir, ArtifactQueue queue) {
		this.repoDir = repoDir;
		this.queue = queue;
	}

	@Override
	public void run() {
		for (Iterator<Artifact> it = queue.iterator(); it.hasNext();) {
			final Artifact artifact = it.next();
			process(artifact, artifact.path);
			it.remove();
		}
	}

	private void process(Artifact artifact, String path) {
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(repoDir + "/" + path));
				ZipInputStream zip = new ZipInputStream(bis)) {

			ZipEntry ze;
			while ((ze = zip.getNextEntry()) != null) {
				int len = 0;

				while ((len = zip.read(buffer)) > 0) {
					stream.write(buffer, 0, len);
				}

				final byte[] fileData = stream.toByteArray();
				stream.reset();

				processEntry(artifact, ze.getName(), fileData);
			}

		} catch (FileNotFoundException e) {
			log.info("File not found: %s", e.getMessage());
		} catch (IOException e) {
			log.info("IO Exception: %s", e);
		}
	}

	abstract void processEntry(Artifact artifact, String filename, byte[] classFile);
}
