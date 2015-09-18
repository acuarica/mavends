package ch.usi.inf.mavends.analysis.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.mavends.analysis.MavenVisitor;
import ch.usi.inf.mavends.util.log.Log;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class StatsVisitor extends MavenVisitor {

	private static final Log log = new Log(System.out);

	private long classCount = 0;
	private long methodCount = 0;
	private long callsiteCount = 0;
	private long fielduseCount = 0;
	private long constantCount = 0;

	@Override
	public ClassVisitor visitClass() {
		return new ClassVisitor(Opcodes.ASM5) {

			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				super.visit(version, access, name, signature, superName, interfaces);
				classCount++;
			}

			@Override
			public MethodVisitor visitMethod(int access, final String methodName, final String methodDesc,
					String signature, String[] exceptions) {

				methodCount++;

				MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						callsiteCount++;
					}

					@Override
					public void visitFieldInsn(int opcode, String owner, String name, String desc) {
						fielduseCount++;
					};

					@Override
					public void visitLdcInsn(Object cst) {
						constantCount++;
					}
				};

				return mv;
			}
		};
	}

	@Override
	public void close() {
		log.info("Number of classes: %,d", classCount);
		log.info("Number of methods: %,d", methodCount);
		log.info("Number of call sites: %,d", callsiteCount);
		log.info("Number of field uses: %,d", fielduseCount);
		log.info("Number of constants: %,d", constantCount);
	}
}
