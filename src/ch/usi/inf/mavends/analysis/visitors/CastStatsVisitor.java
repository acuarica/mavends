package ch.usi.inf.mavends.analysis.visitors;

import ch.usi.inf.mavends.util.extract.Artifact;
import ch.usi.inf.mavends.util.extract.MavenVisitor;
import org.objectweb.asm.*;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class CastStatsVisitor extends MavenVisitor {

	private final PrintStream out;

	public CastStatsVisitor() throws FileNotFoundException {
		out = new PrintStream("out/casts.csv");
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
					public void visitTypeInsn(int opcode, String type) {
                        if (opcode == Opcodes.CHECKCAST) {
                            add("CHECKCAST", type, "");
                        } else if (opcode == Opcodes.INSTANCEOF) {
                            add("INSTANCEOF", type, "");
						}
					}

					public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
				        if ("java/lang/ClassCastException".equals(type)) {
                            add("ClassCastException", type, "");
                        }
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
