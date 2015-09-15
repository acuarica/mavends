package ch.usi.inf.mavends.util.args;

import java.lang.reflect.Field;

import ch.usi.inf.mavends.util.log.Log;

/**
 * The ArgsParser class provides a way to parser command-line arguments.
 * 
 * @author Luis Mastrangelo
 *
 */
public class ArgsParser {

	private static final Log log = new Log(System.out);

	private static <T> void showField(T ar, String name, Field f) throws IllegalArgumentException,
			IllegalAccessException {
		if (f.getType() == String[].class) {
			String parts[] = (String[]) f.get(ar);

			log.info("Using %d %s:", parts.length, name);

			for (String value : parts) {
				log.info("  * %s", value);
			}
		} else {
			log.info("%s: %s", name, f.get(ar));
		}
	}

	private static <T> void show(T ar) throws IllegalArgumentException, IllegalAccessException {

		for (Field f : ar.getClass().getFields()) {
			Arg arg = f.getAnnotation(Arg.class);

			if (arg != null) {
				showField(ar, arg.name(), f);
			}
		}
	}

	private static <T> String getUsage(Class<T> cls) {

		String usage = "Usage:\n";
		for (Field f : cls.getFields()) {
			Arg arg = f.getAnnotation(Arg.class);

			if (arg != null) {
				usage += String.format("  --%s: %s\n", arg.key(), arg.desc());
			}
		}

		return usage;
	}

	private static String match(Arg arg, String param) {
		int i = param.indexOf("=");
		if (i >= 0) {
			String key = param.substring(0, i);
			String value = param.substring(i + 1);
			return key.equals("--" + arg.key()) ? value : null;
		} else {
			return null;
		}
	}

	private static <T> void setField(T result, Field f, String value) throws IllegalArgumentException,
			IllegalAccessException {
		if (f.getType() == String[].class) {
			String parts[] = value.split(",");
			f.set(result, parts);
		} else if (f.getType() == Integer.class) {
			f.set(result, Integer.parseInt(value));
		} else {
			f.set(result, value);
		}
	}

	private static <T> void set(String[] args, Arg arg, T result, Field f) throws IllegalArgumentException,
			IllegalAccessException, ArgumentMissingException {
		for (int i = 0; i < args.length; i++) {
			String value = match(arg, args[i]);
			if (value != null) {
				setField(result, f, value);
				return;
			}
		}

		throw new ArgumentMissingException();
	}

	private static <T> void internalParse(String[] args, T ar) throws IllegalArgumentException, IllegalAccessException,
			ArgumentMissingException {
		for (Field f : ar.getClass().getFields()) {
			Arg arg = f.getAnnotation(Arg.class);

			if (arg != null) {
				set(args, arg, ar, f);
			}
		}
	}

	public static <T> T parse(String[] args, T ar) throws IllegalArgumentException, IllegalAccessException {
		try {
			internalParse(args, ar);
			show(ar);
			return ar;
		} catch (ArgumentMissingException e) {
			String usage = getUsage(ar.getClass());
			System.out.println(usage);

			System.exit(1);

			return null;
		}
	}
}
