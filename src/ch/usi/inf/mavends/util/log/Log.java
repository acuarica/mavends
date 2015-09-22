package ch.usi.inf.mavends.util.log;

import java.io.PrintStream;

/**
 * Simple wrapper around a PrintStream to serve as a log.
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Log {

	/**
	 * The PrintStream to log messages.
	 */
	private final PrintStream out;

	/**
	 * Creates a Log using the specified PrintStream.
	 * 
	 * @param out
	 *            The PrintStream to use as the back-end for this Log.
	 */
	public Log(PrintStream out) {
		this.out = out;
	}

	/**
	 * Logs the specified message.
	 * 
	 * @param message
	 *            The message to log.
	 * @param args
	 *            Arguments referenced by the format specifiers in the format
	 *            string.
	 */
	public void info(String message, Object... args) {
		out.format(message + "\n", args);
	}
}
