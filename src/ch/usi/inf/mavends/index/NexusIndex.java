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

	public final int headb;

	public final Date creationDate;

	/**
	 * Creates a new parser with the specified path. The indexPath must be a
	 * valid Nexus Index.
	 * 
	 * @param indexPath
	 *            Path to the Nexus Index to parse.
	 * @throws IOException
	 */
	public NexusIndex(String indexPath) throws IOException {
		raf = new RandomAccessFile(indexPath, "r");
		fc = raf.getChannel();
		mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

		headb = mbb.get();
		creationDate = new Date(mbb.getLong());
	}

	public boolean hasNext() {
		return mbb.hasRemaining();
	}

	public NexusRecord next() {
		int fieldCount = mbb.getInt();
		NexusRecord nr = new NexusRecord();

		for (int i = 0; i < fieldCount; i++) {
			mbb.get();

			String key = getString(mbb.getShort());
			String value = getString(mbb.getInt());
			nr.put(key, value);
		}

		return nr;
	}

	@Override
	public void close() throws IOException {
		fc.close();
		raf.close();
	}

	private String getString(int length) {
		byte[] buffer = new byte[length];
		mbb.get(buffer);
		return new String(buffer);
	}
}
