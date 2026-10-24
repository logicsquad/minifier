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
		throwsOnMinify("exceptions/unterminated-comment.js", JSMinifier.UnterminatedCommentException.class);
		return;
	}

	/**
	 * Tests that input ending in "/" gains nothing at the end. Looking past the
	 * end of the input once added U+FFFF to the output.
	 *
	 * @throws MinificationException if minification fails
	 */
	@Test
	public void trailingSlashGainsNothing() throws MinificationException {
		assertEquals("a=b/", new JSMinifier().minify("a = b /").trim());
	}
}
