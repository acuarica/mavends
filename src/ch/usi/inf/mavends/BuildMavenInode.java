package ch.usi.inf.mavends;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.db.Db;
import ch.usi.inf.mavends.util.db.Inserter;

public class BuildMavenInode {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index.")
		public String mavenIndexPath;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "Filter query", desc = "Specifies the path of the Maven repository.")
		public String query;

		@Arg(key = "maveninode", name = "Maven FS DB path", desc = "Specifies the path of the output db file.")
		public String mavenInodePath;

	}

	/**
	 * Return the String representation of the hash byte array.
	 * 
	 * @param hash
	 *            The byte array to transform.
	 * @return The hexadecimal String representation of hash.
	 */
	private static String byteArray2Hex(byte[] hash) {
		try (Formatter f = new Formatter()) {
			for (byte b : hash) {
				f.format("%02x", b);
			}

			return f.toString();
		}
	}

	public static byte[] compress(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer); // returns the generated
													// code... index
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();
		return output;
	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException, SQLException, NoSuchAlgorithmException {
		Args ar = ArgsParser.parse(args, new Args());

		try (Db dbi = new Db(ar.mavenIndexPath);
				ResultSet rs = dbi
						.select("select coorid, (select path from artifact_view a where a.coorid = t.coorid) as path from ("
								+ ar.query + ") t");
				Db db = new Db(ar.mavenInodePath)) {

			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] buffer = new byte[8192];
			ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);

			int n = 0;
			while (rs.next()) {
				String coorid = rs.getString("coorid");
				String path = rs.getString("path");

				try (Inserter ins = db
						.createInserter("insert into file (coorid, filename, originalsize, compressedsize, crc32, sha1, data) values (?,?,?,?,?,?,?)");
						FileInputStream fis = new FileInputStream(ar.repoDir + "/" + path);
						BufferedInputStream bis = new BufferedInputStream(fis);
						ZipInputStream zip = new ZipInputStream(bis)) {

					ZipEntry ze = zip.getNextEntry();
					while ((ze = zip.getNextEntry()) != null) {
						stream.reset();
						int len = 0;
						while ((len = zip.read(buffer)) > 0) {
							stream.write(buffer, 0, len);
						}

						byte[] data = stream.toByteArray();
						String sha1 = byteArray2Hex(md.digest(data));

						byte[] cdata = ze.getName().endsWith(".class") ? compress(data) : null;

						ins.insert(coorid, ze.getName(), ze.getSize(), ze.getCompressedSize(), ze.getCrc(), sha1, cdata);
					}
				} catch (IOException e) {
					log.info("Exception in %s (# %d): %s", path, n, e);
				}

				db.conn.commit();

				n++;
			}

			log.info("No. jar files: %d", n);
		}
	}
}
