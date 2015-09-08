package ch.usi.inf.mavends.analysis;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.mavends.util.db.Inserter;

public class ExtractVisitor extends ClassVisitor {

//	private final String coorid;
//	private final Inserter callsite;
	String className;

	public ExtractVisitor(String coorid, Inserter callsite) {
		super(Opcodes.ASM5);
//		this.coorid = coorid;
//		this.callsite = callsite;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		className = name;
		super.visit(version, access, name, signature, superName, interfaces);

		// cls.insert(pid, name, superName, version, access,
		// signature);
	}

	@Override
	public MethodVisitor visitMethod(int access, final String methodName,
			final String methodDesc, String signature, String[] exceptions) {

		// method.insert(pid, className, methodName,
		// methodDesc);
		MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

//			int offset = 0;
//
//			private String[] get(String c) {
//				int i = c.lastIndexOf("/");
//				if (i == -1) {
//					return new String[] { "", c };
//				}
//				return new String[] { c.substring(0, i), c.substring(i + 1) };
//			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name,
					String desc, boolean itf) {
//				String[] cn = get(className);
//				String[] tcn = get(owner);
//				callsite.insert(coorid, cn[0], cn[1], methodName, methodDesc,
//						offset++, tcn[0], tcn[1], name, desc);
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name,
					String desc) {
				// fieldaccess.insert(pid, className,
				// methodName,
				// methodDesc,
				// offset++, owner, name, desc);
			};

			@Override
			public void visitLdcInsn(Object cst) {
				if (cst instanceof String) {
					String value = (String) cst;
					// literal.insert(pid, className,
					// methodName,
					// methodDesc,
					// offset++, value);
				}
			}
		};

		return mv;
	}
}