package ch.usi.inf.mavends.analysis.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.mavends.analysis.MavenVisitor;
import ch.usi.inf.mavends.util.log.Log;

public class StatsVisitor extends MavenVisitor {

	private static final Log log = new Log(System.out);

	private long noclasses = 0;
	private long nomethods = 0;
	private long nocallsites = 0;
	private long nofielduses = 0;
	private long noliteral = 0;

	@Override
	public ClassVisitor visitClass() {
		return new ClassVisitor(Opcodes.ASM5) {

			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				super.visit(version, access, name, signature, superName, interfaces);
				noclasses++;
			}

			@Override
			public MethodVisitor visitMethod(int access, final String methodName, final String methodDesc,
					String signature, String[] exceptions) {

				nomethods++;

				MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						nocallsites++;
					}

					@Override
					public void visitFieldInsn(int opcode, String owner, String name, String desc) {
						nofielduses++;
					};

					@Override
					public void visitLdcInsn(Object cst) {
						noliteral++;
					}
				};

				return mv;
			}
		};
	}

	@Override
	public void close() {
		log.info("No classes: %,d", noclasses);
		log.info("No methods: %,d", nomethods);
		log.info("No callsites: %,d", nocallsites);
		log.info("No field uses: %,d", nofielduses);
		log.info("No literal: %,d", noliteral);
	}
}
