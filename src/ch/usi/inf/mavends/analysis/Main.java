package ch.usi.inf.mavends.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.InflaterInputStream;

import org.objectweb.asm.ClassReader;

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

		@Arg(key = "maveninode", name = "Maven FS DB path", desc = "Specifies the path of the output db file.")
		public String mavenInode;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException,
			IOException {
		final Args ar = ArgsParser.parse(args, new Args());

		try (final Db dbi = new Db(ar.mavenIndex); final Db db = new Db(ar.mavenInode)) {
			final ResultSet rs = dbi.select(ar.query);

			int n = 0;
			while (rs.next()) {
				final long coordid = rs.getLong("coordid");

				final ResultSet fs = db.select("select * from file where coordid = ?", coordid);

				n++;

				while (fs.next()) {
					final String filename = fs.getString("filename");

					if (filename.endsWith(".class")) {
						final InputStream cdata = fs.getBinaryStream("cdata");
						final InflaterInputStream iis = new InflaterInputStream(cdata);

						ClassReader cr = new ClassReader(iis);
						StatsVisitor v = new StatsVisitor();
						cr.accept(v, 0);
					}
				}
			}

			log.info("No. jar files: %d", n);
			log.info("No classes: %,d", StatsVisitor.noclasses);
			log.info("No methods: %,d", StatsVisitor.nomethods);
			log.info("No callsites: %,d", StatsVisitor.nocallsites);
			log.info("No field uses: %,d", StatsVisitor.nofielduses);
			log.info("No literal: %,d", StatsVisitor.noliteral);
		}
	}
}
