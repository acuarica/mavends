package ch.usi.inf.mavends.analysis.visitors;

import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Statement;
import ch.usi.inf.mavends.util.extract.Artifact;
import ch.usi.inf.mavends.util.extract.MavenVisitor;
import org.objectweb.asm.*;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Arrays;

public class MavenClassVisitor extends MavenVisitor {

    private final Db db;
    private final Statement insertClassStmt;
    private final Statement insertMethodStmt;
    private final Statement insertZeroInstStmt;

    private final MyClassVisitor classVisitor = new MyClassVisitor();

    public MavenClassVisitor() throws FileNotFoundException, SQLException {
        db = new Db("out/mavenclass.sqlite3");
        insertClassStmt = db.createStatement("insert into class (version, access, classname, signature, superclass, interfaces) values (?, ?, ?, ?, ?, ?)");
        insertMethodStmt = db.createStatement("insert into method (classid, access, methodname, methoddesc, signature, exceptions) values (?, ?, ?, ?, ?, ?)");
        insertZeroInstStmt = db.createStatement("insert into code (methodid, opcode, args) values (?, ?, ?)");
    }

    @Override
    public ClassVisitor visitClass(final Artifact art) {
        return classVisitor;
    }

    @Override
    public void close() throws SQLException {
        try {
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class MyClassVisitor extends ClassVisitor {

        String className;
        long classId;

        MyClassVisitor() {
            super(Opcodes.ASM5);
        }

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
            try {
                insertMethodStmt.execute(classId, access, methodName, methodDesc, signature, Arrays.toString(exceptions));
                final long methodId = db.lastInsertRowId();

                return new MyMethodVisitor(methodId);

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        }
    }

    private class MyMethodVisitor extends MethodVisitor {

        private final long methodId;

        MyMethodVisitor(long methodId) {
            super(Opcodes.ASM5);
            this.methodId = methodId;
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            insert(start, end, handler, type);
        }

        @Override
        public void visitLabel(Label label) {
            insert(label);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            insert(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            insert(opcode, owner, name, desc);
        }

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

        private void insert(Object opcode, Object... values) {
            try {
                insertZeroInstStmt.execute(methodId, opcode, Arrays.toString(values));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
