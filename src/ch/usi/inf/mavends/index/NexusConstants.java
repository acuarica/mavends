package ch.usi.inf.mavends.index;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class NexusConstants {

	public static final byte BAR = "|".getBytes()[0];

	public static final byte[] CRLF = "\n".getBytes();

	public static final int BUFFER_SIZE = 1024 * 16;

	public static byte[][] split(byte[] value, int length) {
		byte[][] res = new byte[length][];

		int prev = 0;
		int index = 0;
		for (int i = 0; i < value.length; i++) {
			if (value[i] == BAR) {
				res[index] = Arrays.copyOfRange(value, prev, i);
				index++;
				prev = i + 1;
			}
		}

		res[index] = Arrays.copyOfRange(value, prev, value.length);

		return res;
	}

	public static ArrayList<byte[]> split(byte[] value) {
		ArrayList<byte[]> res = new ArrayList<byte[]>(128);

		int prev = 0;
		for (int i = 0; i < value.length; i++) {
			if (value[i] == BAR) {
				res.add(Arrays.copyOfRange(value, prev, i));
				prev = i + 1;
			}
		}

		res.add(Arrays.copyOfRange(value, prev, value.length));

		return res;
	}

	public static void write(OutputStream os, byte[]... args) throws IOException {
		os.write(args[0]);
		for (int i = 1; i < args.length; i++) {
			os.write(BAR);
			os.write(args[i]);
		}
		os.write(CRLF);
	}

	public static void write(OutputStream os, Object... args) throws IOException {
		byte[][] values = new byte[args.length][];
		for (int i = 0; i < args.length; i++) {
			values[i] = args[i] == null ? new byte[0] : args[i].toString().getBytes();
		}

		write(os, values);
	}
}
