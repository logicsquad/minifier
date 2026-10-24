package net.logicsquad.minifier.js;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import net.logicsquad.minifier.AbstractMinifierTest;
import net.logicsquad.minifier.MinificationException;
import net.logicsquad.minifier.Minifier;

/**
 * Unit tests on {@link JSMinifier} class.
 *
 * @author paulh
 */
public class JSMinifierTest extends AbstractMinifierTest {
	/**
	 * Extension for resource files
	 */
	private static final String EXTENSION = "js";

	@Override
	protected String extension() {
		return EXTENSION;
	}

	@Override
	protected Minifier miniferForReader(Reader reader) {
		return new JSMinifier(reader);
	}

	/**
	 * Returns {@code source}, minified, without the line break that
	 * {@link JSMinifier} outputs in front of the first token.
	 *
	 * @param source a script
	 * @return {@code source}, minified
	 * @throws MinificationException if minification fails
	 */
	private static String minify(String source) throws MinificationException {
		StringWriter writer = new StringWriter();
		new JSMinifier(new StringReader(source)).minify(writer);
		return writer.toString().trim();
	}

	@Test
	public void unterminatedCommentThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-comment.js", JSMinifier.UnterminatedCommentException.class);
		return;
	}

	@Test
	public void unterminatedTemplateLiteralThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-template.js", JSMinifier.UnterminatedStringLiteralException.class);
		return;
	}

	@Test
	public void unterminatedSubstitutionThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-substitution.js", JSMinifier.UnterminatedStringLiteralException.class);
		return;
	}

	/**
	 * Tests that a line continuation in a string works with each kind of line
	 * break: "\r\n" is one line break, not two.
	 *
	 * @throws MinificationException if minification fails
	 */
	@Test
	public void lineContinuationWorksWithEachLineBreak() throws MinificationException {
		assertEquals("s='abc\\\ndef';", minify("s = 'abc\\\ndef';"));
		assertEquals("s='abc\\\ndef';", minify("s = 'abc\\\r\ndef';"));
		assertEquals("s='abc\\\ndef';", minify("s = 'abc\\\rdef';"));
	}
}
