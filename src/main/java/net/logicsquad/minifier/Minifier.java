package net.logicsquad.minifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A web resource minifier: a function from the text of a resource, such as a
 * file, to its minified text. A {@code Minifier} keeps no state between calls,
 * so a single instance can minify any number of resources, from any number of
 * threads.
 *
 * @author paulh
 */
@FunctionalInterface
public interface Minifier {
	/**
	 * Returns {@code source}, minified.
	 *
	 * @param source the text of a web resource
	 * @return {@code source}, minified
	 * @throws MinificationException if {@code source} can't be minified
	 */
	String minify(String source) throws MinificationException;

	/**
	 * Reads a web resource from {@code reader}, and writes it to {@code writer},
	 * minified. Nothing is written unless minification succeeds, and neither stream
	 * is closed.
	 *
	 * @param reader a {@link Reader} supplying the resource
	 * @param writer a {@link Writer} for the minified resource
	 * @throws IOException           if {@code reader} or {@code writer} throws one
	 * @throws MinificationException if the resource can't be minified
	 */
	default void minify(Reader reader, Writer writer) throws IOException, MinificationException {
		StringBuilder source = new StringBuilder();
		char[] buffer = new char[8192];
		for (int n; (n = reader.read(buffer)) != -1;) {
			source.append(buffer, 0, n);
		}
		writer.write(minify(source.toString()));
	}
}
