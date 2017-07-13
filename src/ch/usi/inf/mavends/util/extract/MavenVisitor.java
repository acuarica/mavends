package ch.usi.inf.mavends.util.extract;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public abstract class MavenVisitor implements AutoCloseable {

    public long filesNotFound;

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
