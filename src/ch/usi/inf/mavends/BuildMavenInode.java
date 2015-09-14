package ch.usi.inf.mavends;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.index.MavenRecord;
import ch.usi.inf.mavends.util.Log;
import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;

public class BuildMavenInode {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

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

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException,
			NoSuchAlgorithmException, IOException {
		Args ar = ArgsParser.parse(args, new Args());

		final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] buffer = new byte[8192];
		ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);

		long total = 0;
		// try (
		// Db db = new Db(ar.mavenInodePath)
		// )
		{
			// Inserter ins = db
			// .createInserter("insert into file (coorid, filename, originalsize, compressedsize, crc32, sha1, data) values (?,?,?,?,?,?,?)");

			int n = 0;
			String line;
			while ((line = stdin.readLine()) != null) {
				final String[] parts = line.split("\\|");
				final String groupid = parts[1];
				final String artifactid = parts[2];
				final String version = parts[3];
				final String classifier = parts[4].equals("") ? null : parts[4];
				final String extension = parts[5];

				n++;
				final String path = MavenRecord.getPath(groupid, artifactid, version, classifier, extension);

				try (FileInputStream fis = new FileInputStream(ar.repoDir + "/" + path);
						BufferedInputStream bis = new BufferedInputStream(fis);
						ZipInputStream zip = new ZipInputStream(bis)) {

					ZipEntry ze;
					while ((ze = zip.getNextEntry()) != null) {

						int size = (int) ze.getSize();

						stream.reset();
						int len = 0;
						while ((len = zip.read(buffer)) > 0) {
							stream.write(buffer, 0, len);
						}

						byte[] data = stream.toByteArray();

						// String sha1 =null;// byteArray2Hex(md.digest(data));

						// byte[] cdata = ze.getName().endsWith(".class") ?
						// compress(data) : null;

						// ins.insert(coorid, ze.getName(), ze.getSize(),
						// ze.getCompressedSize(), ze.getCrc(), sha1, data);
						zip.closeEntry();
					}
				} catch (IOException e) {
					log.info("Exception in %s (# %d): %s", path, n, e);
				}

				// db.conn.commit();
			}

			log.info("No. jar files: %d", n);
			log.info("Total size: %,d", total);
		}
	}
}
