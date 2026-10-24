package net.logicsquad.minifier.css;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import net.logicsquad.minifier.AbstractMinifierTest;
import net.logicsquad.minifier.MinificationException;
import net.logicsquad.minifier.Minifier;

/**
 * Unit tests on {@link Minifier} class.
 *
 * @author paulh
 */
public class CSSMinifierTest extends AbstractMinifierTest {
	/**
	 * Extension for resource files
	 */
	private static final String EXTENSION = "css";

	@Override
	protected String extension() {
		return EXTENSION;
	}

	@Override
	protected Minifier minifier() {
		return new CSSMinifier();
	}

	@Test
	public void unterminatedCommentThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-comment.css", "Unterminated comment", 1, 20);
		return;
	}

	@Test
	public void unmatchedBraceThrowsException() throws IOException {
		throwsOnMinify("exceptions/unmatched-brace.css", "Unmatched \"}\"", 2, 1);
		return;
	}

	/**
	 * Tests that "\r\n" and "\r" each count as one line break in where a problem
	 * is reported.
	 */
	@Test
	public void otherLineBreaksCountOnceInWhereProblemIs() {
		throwsOnMinifySource("a { color: red; }\r\n\r}", "Unmatched \"}\"", 3, 1);
		return;
	}

	/**
	 * Tests that the output has no line break at either end. "\r\n" was once
	 * appended to it.
	 *
	 * @throws MinificationException if minification fails
	 */
	@Test
	public void noLineBreakAtEitherEnd() throws MinificationException {
		assertEquals("a{color:red}", new CSSMinifier().minify("\na { color: red; }\n"));
	}
}
