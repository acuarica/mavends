package ch.usi.inf.mavends.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Iterator interface to parse a Nexus Maven Repository Index.
 * 
 * @author Luis Mastrangelo
 *
 */
public class NexusIndex implements AutoCloseable {

	private RandomAccessFile raf;
	private FileChannel fc;
	private MappedByteBuffer mbb;

	public final byte headb;

	public final long headl;

	public long nrecs = 0;

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
		headl = mbb.getLong();
	}

	public boolean hasNext() {
		return mbb.hasRemaining();
	}

	public NexusRecord next() {
		nrecs++;

		int fieldCount = mbb.getInt();
		NexusRecord nr = new NexusRecord(fieldCount);

		for (int i = 0; i < fieldCount; i++) {
			mbb.get();

			byte[] key = new byte[mbb.getShort()];
			mbb.get(key);

			byte[] value = new byte[mbb.getInt()];
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
