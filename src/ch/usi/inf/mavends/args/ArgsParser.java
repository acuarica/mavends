package ch.usi.inf.mavends.args;

import java.lang.reflect.Field;

import ch.usi.inf.mavends.util.Log;

/**
 * The ArgsParser class provides a way to parser command-line arguments.
 * 
 * @author Luis Mastrangelo
 *
 */
public class ArgsParser {

	private static final Log log = new Log(System.out);

	/**
	 * Exception internally thrown when some argument is misssing.
	 * 
	 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
	 *
	 */
	private static class ArgumentMissingException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5053354343992245633L;

	}

	private static <T> void show(T ar) throws InstantiationException,
			IllegalAccessException {

		for (Field f : ar.getClass().getFields()) {
			Arg arg = f.getAnnotation(Arg.class);

			if (arg != null) {
				showField(ar, arg.name(), f);
			}
		}
	}

	private static <T> void showField(T ar, String name, Field f)
			throws IllegalArgumentException, IllegalAccessException {
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

	private static <T> String getUsage(Class<T> cls)
			throws InstantiationException, IllegalAccessException {

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

	private static <T> void setField(T result, Field f, String value)
			throws IllegalArgumentException, IllegalAccessException {
		if (f.getType() == String[].class) {
			String parts[] = value.split(",");
			f.set(result, parts);
		} else if (f.getType() == Integer.class) {
			f.set(result, Integer.parseInt(value));
		} else {
			f.set(result, value);
		}
	}

	private static <T> void set(String[] args, Arg arg, T result, Field f)
			throws IllegalArgumentException, IllegalAccessException,
			ArgumentMissingException {
		for (int i = 0; i < args.length; i++) {
			String value = match(arg, args[i]);
			if (value != null) {
				setField(result, f, value);
				return;
			}
		}

		throw new ArgumentMissingException();
	}

	private static <T> T internalParse(String[] args, Class<T> cls)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, ArgumentMissingException {
		T result = cls.newInstance();

		for (Field f : cls.getFields()) {
			Arg arg = f.getAnnotation(Arg.class);

			if (arg != null) {
				set(args, arg, result, f);
			}
		}

		return result;
	}

	public static <T> T parse(String[] args, Class<T> cls)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException {
		try {
			T ar = internalParse(args, cls);
			show(ar);
			return ar;
		} catch (ArgumentMissingException e) {
			String usage = getUsage(cls);
			System.out.println(usage);

			System.exit(1);

			return null;
		}
	}
}
