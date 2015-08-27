package ch.usi.inf.mavends;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.mavends.argsparser.Arg;
import ch.usi.inf.mavends.argsparser.ArgsParser;
import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.db.Inserter;
import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.util.JarVisitor;
import ch.usi.inf.mavends.util.Log;

public class BuildMavenBytecode {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index.")
		public String mavenIndexPath;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "Filter query", desc = "Specifies the path of the Maven repository.")
		public String query;

		@Arg(key = "mavenbytecode", name = "Maven Bytecode DB path", desc = "Specifies the path of the output db file.")
		public String mavenBytecodePath;

	}

	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			ClassNotFoundException, SQLException, IOException {
		Args ar = ArgsParser.parse(args, Args.class);

		Db db = new Db(ar.mavenBytecodePath);

		db.send("mavenbytecode.sql", "Creating database with");

		db.conn.setAutoCommit(false);

		ResultSet rs = new Db(ar.mavenIndexPath)
				.select("select a.coorid as coorid, a.groupid as groupid, a.artifactid as artifactid, a.version as version, a.classifier as classifier, a.extension as extension from ("
						+ ar.query
						+ ") t inner join artifact a on a.coorid = t.coorid");

		final Inserter cls;
		final Inserter method;
		final Inserter callsite;
		final Inserter fieldaccess;
		final Inserter literal;

		// cls =
		// db.createInserter("insert into class (pid, classname, supername, version, access, signature) values (?,  ?, ?, ?, ?, ?)");

		// cls =
		// db.createInserter("insert into cp_class (classnameid, classname) values (?,  ?)");

		// method = db
		// .createInserter("insert into method (mid, cid, methodname, methoddesc) values (?,  ?, ?, ?)");
		//
		callsite = db
				.createInserter("insert into callsite_view (coorid, classname,methodname, methoddesc) values (?,  ?, ?, ?)");
		// callsite = db
		// .createInserter("insert into callsite (pid, tm) values (?,  ?)");
		//
		// fieldaccess = db
		// .createInserter("insert into fieldaccess (pid, classname,methodname, methoddesc, offset, targetclass,targetfield,targetdesc) values (?,  ?, ?, ?, ?,  ?, ?, ?)");
		//
		// literal = db
		// .createInserter("insert into literal (pid, classname,methodname, methoddesc, offset, literal) values (?,  ?, ?, ?, ?,  ?)");

		int n = 0;
		while (rs.next()) {
			final String coorid = rs.getString("coorid");
			final String groupid = rs.getString("groupid");
			final String artifactid = rs.getString("artifactid");
			final String version = rs.getString("version");
			final String classifier = rs.getString("classifier");
			final String extension = rs.getString("extension");

			final String path = MavenRecord.getPath(groupid, artifactid,
					version, classifier, extension);

			// String pid = String.format("%s:%s@%s", gid, aid, ver); //
			// rs.getString("pid");
			// String path = MavenRecord.getPath(gid, aid, ver, null, "jar"); //
			// rs.getString("path");

			log.info("Analysing %s...", path);

			try {
				JarVisitor.accept(ar.repoDir + "/" + path, new ClassVisitor(
						Opcodes.ASM5) {

					String className;

					@Override
					public void visit(int version, int access, String name,
							String signature, String superName,
							String[] interfaces) {
						className = name;
						super.visit(version, access, name, signature,
								superName, interfaces);

						// cls.insert(pid, name, superName, version, access,
						// signature);
					}

					@Override
					public MethodVisitor visitMethod(int access,
							final String methodName, final String methodDesc,
							String signature, String[] exceptions) {

						// method.insert(pid, className, methodName,
						// methodDesc);
						MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

							int offset = 0;

							// int getc(String cn) {
							// int hc = cn.hashCode();
							// cls.insert(hc, cn);
							// return hc;
							// }
							//
							// int get(String a, String b, String c) {
							// int cid = getc(a);
							// int hc = (a + b + c).hashCode();
							// method.insert(hc, cid, b, c);
							// return hc;
							// }

							@Override
							public void visitMethodInsn(int opcode,
									String owner, String name, String desc,
									boolean itf) {
								// int a = get(className, methodName,
								// methodDesc);
								// int b = get(owner, name, desc);
								// callsite.insert(pid, className, methodName,
								// methodDesc,
								// offset++, owner, name, desc);
								callsite.insert(coorid, owner, name, desc);
								// callsite.insert(coorid, b);
							}

							@Override
							public void visitFieldInsn(int opcode,
									String owner, String name, String desc) {
								// fieldaccess.insert(pid, className,
								// methodName,
								// methodDesc,
								// offset++, owner, name, desc);
							};

							@Override
							public void visitLdcInsn(Object cst) {
								if (cst instanceof String) {
									String value = (String) cst;
									// literal.insert(pid, className,
									// methodName,
									// methodDesc,
									// offset++, value);
								}
							}

						};

						return mv;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

			n++;
		}

		log.info("No. jar files: %d", n);

		db.conn.commit();
	}
}
