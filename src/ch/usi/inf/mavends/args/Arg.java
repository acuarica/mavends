package ch.usi.inf.mavends.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that a given field is target of an command line
 * argument.
 * 
 * @author Luis Mastrangelo
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Arg {

	/**
	 * The key for this argument, used in scripts to self-document.
	 * 
	 * @return The key of this argument.
	 */
	public String key();

	/**
	 * The name is used to identify this argument.
	 * 
	 * @return The name of this argument.
	 */
	public String name();

	/**
	 * The desc is used to provide some help text to the user to indicate what
	 * is the purpose of the argument.
	 * 
	 * @return The description of this argument.
	 */
	public String desc();

}
