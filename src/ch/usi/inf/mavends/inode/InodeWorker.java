package ch.usi.inf.mavends.inode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.util.log.Log;

abstract class InodeWorker extends Thread {

	private static final Log log = new Log(System.out);

	private static class Entry {
		final long coordid;
		final String path;

		Entry(long coordid, String path) {
			this.coordid = coordid;
			this.path = path;
		}
	}

	private final List<Entry> queue = new LinkedList<Entry>();
	private final byte[] buffer = new byte[8192];

	private final String repoDir;
	private final MessageDigest md;

	InodeWorker(String repoDir) throws NoSuchAlgorithmException {
		this.repoDir = repoDir;
		this.md = MessageDigest.getInstance("SHA-1");
	}

	void add(long coordid, String path) {
		queue.add(new Entry(coordid, path));
	}

	int size() {
		return queue.size();
	}

	@Override
	public void run() {
		for (final Iterator<Entry> it = queue.iterator(); it.hasNext();) {
			final Entry entry = it.next();

			process(entry.coordid, repoDir + "/" + entry.path);

			it.remove();
		}
	}

	private void process(long coordid, String path) {
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
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

				final String sha1 = byteArray2Hex(md.digest());

				processEntry(coordid, filename, ze.getSize(), ze.getCompressedSize(), ze.getCrc(), sha1, cdata);
			}

			processJar();
		} catch (IOException | SQLException e) {
			log.info("Exception in %s (# %d): %s", path, 0, e);
		}
	}

	/**
	 * 
	 * @param coordid
	 * @param filename
	 * @param size
	 * @param compressedSize
	 * @param crc
	 * @param sha1
	 * @param cdata
	 * @throws IOException
	 * @throws SQLException
	 */
	abstract void processEntry(long coordid, String filename, long size, long compressedSize, long crc, String sha1,
			byte[] cdata) throws IOException, SQLException;

	/**
	 * @throws SQLException
	 * 
	 */
	abstract void processJar() throws SQLException;

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
}
