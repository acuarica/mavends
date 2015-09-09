package ch.usi.inf.mavends.index;

import java.util.Arrays;

/**
 * Represents a Nexus Record within a Nexus Index. A record is a map from keys
 * to a value.
 * 
 * @author Luis Mastrangelo
 *
 */
public class NexusRecord {

	private static class Entry {
		public Entry(byte[] key, byte[] value) {
			this.key = key;
			this.value = value;
		}

		public final byte[] key;
		public final byte[] value;
	}

	 private final byte[][] keys;
	 private final byte[][] values;

	//private final Entry[] values;

	public NexusRecord(int fieldCount) {
		 keys = new byte[fieldCount][];
		 values = new byte[fieldCount][];
	//	values = new Entry[fieldCount];
	}

	/**
	 * Given a key, retrieves the associated value in the record.
	 * 
	 * @param key
	 *            The key to look for
	 * @return The associated value to the given key.
	 */
	public byte[] get(byte[] key) {
		 for (int i = 0; i < keys.length; i++) {
		 if (Arrays.equals(key, keys[i])) {
		 return values[i];
		 }
		 }

//		for (int i = 0; i < values.length; i++) {
//			if (Arrays.equals(key, values[i].key)) {
//				return values[i].value;
//			}
//		}

		return null;
	}

	/**
	 * Puts a new pair inside the record.
	 * 
	 * @param key
	 *            The key of the new record.
	 * @param value
	 *            The value associated to the given key.
	 */
	public void put(int index, byte[] key, byte[] value) {
		 keys[index] = key;
		 values[index] = value;
//		values[index] = new Entry(key, value);
	}

	@Override
	public String toString() {
		String res = "";
//		for (Entry entry : values) {
//			res += new String(entry.key) + "=" + new String(entry.value) + " ";
//		}

		return res;
	}
}
