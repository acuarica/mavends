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
	 * @throws IOException
	 */
	public static void accept(byte[] jarFileBuffer, ClassVisitor cv)
			throws IOException {
		ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(
				jarFileBuffer));

		ZipEntry entry;

		while ((entry = zip.getNextEntry()) != null) {
			if (!entry.getName().endsWith(".class")) {
				continue;
			}

			ByteArrayOutputStream classfile = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];

			int len = 0;
			while ((len = zip.read(buffer)) > 0) {
				classfile.write(buffer, 0, len);
			}

			ClassReader cr = new ClassReader(classfile.toByteArray());
			cr.accept(cv, 0);
		}
	}

	/**
	 * For each .class file in the jar file path, it calls the ClassVisitor cv.
	 * 
	 * @param jarFileName
	 * @param cv
	 * @throws IOException
	 * @see {@link #accept(byte[], ClassVisitor)}
	 */
	public static void accept(String jarFileName, ClassVisitor cv)
			throws IOException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		accept(jarFileBuffer, cv);
	}
}