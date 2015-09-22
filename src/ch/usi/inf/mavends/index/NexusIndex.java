package ch.usi.inf.mavends.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 * Iterator interface to parse a Nexus Maven Repository Index.
 * 
 * @author Luis Mastrangelo
 *
 */
final class NexusIndex implements AutoCloseable {

	private final RandomAccessFile raf;
	private final FileChannel fc;
	private final MappedByteBuffer mbb;

	final int headb;

	final Date creationDate;

	/**
	 * Creates a new parser with the specified path. The indexPath must be a
	 * valid Nexus Index.
	 * 
	 * @param indexPath
	 *            Path to the Nexus Index to parse.
	 * @throws IOException
	 */
	NexusIndex(String indexPath) throws IOException {
		raf = new RandomAccessFile(indexPath, "r");
		fc = raf.getChannel();
		mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

		headb = mbb.get();
		creationDate = new Date(mbb.getLong());
	}

	boolean hasNext() {
		return mbb.hasRemaining();
	}

	NexusRecord next() {
		final int fieldCount = mbb.getInt();
		final NexusRecord nr = new NexusRecord(fieldCount);

		for (int i = 0; i < fieldCount; i++) {
			mbb.get();

			int keyLen = mbb.getShort();
			byte[] key = new byte[keyLen];
			mbb.get(key);

			int valueLen = mbb.getInt();
			byte[] value = new byte[valueLen];
			mbb.get(value);

			nr.put(i, key, value);
		}

		return nr;
	}

	@Override
	public void close() throws IOException {
		fc.close();
		raf.close();
	}
}
