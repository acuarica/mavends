package ch.usi.inf.mavends.index;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a Nexus Record within a Nexus Index. A record is a map from keys
 * to a value.
 * 
 * @author Luis Mastrangelo
 *
 */
final class NexusRecord {

	private final Map<String, String> values = new HashMap<String, String>();

	/**
	 * Given a key, retrieves the associated value in the record.
	 * 
	 * @param key
	 *            The key to look for
	 * @return The associated value to the given key.
	 */
	public String get(String key) {
		return values.get(key);
	}

	/**
	 * Puts a new pair inside the record.
	 * 
	 * @param key
	 *            The key of the new record.
	 * @param value
	 *            The value associated to the given key.
	 */
	public void put(String key, String value) {
		values.put(key, value);
	}

	@Override
	public String toString() {
		String res = "";
		for (Entry<String, String> entry : values.entrySet()) {
			res += entry.getKey() + "=" + entry.getValue() + " ";
		}

		return res;
	}
}
