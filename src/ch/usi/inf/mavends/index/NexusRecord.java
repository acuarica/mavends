package ch.usi.inf.mavends.index;

import java.util.Arrays;

/**
 * Represents a Nexus Record within a Nexus Index. A record is a map from keys
 * (String) to a value (also String).
 * 
 * @author Luis Mastrangelo
 *
 */
public class NexusRecord {

	/**
	 * Map to hold the key/value pairs.
	 */

	private static class Entry {
		final byte[] key;
		final byte[] value;

		Entry(byte[] key, byte[] value) {
			this.key = key;
			this.value = value;
		}
	}

	private final Entry[] values;

	public NexusRecord(int fieldCount) {
		values = new Entry[fieldCount];
	}

	/**
	 * Given a key, retrieves the associated value in the record.
	 * 
	 * @param key
	 *            The key to look for
	 * @return The associated value to the given key.
	 */
	public byte[] get(byte[] key) {
		for (Entry entry : values) {
			if (Arrays.equals(key, entry.key)) {
				return entry.value;
			}
		}

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
		values[index] = new Entry(key, value);
	}

	@Override
	public String toString() {
		String res = "";
		for (Entry entry : values) {
			res += entry.key + "=" + entry.value + " ";
		}

		return res;
	}
}
