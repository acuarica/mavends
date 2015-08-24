package ch.usi.inf.mavends.extract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.db.Inserter;

public class ClassAnalysis {

	private static class ExtractVisitor extends ClassVisitor {

		private String gid;
		private String aid;
		private String ver;
		private String className;
		private Inserter cls;
		private Inserter method;
		private Inserter callsite;
		private Inserter allocsite;
		private Inserter fieldaccess;
		private Inserter literal;
		private Inserter zero;

		public ExtractVisitor(Db db, String gid, String aid, String ver)
				throws SQLException {
			super(Opcodes.ASM5);

			this.gid = gid;
			this.aid = aid;
			this.ver = ver;

			cls = db.createInserter("insert into class (gid, aid, ver, classname, supername, version, access, signature) values (?,?,?,  ?, ?, ?, ?, ?)");

			method = db
					.createInserter("insert into method (gid, aid, ver, classname, methodname, methoddesc) values (?,?,?,  ?, ?, ?)");

			callsite = db
					.createInserter("insert into callsite (gid, aid, ver, classname,methodname, methoddesc, offset, targetclass,targetmethod,targetdesc) values (?,?,?,  ?, ?, ?, ?,  ?, ?, ?)");

			allocsite = db
					.createInserter("insert into allocsite (gid, aid, ver, classname,methodname, methoddesc, offset, opcode, type) values (?,?,?,  ?, ?, ?, ?, ?,  ?)");

			fieldaccess = db
					.createInserter("insert into fieldaccess (gid, aid, ver, classname,methodname, methoddesc, offset, targetclass,targetfield,targetdesc) values (?,?,?,  ?, ?, ?, ?,  ?, ?, ?)");

			literal = db
					.createInserter("insert into literal (gid, aid, ver, classname,methodname, methoddesc, offset, literal) values (?,?,?,  ?, ?, ?, ?,  ?)");

			zero = db
					.createInserter("insert into zero (gid, aid, ver, classname,methodname, methoddesc, offset, opcode) values (?,?,?,  ?, ?, ?, ?,  ?)");
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			className = name;
			super.visit(version, access, name, signature, superName, interfaces);

			cls.insert(gid, aid, ver, name, superName, version, access,
					signature);
		}

		@Override
		public MethodVisitor visitMethod(int access, final String methodName,
				final String methodDesc, String signature, String[] exceptions) {

			method.insert(gid, aid, ver, className, methodName, methodDesc);

			MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

				int offset = 0;

				@Override
				public void visitInsn(int opcode) {
					// String opcodeName = Printer.OPCODES[opcode];
					// zero.insert(gid, aid, ver, className, methodName,
					// methodDesc, offset++, opcodeName);
				}

				@Override
				public void visitMethodInsn(int opcode, String owner,
						String name, String desc, boolean itf) {
					callsite.insert(gid, aid, ver, className, methodName,
							methodDesc, offset++, owner, name, desc);
				}

				@Override
				public void visitFieldInsn(int opcode, String owner,
						String name, String desc) {
					fieldaccess.insert(gid, aid, ver, className, methodName,
							methodDesc, offset++, owner, name, desc);
				};

				@Override
				public void visitLdcInsn(Object cst) {
					if (cst instanceof String) {
						String value = (String) cst;
						literal.insert(gid, aid, ver, className, methodName,
								methodDesc, offset++, value);
					}
				}

				@Override
				public void visitTypeInsn(int opcode, String type) {
					// String opcodeName = Printer.OPCODES[opcode];
					// allocsite.insert(gid, aid, ver, className, methodName,
					// methodDesc, offset++, opcodeName, type);
				}

			};

			return mv;
		}
	}

	private static void searchJarFile(byte[] jarFileBuffer, Db db, String gid,
			String aid, String ver) throws IOException, SQLException {
		ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(
				jarFileBuffer));

		ZipEntry entry;

		Inserter ei = db
				.createInserter("insert into jarentry (gid, aid, ver, filename, originalsize, compressedsize) values (?,?,?, ?, ?, ?)");

		while ((entry = zip.getNextEntry()) != null) {
			ei.insert(gid, aid, ver, entry.getName(), entry.getSize(),
					entry.getCompressedSize());

			if (!entry.getName().endsWith(".class")) {
				continue;
			}

			ByteArrayOutputStream classfile = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];

			int len = 0;
			while ((len = zip.read(buffer)) > 0) {
				classfile.write(buffer, 0, len);
			}

			ClassReader cr = new ClassReader(classfile.toByteArray());
			ExtractVisitor uv = new ExtractVisitor(db, gid, aid, ver);
			cr.accept(uv, 0);
		}
	}

	public static void searchJarFile(String jarFileName, Db db, String gid,
			String aid, String ver) throws IOException, SQLException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		searchJarFile(jarFileBuffer, db, gid, aid, ver);
	}
}
