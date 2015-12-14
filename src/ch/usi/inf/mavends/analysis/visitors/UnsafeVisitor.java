package ch.usi.inf.mavends.analysis.visitors;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.mavends.analysis.Artifact;
import ch.usi.inf.mavends.analysis.MavenVisitor;

public class UnsafeVisitor extends MavenVisitor {

	private final PrintStream out;

	public UnsafeVisitor() throws FileNotFoundException {
		out = new PrintStream("out/unsafe.csv");
		out.format("coordid,groupid,artifactid,version,idate,mdate,className,methodName,methodDesc,owner,name,desc\n");
	}

	@Override
	public ClassVisitor visitClass(final Artifact art) {
		return new ClassVisitor(Opcodes.ASM5) {

			String className;

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

					@Override
					public void visitInvokeDynamicInsn(String arg0, String arg1, Handle arg2, Object... arg3) {
						add("invokedynamic", arg0, arg1);
					}

					private void add(String owner, String name, String desc) {
						out.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", art.coordid, art.groupid, art.artifactid,
								art.version, art.idate, art.mdate, className, methodName, methodDesc, owner, name, desc);
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
