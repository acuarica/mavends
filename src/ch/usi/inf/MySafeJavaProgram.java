package ch.usi.inf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sun.misc.Unsafe;

public class MySafeJavaProgram {

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor();
		c.setAccessible(true);
		Unsafe unsafe = c.newInstance();

		unsafe.getLong((long) 1);
	}
}
