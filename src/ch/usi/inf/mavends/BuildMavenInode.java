package ch.usi.inf.mavends;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.util.args.Arg;
import ch.usi.inf.mavends.util.args.ArgsParser;
import ch.usi.inf.mavends.util.log.Log;

public class BuildMavenInode  {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(key = "repo", name = "Maven Repo", desc = "Specifies the path of the Maven repository.")
		public String repoDir;

		@Arg(key = "inode", name = "Maven FS DB path", desc = "Specifies the path of the output db file.")
		public String inode;

		@Arg(key = "maveninode", name = "Maven FS DB path", desc = "Specifies the path of the output db file.")
		public String mavenInode;

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
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();
		return output;
	}

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException,
			NoSuchAlgorithmException, IOException {
		Args ar = ArgsParser.parse(args, new Args());

		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] buffer = new byte[8192];
		ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);

		final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(ar.mavenInode))) {

			long total = 0;

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
				final String path = NexusConstants.getPath(groupid, artifactid, version, classifier, extension);

				try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(ar.repoDir + "/" + path));
						ZipInputStream zip = new ZipInputStream(bis)) {

					ZipEntry ze;
					while ((ze = zip.getNextEntry()) != null) {

						stream.reset();
						int len = 0;
						while ((len = zip.read(buffer)) > 0) {
							stream.write(buffer, 0, len);
						}

						byte[] data = stream.toByteArray();
						String sha1 = byteArray2Hex(md.digest(data));
						byte[] cdata = compress(data);

						try (BufferedOutputStream shaf = new BufferedOutputStream(new FileOutputStream(ar.inode + "/"
								+ sha1))) {
							shaf.write(cdata);
						}

//						write(os, groupid, artifactid, version, ze.getName(), ze.getSize(), ze.getCompressedSize(),
//								ze.getCrc(), sha1);
					}
				} catch (IOException e) {
					log.info("Exception in %s (# %d): %s", path, n, e);
				}
			}

			log.info("No. jar files: %d", n);
			log.info("Total size: %,d", total);
		}
	}
}
