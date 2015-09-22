package ch.usi.inf.mavends.analysis;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public abstract class MavenVisitor implements AutoCloseable {

	public ClassVisitor visitClass(Artifact artifact) {
		return null;
	}

	public void visitFileEntry(Artifact artifact, String fileName, byte[] fileData) {
		if (fileName.endsWith(".class")) {
			final ClassReader cr = new ClassReader(fileData);
			final ClassVisitor v = visitClass(artifact);

			if (v != null) {
				cr.accept(v, 0);
			}
		}
	}
}
