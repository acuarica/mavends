package ch.usi.inf.mavends.analysis.visitors;

import org.objectweb.asm.*;

import ch.usi.inf.mavends.util.extract.Artifact;
import ch.usi.inf.mavends.util.extract.MavenVisitor;
import ch.usi.inf.mavends.util.log.Log;

/**
 *
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class StatsVisitor extends MavenVisitor {

	private static final Log log = new Log(System.out);

	private long size = 0;
	private long classCount = 0;
	private long methodCount = 0;
	private long callsiteCount = 0;
	private long fielduseCount = 0;
	private long constantCount = 0;
	private long checkCastCount = 0;
	private long instanceOfCount = 0;
	private long classCastExceptionCount = 0;
    private long zeroOpCount;
    private long iincCount;
    private long multiANewArrayCount;
    private long intOpCount;
    private long jumpCount;
    private long varCount;
    private long invokeDynamicCount;
    private long lookupSwitchCount;
    private long tableSwitchCount;
    private boolean hasCheckcast;
    private boolean hasInstanceOf;
    private long methodsWithCheckcast;
    private long methodsWithInstanceOf;

    @Override
	public void visitFileEntry(Artifact artifact, String fileName, byte[] fileData) {
		size += fileData.length;
		super.visitFileEntry(artifact, fileName, fileData);
	}

	@Override
	public ClassVisitor visitClass(Artifact artifact) {
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
				hasCheckcast = false;
				hasInstanceOf = false;

				MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

                    @Override
                    public void visitEnd() {
                        if (hasCheckcast) {
                            methodsWithCheckcast++;
                        }

                        if (hasInstanceOf) {
                            methodsWithInstanceOf++;
                        }
                    }

                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        if (opcode == Opcodes.CHECKCAST) {
                            checkCastCount++;
                            hasCheckcast = true;
                        } else if (opcode == Opcodes.INSTANCEOF) {
                            instanceOfCount++;
                            hasInstanceOf = true;
                        }
                    }

                    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                        if ("java/lang/ClassCastException".equals(type)) {
                            classCastExceptionCount++;
                        }
                    }
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

                    @Override
                    public void visitInsn(int i) {
					    zeroOpCount++;
                    }

                    @Override
                    public void visitIincInsn(int i, int i1) {
					    iincCount++;
                    }

                    @Override
                    public void visitMultiANewArrayInsn(String s, int i) {
					    multiANewArrayCount++;
                    }

                    @Override
                    public void visitIntInsn(int i, int i1) {
					    intOpCount++;
                    }

                    @Override
                    public void visitJumpInsn(int i, Label label) {
					    jumpCount++;
                    }

                    @Override
                    public void visitVarInsn(int i, int i1) {
					    varCount++;
                    }

                    @Override
                    public void visitInvokeDynamicInsn(String s, String s1, Handle handle, Object... objects) {
					    invokeDynamicCount++;
                    }

                    @Override
                    public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
					    lookupSwitchCount++;
                    }

                    @Override
                    public void visitTableSwitchInsn(int i, int i1, Label label, Label... labels) {
					    tableSwitchCount++;
                    }
                };

				return mv;
			}
		};
	}

	@Override
	public void close() {
        log.info("--- Size ---");
		log.info("Total uncompressed size: %,d MB", size / (1024 * 1024));
        log.info("--- Structural ---");
		log.info("Number of classes: %,d", classCount);
		log.info("Number of methods: %,d", methodCount);
		log.info("Number of call sites: %,d", callsiteCount);
		log.info("Number of field uses: %,d", fielduseCount);
		log.info("Number of constants: %,d", constantCount);

        log.info("--- Instructions ---");
        log.info("Number of zeroOpCount: %,d", zeroOpCount);
        log.info("Number of iincCount: %,d", iincCount);
        log.info("Number of multiANewArrayCount: %,d", multiANewArrayCount);
        log.info("Number of intOpCount: %,d", intOpCount);
        log.info("Number of jumpCount: %,d", jumpCount);
        log.info("Number of varCount: %,d", varCount);
        log.info("Number of invokeDynamicCount: %,d", invokeDynamicCount);
        log.info("Number of lookupSwitchCount: %,d", lookupSwitchCount);
        log.info("Number of tableSwitchCount: %,d", tableSwitchCount);

        log.info("--- Casts ---");
        log.info("Number of CHECKCAST: %,d", checkCastCount);
        log.info("Number of INSTANCEOF: %,d", instanceOfCount);
        log.info("Number of ClassCastException: %,d", classCastExceptionCount);
        log.info("Methods w/ CHECKCAST: %,d", methodsWithCheckcast);
        log.info("Methods w/ INSTANCEOF: %,d", methodsWithInstanceOf);

		log.info("--- Error ---");
        log.info("Files not found: %,d", filesNotFound);
	}
}
