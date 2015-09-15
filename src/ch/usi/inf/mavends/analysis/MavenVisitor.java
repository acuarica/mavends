package ch.usi.inf.mavends.analysis;

import org.objectweb.asm.ClassVisitor;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public abstract class MavenVisitor implements AutoCloseable {

	public abstract ClassVisitor visitClass();

}
