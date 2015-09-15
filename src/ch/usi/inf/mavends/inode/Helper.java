package ch.usi.inf.mavends.inode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Formatter;
import java.util.zip.Deflater;

final class Helper {

	/**
	 * Return the String representation of the hash byte array.
	 * 
	 * @param hash
	 *            The byte array to transform.
	 * @return The hexadecimal String representation of hash.
	 */
	static String byteArray2Hex(byte[] hash) {
		try (Formatter f = new Formatter()) {
			for (byte b : hash) {
				f.format("%02x", b);
			}

			return f.toString();
		}
	}

//	static byte[] compress(byte[] data) throws IOException {
//		Deflater deflater = new Deflater();
//		deflater.setInput(data);
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
//		deflater.finish();
//		byte[] buffer = new byte[1024];
//		while (!deflater.finished()) {
//			int count = deflater.deflate(buffer);
//			outputStream.write(buffer, 0, count);
//		}
//		outputStream.close();
//		byte[] output = outputStream.toByteArray();
//		return output;
//	}
}
