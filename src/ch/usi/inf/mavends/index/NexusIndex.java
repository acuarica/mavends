package ch.usi.inf.mavends.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
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

//    private final int BLOCK_SIZE = Integer.MAX_VALUE;
    private final int BLOCK_SIZE = 10000*1000;

	private final RandomAccessFile raf;
	private final FileChannel fc;
	private MappedByteBuffer mbb = null;

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

		System.out.println(Integer.MAX_VALUE);
		map(0);

		headb = mbb.get();
		creationDate = new Date(mbb.getLong());
	}

	private void map(int position) throws IOException {
	    System.out.println("position: " + position);
	    if (fc.size() - position > BLOCK_SIZE) {
           mbb = fc.map(FileChannel.MapMode.READ_ONLY, position, BLOCK_SIZE);
       } else {
           mbb = fc.map(FileChannel.MapMode.READ_ONLY, position, fc.size() - position);
       }
    }

    private int n = 10000;

	boolean hasNext() {
	    n--;

	    if (n == 0) {
	        return false;
        }

		return mbb.hasRemaining();
	}

	NexusRecord next() throws IOException {
		final int fieldCount = getInt();
		final NexusRecord nr = new NexusRecord(fieldCount);

		for (int i = 0; i < fieldCount; i++) {
			get();

			int keyLen = getShort();
			byte[] key = new byte[keyLen];
			get(key);

			int valueLen = getInt();
			byte[] value = new byte[valueLen];
			get(value);

			nr.put(i, key, value);
		}

		return nr;
	}

	private int getInt() throws IOException {
	   if (mbb.position() + 4 > BLOCK_SIZE) {
           byte[] value = new byte[4];
           get(value);
           return ByteBuffer.wrap(value).getInt();
       }

       return mbb.getInt();
    }

    private int getShort() throws IOException {
        if (mbb.position() + 2 > BLOCK_SIZE) {
            byte[] value = new byte[2];
            get(value);
            return ByteBuffer.wrap(value).getShort();
        }

        return mbb.getShort();
    }

    private byte get() throws IOException {
        if (mbb.position() + 1 > BLOCK_SIZE) {
            byte[] value = new byte[1];
            get(value);
            return ByteBuffer.wrap(value).get();
        }

        return mbb.get();
    }

    private void get(byte[] value) throws IOException {
        if (mbb.position() + value.length > BLOCK_SIZE) {
            System.out.print(mbb.position());
            System.out.print(", ");
            System.out.print(value.length);
            System.out.print(", ");
            System.out.println(BLOCK_SIZE);

            int r = BLOCK_SIZE - mbb.position();
            mbb.get(value, 0, r);
            map(mbb.position());
            mbb.get(value, r, value.length - r);
        }

        mbb.get(value);
    }

	@Override
	public void close() throws IOException {
		fc.close();
		raf.close();
	}
}
