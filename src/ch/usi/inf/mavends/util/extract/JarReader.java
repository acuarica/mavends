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

public abstract class JarReader {

	private static final Log log = new Log(System.out);

	private final byte[] buffer = new byte[2 * 8192];

	private final ByteArrayOutputStream stream = new ByteArrayOutputStream(buffer.length);

	private final String repoDir;

	public JarReader(String repoDir) {
		this.repoDir = repoDir;
	}

	public void process(Artifact artifact, String path) {
		try (
				final FileInputStream fis = new FileInputStream(repoDir + "/" + path);
				final BufferedInputStream bis = new BufferedInputStream(fis);
				final ZipInputStream zip = new ZipInputStream(bis)) {

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
            processFileNotFound();
		} catch (IOException e) {
			log.info("IO Exception: %s", e);
		}
	}

	public abstract void processEntry(Artifact artifact, String filename, byte[] classFile);

	public abstract void processFileNotFound();
}
