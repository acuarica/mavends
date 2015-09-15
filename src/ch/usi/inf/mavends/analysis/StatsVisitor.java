package ch.usi.inf.mavends.analysis;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class StatsVisitor extends ClassVisitor {

	static long noclasses = 0;
	static long nomethods = 0;
	static long nocallsites = 0;
	static long nofielduses = 0;
	static long noliteral = 0;

	public StatsVisitor() {
		super(Opcodes.ASM5);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		noclasses++;
	}

	@Override
	public MethodVisitor visitMethod(int access, final String methodName, final String methodDesc, String signature,
			String[] exceptions) {

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
}
