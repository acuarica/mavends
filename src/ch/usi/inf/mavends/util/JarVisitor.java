package ch.usi.inf.mavends.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import ch.usi.inf.mavends.db.Inserter;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class JarVisitor {

	/**
	 * For each .class file in the jar file buffer, it calls the ClassVisitor
	 * cv.
	 * 
	 * @param jarFileBuffer
	 * @param cv
	 * @param ji
	 * @param coorid
	 * @throws IOException
	 */
	public static void accept(byte[] jarFileBuffer, ClassVisitor cv,
			Inserter ji, String coorid) throws IOException {
		ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(
				jarFileBuffer));

		ZipEntry entry;

		while ((entry = zip.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				continue;
			}

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];

			int len = 0;
			while ((len = zip.read(buffer)) > 0) {
				stream.write(buffer, 0, len);
			}

			ji.insert(coorid, entry.getName(), entry.getSize(),
					entry.getCompressedSize(), entry.getCrc());

			if (entry.getName().endsWith(".class")) {
				ClassReader cr = new ClassReader(stream.toByteArray());
				cr.accept(cv, 0);
			}
		}
	}

	/**
	 * For each .class file in the jar file path, it calls the ClassVisitor cv.
	 * 
	 * @param jarFileName
	 * @param cv
	 * @param ji
	 * @param coorid
	 * @throws IOException
	 * @see {@link #accept(byte[], ClassVisitor)}
	 */
	public static void accept(String jarFileName, ClassVisitor cv, Inserter ji,
			String coorid) throws IOException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		accept(jarFileBuffer, cv, ji, coorid);
	}
}
