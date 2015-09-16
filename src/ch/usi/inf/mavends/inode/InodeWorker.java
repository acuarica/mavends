package ch.usi.inf.mavends.inode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.usi.inf.mavends.util.log.Log;

final class InodeWorker extends Thread {

	private static final Log log = new Log(System.out);

	private static class Entry {

		final long coordid;
		final String path;

		Entry(long coordid, String path) {
			this.coordid = coordid;
			this.path = path;
		}
	}

	static class ReadyEntry {

		final long coordid;
		final String filename;
		final long size;
		final long compressedSize;
		final long crc;
		final String sha1;
		final byte[] cdata;

		ReadyEntry(long coordid, String filename, long size, long compressedSize, long crc, String sha1, byte[] cdata) {
			this.coordid = coordid;
			this.filename = filename;
			this.size = size;
			this.compressedSize = compressedSize;
			this.crc = crc;
			this.sha1 = sha1;
			this.cdata = cdata;
		}
	}

	private final String repoDir;
	private final MessageDigest md;

	volatile List<Entry> queue = new ArrayList<Entry>();
	volatile List<ReadyEntry> ready = Collections.synchronizedList(new ArrayList<ReadyEntry>());

	private final byte[] buffer = new byte[8192];

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
		for (Entry entry : queue) {
			process(entry.coordid, repoDir + "/" + entry.path);
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

					final String sha1 = byteArray2Hex(md.digest());

					// try (BufferedOutputStream fos = new
					// BufferedOutputStream(new FileOutputStream("out/inode/" +
					// sha1))) {
					try (FileOutputStream fos = new FileOutputStream("out/inode/" + sha1)) {
						fos.write(cdata);
					}
				} else {
					while ((len = zip.read(buffer)) > 0) {
						md.update(buffer, 0, len);
					}

					cdata = null;
				}

				// fos.w
				// synchronized (ready) {
				// ready.add(new ReadyEntry(coordid, filename, ze.getSize(),
				// ze.getCompressedSize(),
				// ze.getCrc(), sha1, cdata));
				// }
			}
		} catch (IOException e) {
			log.info("Exception in %s (# %d): %s", path, 0, e);
		}
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
}
