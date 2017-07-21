package ch.usi.inf.mavends.analysis.visitors;

import ch.usi.inf.mavends.util.extract.Artifact;
import ch.usi.inf.mavends.util.extract.MavenVisitor;
import org.objectweb.asm.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class CastStatsVisitor extends MavenVisitor {

	private final PrintStream out;

    private boolean hasCheckcast;

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

                hasCheckcast = false;
            Printer p = new Textifier(Opcodes.ASM5) {
                            @Override
                            public void visitMethodEnd() {
                                if (hasCheckcast) {
                                    String m = String.format("---- Method %s.%s ----", className, methodName);
                                    System.out.println(m);
                                    print(new PrintWriter(System.out));
                                }
                            }
                        };

				MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

                    @Override
					public void visitTypeInsn(int opcode, String type) {
                        if (opcode == Opcodes.CHECKCAST) {
                            hasCheckcast = true;
                        } else if (opcode == Opcodes.INSTANCEOF) {
						}
					}

					public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
				        if ("java/lang/ClassCastException".equals(type)) {
//                            add("ClassCastException", type, "");
                        }
					}

					private void add(String owner, String name, String desc) {
//						out.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", art.coordid, art.groupid, art.artifactid,
//								art.version, art.idate, art.mdate, className, methodName, methodDesc, owner, name, desc);
					}
				};

				return new TraceMethodVisitor(mv, p);
			}
		};
	}

	@Override
	public void close() {
		out.close();
	}
}
