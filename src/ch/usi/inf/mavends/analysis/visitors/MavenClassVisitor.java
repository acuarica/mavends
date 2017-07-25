package ch.usi.inf.mavends.analysis.visitors;

import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Statement;
import ch.usi.inf.mavends.util.extract.Artifact;
import ch.usi.inf.mavends.util.extract.MavenVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
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
    private static final String UPDATE_CODETEXT = "update method set codetext=? where methodid=?";

	private final Db db;
    private final Statement insertClassStmt;
    private final Statement insertMethodStmt;
    private final Statement updateCodeTextStmt;
    private final Statement insertZeroInstStmt;

    private boolean hasCheckcast;

	public MavenClassVisitor() throws FileNotFoundException, SQLException {
		db = new Db("out/mavenclass.sqlite3");
		insertClassStmt = db.createStatement(INSERT_CLASS);
        insertMethodStmt = db.createStatement(INSERT_METHOD);
        updateCodeTextStmt = db.createStatement(UPDATE_CODETEXT);
        insertZeroInstStmt = db.createStatement("insert into zero (methodid, opcode) values (?, ?)");
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
//                    db.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                hasCheckcast = false;
                long finalMethodId = methodId;
                Printer p = new Textifier(Opcodes.ASM5) {
                            @Override
                            public void visitMethodEnd() {
//                                if (hasCheckcast) {
                                    String m = String.format("---- Method %s.%s ----", className, methodName);
                                    StringWriter sw = new StringWriter();
                                    print(new PrintWriter(sw));

                                    System.out.println(sw.toString());
                                try {
                                    updateCodeTextStmt.execute(sw.toString(), finalMethodId);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
//                                }
                            }
                        };

				MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

                    @Override
                    public void visitInsn(int i) {
                        try {
                            insertZeroInstStmt.execute(finalMethodId, i);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

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
//		db.close();
	}
}
