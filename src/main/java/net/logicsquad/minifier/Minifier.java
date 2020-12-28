package net.logicsquad.minifier;

import java.io.Writer;

/**
 * A web resource minifier. A {@code Minifier} should be constructed to
 * represent a single web resource requiring minification, such as a file. Each
 * call to {@link #minify(Writer)} will minify that resource to the
 * {@link Writer} supplied.
 *
 * @author paulh
 */
public interface Minifier {
	/**
	 * Minifies the web resource represented by this object, and writes out the
	 * result to {@code writer}.
	 *
	 * @param writer a {@link Writer}
	 */
	void minify(Writer writer);
}
