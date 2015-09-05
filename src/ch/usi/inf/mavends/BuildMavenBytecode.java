package ch.usi.inf.mavends;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.usi.inf.mavends.args.Arg;
import ch.usi.inf.mavends.args.ArgsParser;
import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.db.Inserter;
import ch.usi.inf.mavends.extract.ExtractVisitor;
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

		Db db4 = new Db(ar.mavenBytecodePath);

		db4.send("mavenbytecode.sql", "Creating database with");
		db4.close();

		ResultSet rs = new Db(ar.mavenIndexPath)
				.select("select a.coorid as coorid, a.groupid as groupid, a.artifactid as artifactid, a.version as version, a.classifier as classifier, a.extension as extension from ("
						+ ar.query
						+ ") t inner join artifact a on a.coorid = t.coorid");

		final Inserter cls;
		final Inserter method;
		final Inserter callsite;
		final Inserter fieldaccess;
		final Inserter literal;

		Db dbm = new Db(":memory:");
		dbm.execute("attach database '" + ar.mavenBytecodePath + "' as mb");
		dbm.send("mavencallsite.sql", "Creating database with");

		dbm.conn.setAutoCommit(false);

		Inserter ji = dbm
				.createInserter("insert into mb.jarentry (coorid,filename, originalsize,compressedsize,crc32) values (?,?,?,?,?)");

		// cls =
		// db.createInserter("insert into class (pid, classname, supername, version, access, signature) values (?,  ?, ?, ?, ?, ?)");

		// cls =
		// db.createInserter("insert into cp_class (classnameid, classname) values (?,  ?)");

		// method = db
		// .createInserter("insert into method (mid, cid, methodname, methoddesc) values (?,  ?, ?, ?)");
		//
		callsite = dbm
				.createInserter("insert into callsite (coorid, package, classname,methodname, methoddesc, offset, targetpackage, targetclassname,targetmethodname, targetmethoddesc) values (?,  ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		//
		// fieldaccess = db
		// .createInserter("insert into fieldaccess (pid, classname,methodname, methoddesc, offset, targetclass,targetfield,targetdesc) values (?,  ?, ?, ?, ?,  ?, ?, ?)");
		//
		// literal = db
		// .createInserter("insert into literal (pid, classname,methodname, methoddesc, offset, literal) values (?,  ?, ?, ?, ?,  ?)");
		//
		// Inserter ii1 = dbm
		// .createInserter("insert into mb.cp_class (package, classname) select cs.targetpackage, cs.targetclassname from callsite cs");
		// Inserter ii2 = dbm
		// .createInserter("insert into mb.cp_methodref (classnameid, methodname, methoddesc) select "
		// +
		// "(select c.classnameid from mb.cp_class c where c.package = cs.targetpackage and c.classname = cs.targetclassname), cs.targetmethodname, cs.targetmethoddesc from callsite cs");
		// Inserter ii3 = dbm
		// .createInserter("insert into mb.callsite (coorid, sm, tm) select cs.coorid, sm.methodrefid, tm.methodrefid from callsite cs "
		// +
		// "inner join mb.cp_methodref_view sm on sm.package=cs.package and sm.classname=cs.classname and sm.methodname=cs.methodname and sm.methoddesc=cs.methoddesc "
		// +
		// "inner join mb.cp_methodref_view tm on tm.package=cs.targetpackage and tm.classname=cs.targetclassname and tm.methodname=cs.targetmethodname and tm.methoddesc=cs.targetmethoddesc ");

		int n = 0;
		while (rs.next()) {
			final String coorid = rs.getString("coorid");
			String groupid = rs.getString("groupid");
			String artifactid = rs.getString("artifactid");
			String version = rs.getString("version");
			String classifier = rs.getString("classifier");
			String extension = rs.getString("extension");

			String path = MavenRecord.getPath(groupid, artifactid, version,
					classifier, extension);

			if (n % 100 == 0) {
				// log.info("Analysing %s...", path);
				log.info("%d jars", n);
			}

			try {
				JarVisitor.accept(ar.repoDir + "/" + path, new ExtractVisitor(
						coorid, callsite), ji, coorid);

				// ii1.insert();
				// ii2.insert();
				// ii3.insert();
				// dbm.execute("delete from callsite");
				// dbm.conn.commit();
			} catch (Exception e) {
				log.info("Exception analysing %s...", path);
				e.printStackTrace();

				ji = dbm.createInserter("insert into mb.jarentry (coorid,filename, originalsize,compressedsize,crc32) values (?,?,?,?,?)");

			}

			n++;
		}

		log.info("No. jar files: %d", n);

		dbm.conn.commit();
	}
}
