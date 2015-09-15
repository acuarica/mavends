package ch.usi.inf.mavends.analysis.visitors;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.mavends.analysis.MavenVisitor;

public class UnsafeVisitor extends MavenVisitor {

	private final PrintStream out;

	public UnsafeVisitor() throws FileNotFoundException {
		out = new PrintStream("out/unsafe.csv");
		out.format("className,methodName,methodDesc,owner,name,desc\n");
	}

	@Override
	public ClassVisitor visitClass() {
		return new ClassVisitor(Opcodes.ASM5) {

			// private final String coorid;
			String className;

			// public UnsafeVisitor(String coorid) {
			// super(Opcodes.ASM5);
			// // this.coorid = coorid;
			// }

			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				className = name;
			}

			@Override
			public MethodVisitor visitMethod(int access, final String methodName, final String methodDesc,
					String signature, String[] exceptions) {

				MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						if ("sun/misc/Unsafe".equals(owner)) {
							add(owner, name, desc);
						}
					}

					@Override
					public void visitFieldInsn(int opcode, String owner, String name, String desc) {
						if ("sun/misc/Unsafe".equals(owner)) {
							add(owner, name, desc);
						}
					};

					@Override
					public void visitLdcInsn(Object cst) {
						if (cst instanceof String && "sun.misc.Unsafe".equals((String) cst)) {
							add("sun.misc.Unsafe", "literal", "");
						}
					}

					private void add(String owner, String name, String desc) {
						out.format("%s,%s,%s,%s,%s,%s\n", className, methodName, methodDesc, owner, name, desc);
					}
				};

				return mv;
			}

		};
	}

	@Override
	public void close() {
		out.close();
	}
}
