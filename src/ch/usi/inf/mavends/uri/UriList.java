package ch.usi.inf.mavends.uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

final class UriList implements AutoCloseable {

	private final PrintStream out;
	private String[] mirrors;

	UriList(String uriListPath, String[] mirrors) throws FileNotFoundException {
		this.out = new PrintStream(uriListPath);
		this.mirrors = mirrors;
	}

	void emit(String path) throws IOException {
		for (String mirror : mirrors) {
			out.format("%s/%s\t", mirror, path);
		}

		out.println();
		out.format("\tout=%s", path);
		out.println();
	}

	@Override
	public void close() {
		out.close();
	}
}
