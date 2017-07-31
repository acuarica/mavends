package ch.usi.inf.mavends.analysis.visitors;

import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Statement;
import ch.usi.inf.mavends.util.extract.Artifact;
import ch.usi.inf.mavends.util.extract.MavenVisitor;
import org.objectweb.asm.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class MavenClassVisitor extends MavenVisitor {

	private static final String INSERT_CLASS = "insert into class (version, access, classname, signature, superclass, interfaces) values (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_METHOD = "insert into method (classid, access, methodname, methoddesc, signature, exceptions) values (?, ?, ?, ?, ?, ?)";
//    private static final String UPDATE_CODETEXT = "update method set codetext=? where methodid=?";

	private final Db db;
    private final Statement insertClassStmt;
    private final Statement insertMethodStmt;
//    private final Statement updateCodeTextStmt;
    private final Statement insertZeroInstStmt;
//    private final Statement insertTypeInstStmt;

    private boolean hasCheckcast;

	public MavenClassVisitor() throws FileNotFoundException, SQLException {
		db = new Db("out/mavenclass.sqlite3");
		insertClassStmt = db.createStatement(INSERT_CLASS);
        insertMethodStmt = db.createStatement(INSERT_METHOD);
//        updateCodeTextStmt = db.createStatement(UPDATE_CODETEXT);
        insertZeroInstStmt = db.createStatement("insert into code (methodid, opcode, args) values (?, ?, ?)");
//        insertTypeInstStmt = db.createStatement("insert into type (methodid, opcode, type) values (?, ?, ?)");
	}

	@Override
	public ClassVisitor visitClass(final Artifact art) {
		return new ClassVisitor(Opcodes.ASM5) {

			String className;
			long classId;

			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				className = name;
				try {
					insertClassStmt.execute(version, access, name, signature, superName, Arrays.toString(interfaces));
                    classId = db.lastInsertRowId();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

            @Override
            public void visitEnd() {
                try {
                    db.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
			public MethodVisitor visitMethod(int access, final String methodName, final String methodDesc,
					String signature, String[] exceptions) {

			    long methodId = -1;

                try {
                    insertMethodStmt.execute(classId, access, methodName, methodDesc, signature, Arrays.toString(exceptions));
                    methodId = db.lastInsertRowId();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                hasCheckcast = false;
                long finalMethodId = methodId;
                Printer p = new Textifier(Opcodes.ASM5) {
                            @Override
                            public void visitMethodEnd() {
//                                if (hasCheckcast) {
//                                    String m = String.format("---- Method %s.%s ----", className, methodName);
//                                    StringWriter sw = new StringWriter();
//                                    print(new PrintWriter(sw));

//                                    System.out.println(sw.toString());
//                                try {
//                                    updateCodeTextStmt.execute(sw.toString(), finalMethodId);
//                                } catch (SQLException e) {
//                                    e.printStackTrace();
//                                }
//                                }
                            }
                        };

				MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

				    @Override
                    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {

                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				        insert(opcode, owner, name, desc, itf);
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				        insert(opcode, owner, name, desc);
                    };

                    @Override
                    public void visitLdcInsn(Object cst) {
                        insert(Opcodes.LDC, cst);
                    }

                    @Override
                    public void visitIincInsn(int i, int i1) {
                        insert(i, i1);
                    }

                    @Override
                    public void visitMultiANewArrayInsn(String s, int i) {
                        insert(Opcodes.MULTIANEWARRAY, s, i);
                    }

                    @Override
                    public void visitIntInsn(int i, int i1) {
                        insert(i, i1);
                    }

                    @Override
                    public void visitJumpInsn(int i, Label label) {
                        insert(i, label);
                    }

                    @Override
                    public void visitVarInsn(int i, int i1) {
                        insert(i, i1);
                    }

                    @Override
                    public void visitInvokeDynamicInsn(String s, String s1, Handle handle, Object... objects) {
                        insert(Opcodes.INVOKEDYNAMIC, s, s1, handle, objects);
                    }

                    @Override
                    public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
                        insert(Opcodes.LOOKUPSWITCH, label, ints, labels);
                    }

                    @Override
                    public void visitTableSwitchInsn(int i, int i1, Label label, Label... labels) {
                        insert(Opcodes.TABLESWITCH, i, i1, label, labels);
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        insert(opcode);
                    }

                    @Override
					public void visitTypeInsn(int opcode, String type) {
                        insert(opcode, type);
                    }

					private void insert(int opcode, Object ... values) {
                        try {
                            insertZeroInstStmt.execute(finalMethodId, opcode, Arrays.toString(values));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
				};

				return new TraceMethodVisitor(mv, p);
			}
		};
	}

	@Override
	public void close() {
//		db.close();
	}
}
