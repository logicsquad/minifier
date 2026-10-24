package net.logicsquad.minifier.js;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

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
	protected Minifier minifier() {
		return new JSMinifier();
	}

	@Test
	public void unterminatedCommentThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-comment.js", "Unterminated comment", 1, 1);
		return;
	}

	@Test
	public void unterminatedStringLiteralThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-string.js", "Unterminated string literal", 2, 9);
		return;
	}

	@Test
	public void unterminatedRegExpLiteralThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-regexp.js", "Unterminated regular expression literal", 2, 10);
		return;
	}

	@Test
	public void unterminatedRegExpLiteralClassThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-regexp-class.js", "Unterminated regular expression literal", 2, 10);
		return;
	}

	@Test
	public void unterminatedTemplateLiteralThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-template.js", "Unterminated string literal", 2, 9);
		return;
	}

	@Test
	public void unterminatedSubstitutionThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-substitution.js", "Unterminated string literal", 2, 9);
		return;
	}

	/**
	 * Tests that "\r\n" and "\r" each count as one line break in where a problem
	 * is reported.
	 */
	@Test
	public void otherLineBreaksCountOnceInWhereProblemIs() {
		throwsOnMinifySource("var a = 1;\r\n\rvar s = 'abc\r\n;", "Unterminated string literal", 3, 9);
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
		assertEquals("s='abc\\\ndef';", new JSMinifier().minify("s = 'abc\\\ndef';"));
		assertEquals("s='abc\\\ndef';", new JSMinifier().minify("s = 'abc\\\r\ndef';"));
		assertEquals("s='abc\\\ndef';", new JSMinifier().minify("s = 'abc\\\rdef';"));
	}

	/**
	 * Tests that input ending in "/" gains nothing at the end. Looking past the
	 * end of the input once added U+FFFF to the output.
	 *
	 * @throws MinificationException if minification fails
	 */
	@Test
	public void trailingSlashGainsNothing() throws MinificationException {
		assertEquals("a=b/", new JSMinifier().minify("a = b /"));
		assertEquals("if(x)/re/", new JSMinifier().minify("if (x) /re/"));
	}

	/**
	 * Tests that the output has no line break at either end: JSMin outputs one in
	 * front of the first token.
	 *
	 * @throws MinificationException if minification fails
	 */
	@Test
	public void noLineBreakAtEitherEnd() throws MinificationException {
		assertEquals("var a=1;", new JSMinifier().minify("var a = 1;\n"));
		assertEquals("a\nb", new JSMinifier().minify("\n\na\nb\n\n"));
	}
}
