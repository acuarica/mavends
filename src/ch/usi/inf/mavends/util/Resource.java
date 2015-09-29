package ch.usi.inf.mavends.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Load resources from CLASSPATH.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Resource {

	/**
	 * Loads a resources from current class loader CLASSPATH.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String get(String path) throws IOException {
		ClassLoader cl = Resource.class.getClassLoader();
		InputStream in = cl.getResourceAsStream(path);

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null) {
			out.append(line + "\n");
		}

		return out.toString();
	}
}
