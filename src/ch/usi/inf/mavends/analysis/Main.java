package ch.usi.inf.mavends.analysis;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.zip.InflaterInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.log.Log;

public final class Main {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index DB.")
		public String mavenIndex;

		@Arg(key = "query", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String query;

		@Arg(key = "maveninode", name = "Maven Inode DB path", desc = "Specifies the path of the output db file.")
		public String mavenInode;

		@Arg(key = "mavenvisitor", name = "Maven Visitor", desc = "Specifies the path of the output db file.")
		public String mavenVisitor;

	}

	public static void main(String[] args) throws Exception {
		final Args ar = ArgsParser.parse(args, new Args());

		Class<?> cls = Class.forName(ar.mavenVisitor);

		try (final Db dbi = new Db(ar.mavenIndex);
				final Db db = new Db(ar.mavenInode);
				MavenVisitor mv = (MavenVisitor) cls.newInstance()) {
			final ResultSet rs = dbi.select(ar.query);

			int n = 0;
			while (rs.next()) {
				final long coordid = rs.getLong("coordid");

				final ResultSet fs = db.select("select * from file where coordid = ?", coordid);

				n++;

				while (fs.next()) {
					final String filename = fs.getString("filename");

					if (filename.endsWith(".class")) {
						try {
							final InputStream cdata = fs.getBinaryStream("cdata");
							final InflaterInputStream iis = new InflaterInputStream(cdata);

							ClassReader cr = new ClassReader(iis);
							ClassVisitor v = mv.visitClass();
							cr.accept(v, 0);
						} catch (Exception e) {
							log.info("Exception: %s", e);
						}
					}
				}
			}
			log.info("No. jar files: %d", n);
		}
	}
}
