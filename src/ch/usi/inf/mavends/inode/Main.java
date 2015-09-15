package ch.usi.inf.mavends.inode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Inserter;
import ch.usi.inf.mavends.util.log.Log;

public final class Main {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index DB.")
		public String mavenIndex;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "URI list", desc = "Specifies the output uri list file (*aria2* format).")
		public String query;

		@Arg(key = "maveninode", name = "Maven FS DB path", desc = "Specifies the path of the output db file.")
		public String mavenInode;

	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException,
			NoSuchAlgorithmException, IOException, SQLException {
		final Args ar = ArgsParser.parse(args, new Args());

		final MessageDigest md = MessageDigest.getInstance("SHA-1");
		final byte[] buffer = new byte[8192];

		try (final Db dbi = new Db(ar.mavenIndex); final Db db = new Db(ar.mavenInode)) {
			final ResultSet rs = dbi.select(ar.query);

			int n = 0;

			final Inserter ins = db
					.createInserter("insert into file (coordid, filename, originalsize, compressedsize, crc32, sha1, cdata) values (?,?,?,?,?,?,?)");

			while (rs.next()) {
				final String coordid = rs.getString("coordid");
				final String path = rs.getString("path");

				n++;

				try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(ar.repoDir + "/" + path));
						ZipInputStream zip = new ZipInputStream(bis)) {

					ZipEntry ze;
					while ((ze = zip.getNextEntry()) != null) {
						if (ze.isDirectory()) {
							continue;
						}

						final String filename = ze.getName();

						final byte[] cdata;

						int len = 0;

						if (filename.endsWith(".class")) {
							final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
							final DeflaterOutputStream dos = new DeflaterOutputStream(baos);

							while ((len = zip.read(buffer)) > 0) {
								md.update(buffer, 0, len);
								dos.write(buffer, 0, len);
							}

							dos.finish();

							cdata = baos.toByteArray();
						} else {
							while ((len = zip.read(buffer)) > 0) {
								md.update(buffer, 0, len);
							}

							cdata = null;
						}

						final String sha1 = Helper.byteArray2Hex(md.digest());

						ins.insert(coordid, filename, ze.getSize(), ze.getCompressedSize(), ze.getCrc(), sha1, cdata);
					}

					db.commit();

				} catch (IOException e) {
					log.info("Exception in %s (# %d): %s", path, n, e);
				}
			}

			log.info("No. jar files: %d", n);
		}
	}
}
