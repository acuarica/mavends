package ch.usi.inf.mavends;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.args.Arg;
import ch.usi.inf.mavends.args.ArgsParser;
import ch.usi.inf.mavends.db.Db;
import ch.usi.inf.mavends.db.Inserter;
import ch.usi.inf.mavends.util.Log;

public class BuildMavenFs {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "mavenindex", name = "Maven Index path", desc = "Specifies the path of the Maven Index.")
		public String mavenIndexPath;

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "query", name = "Filter query", desc = "Specifies the path of the Maven repository.")
		public String query;

		@Arg(key = "mavenfs", name = "Maven FS DB path", desc = "Specifies the path of the output db file.")
		public String mavenFsPath;

	}

	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			ClassNotFoundException, SQLException, IOException {

		Args ar = ArgsParser.parse(args, Args.class);

		ResultSet rs = new Db(ar.mavenIndexPath)
				.select("select coorid, (select path from artifact_view a where a.coorid = t.coorid) as path from ("
						+ ar.query + ") t");

		Db db = new Db(ar.mavenFsPath);

		db.conn.setAutoCommit(false);

		int n = 0;
		while (rs.next()) {
			String coorid = rs.getString("coorid");
			String path = rs.getString("path");

			Inserter ins = db
					.createInserter("insert into file (coorid, filename, originalsize, compressedsize, crc32, sha1, data) values (?,?,?,?,?,?,?)");

			try {

				for (JarEntry je : new JarIterator(ar.repoDir + "/" + path)) {
					byte[] data = je.name.endsWith(".class") ? je.data : null;

					ins.insert(coorid, je.name, je.size, je.compressedSize,
							je.crc32, je.sha1, data);
				}

			} catch (Exception e) {
				log.info("Exception analysing %s...", path);
				e.printStackTrace();
			}

			db.conn.commit();

			n++;

			if (n % 100 == 0) {
				log.info("%d jars", n);
			}
		}

		log.info("No. jar files: %d", n);
	}

	private static class JarEntry {
		public final String name;
		public final long size;
		public final long compressedSize;
		public final long crc32;
		public final String sha1;
		public final byte[] data;

		public JarEntry(String name, long size, long compressedSize,
				long crc32, String sha1, byte[] data) {
			this.name = name;
			this.size = size;
			this.compressedSize = compressedSize;
			this.crc32 = crc32;
			this.sha1 = sha1;
			this.data = data;
		}
	}

	private static class JarIterator implements Iterable<JarEntry> {

		private static final MessageDigest md;

		private final ZipInputStream zip;
		private ZipEntry entry;

		static {
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		public JarIterator(String jarFileName) throws IOException {
			byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

			zip = new ZipInputStream(new ByteArrayInputStream(jarFileBuffer));
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

		@Override
		public Iterator<JarEntry> iterator() {
			return new Iterator<JarEntry>() {
				@Override
				public boolean hasNext() {
					try {
						entry = zip.getNextEntry();
						return entry != null;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public JarEntry next() {
					try {
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						byte[] buffer = new byte[4096];

						int len = 0;
						while ((len = zip.read(buffer)) > 0) {
							stream.write(buffer, 0, len);
						}

						byte[] data = stream.toByteArray();
						String sha1 = byteArray2Hex(md.digest(data));

						return new JarEntry(entry.getName(), entry.getSize(),
								entry.getCompressedSize(), entry.getCrc(),
								sha1, data);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}
