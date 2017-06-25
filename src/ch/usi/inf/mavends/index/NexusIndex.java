package ch.usi.inf.mavends.index;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Iterator interface to parse a Nexus Maven Repository Index.
 *
 * @author Luis Mastrangelo
 *
 */
final class NexusIndex {

	final int headb;

	final Date creationDate;

	private final DataInputStream dis;

	/**
	 * Creates a new parser with the specified path. The indexPath must be a
	 * valid Nexus Index.
	 *
	 * @param indexPath
	 *            Path to the Nexus Index to parse.
	 * @throws IOException
	 */
	NexusIndex(String indexPath) throws IOException {
        dis = new DataInputStream(new FileInputStream(indexPath));
		headb = dis.readByte();
		creationDate = new Date(dis.readLong());
	}

	boolean hasNext() throws IOException {
	    return dis.available() > 0;
	}

	NexusRecord next() throws IOException {
		final int fieldCount = dis.readInt();
		final NexusRecord nr = new NexusRecord(fieldCount);

		for (int i = 0; i < fieldCount; i++) {
			dis.readByte();

			int keyLen = dis.readShort();
			byte[] key = new byte[keyLen];
			dis.read(key);

			int valueLen = dis.readInt();
			byte[] value = new byte[valueLen];
			dis.read(value);

			nr.put(i, key, value);
		}

		return nr;
	}
}
