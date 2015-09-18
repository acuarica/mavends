package ch.usi.inf.mavends.analysis;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.util.log.Log;

abstract class JarReader {

	private static final Log log = new Log(System.out);

	private final String repoDir;

	public JarReader(String repoDir) {
		this.repoDir = repoDir;
	}

	public void process(String path) {
		final byte[] buffer = new byte[8192];

		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(repoDir + "/" + path));
				ZipInputStream zip = new ZipInputStream(bis)) {

			ZipEntry ze;
			while ((ze = zip.getNextEntry()) != null) {
				final String filename = ze.getName();

				if (!filename.endsWith(".class")) {
					continue;
				}

				int len = 0;

				final ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);

				while ((len = zip.read(buffer)) > 0) {
					stream.write(buffer, 0, len);
				}

				final byte[] classFile = stream.toByteArray();
				processEntry(filename, classFile);
			}
		} catch (FileNotFoundException e) {
			log.info("File not found: %s", e);
		} catch (IOException e) {
			log.info("IO Exception: %s", e);
		}
	}

	abstract void processEntry(String filename, byte[] classFile);
}
