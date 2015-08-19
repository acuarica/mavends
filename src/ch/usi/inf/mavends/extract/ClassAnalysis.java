package ch.usi.inf.mavends.extract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;

import ch.usi.inf.mavends.index.Inserter;

public class ClassAnalysis {

	private static class ExtractVisitor extends ClassVisitor {

		private String className;
		private Inserter cls;
		private Inserter method;
		private Inserter callsite;
		private Inserter allocsite;
		private Inserter fieldaccess;
		private Inserter literal;
		private Inserter zero;

		public ExtractVisitor(Connection c) throws SQLException {
			super(Opcodes.ASM5);

			cls = new Inserter(
					c,
					"insert into class (name, supername, version, access, signature) values (?, ?, ?, ?, ?)");

//			method = new Inserter(c,
//					"insert into method (clsname, methodname, methoddesc) values (?, ?, ?)");
//
//			callsite = new Inserter(
//					c,
//					"insert into callsite (clsname,methodname, methoddesc, offset, targetclass,targetmethod,targetdesc) values (?, ?, ?, ?,  ?, ?, ?)");
//
//			allocsite = new Inserter(
//					c,
//					"insert into allocsite (clsname,methodname, methoddesc, offset, opcode, type) values (?, ?, ?, ?, ?,  ?)");
//
//			fieldaccess = new Inserter(
//					c,
//					"insert into fieldaccess (clsname,methodname, methoddesc, offset, targetclass,targetfield,targetdesc) values (?, ?, ?, ?,  ?, ?, ?)");
//
//			literal = new Inserter(
//					c,
//					"insert into literal (clsname,methodname, methoddesc, offset, literal) values (?, ?, ?, ?,  ?)");
//
//			zero = new Inserter(
//					c,
//					"insert into zero (clsname,methodname, methoddesc, offset, opcode) values (?, ?, ?, ?,  ?)");

			// a == null ? ""
			// : a.groupId, a == null ? ""
			// : a.artifactId, a == null ? ""
			// : a.version
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			className = name;
			super.visit(version, access, name, signature, superName, interfaces);

			cls.insert(name, superName, version, access, signature);
		}

		@Override
		public MethodVisitor visitMethod(int access, final String methodName,
				final String methodDesc, String signature, String[] exceptions) {

//			method.insert(className, methodName, methodDesc);

			MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

				int offset = 0;

				@Override
				public void visitInsn(int opcode) {
					String opcodeName = Printer.OPCODES[opcode];
			//		zero.insert(className, methodName, methodDesc, offset++,
				//			opcodeName);
				}

				@Override
				public void visitMethodInsn(int opcode, String owner,
						String name, String desc, boolean itf) {
				//	callsite.insert(className, methodName, methodDesc,
					//		offset++, owner, name, desc);
				}

				@Override
				public void visitFieldInsn(int opcode, String owner,
						String name, String desc) {
					//fieldaccess.insert(className, methodName, methodDesc,
						//	offset++, owner, name, desc);
				};

				@Override
				public void visitLdcInsn(Object cst) {
					if (cst instanceof String) {
						String value = (String) cst;
						//literal.insert(className, methodName, methodDesc,
							//	offset++, value);
					}
				}

				@Override
				public void visitTypeInsn(int opcode, String type) {
					// if (opcode == Opcodes.NEW) {
					String opcodeName = Printer.OPCODES[opcode];
				//	allocsite.insert(className, methodName, methodDesc,
					//		offset++, opcodeName, type);
					// }
				}

			};

			return mv;
		}
	}

	private static void searchClassFile(byte[] classFile, Connection c)
			throws SQLException {
		ClassReader cr = new ClassReader(classFile);
		ExtractVisitor uv = new ExtractVisitor(c);
		cr.accept(uv, 0);
	}

	private static void searchJarFile(byte[] jarFileBuffer, Connection c)
			throws IOException, SQLException {
		ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(
				jarFileBuffer));

		ZipEntry entry;

//		Inserter ei = new Inserter(c,
	//			"insert into jarentry (name, size, compressedsize) values (?, ?, ?)");

		while ((entry = zip.getNextEntry()) != null) {
			//ei.insert(entry.getName(), entry.getSize(),
				//	entry.getCompressedSize());

			if (!entry.getName().endsWith(".class")) {
				continue;
			}

			ByteArrayOutputStream classfile = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];

			int len = 0;
			while ((len = zip.read(buffer)) > 0) {
				classfile.write(buffer, 0, len);
			}

			searchClassFile(classfile.toByteArray(), c);
		}
	}

	public static void searchJarFile(String jarFileName, Connection c)
			throws IOException, SQLException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		searchJarFile(jarFileBuffer, c);
	}
}
