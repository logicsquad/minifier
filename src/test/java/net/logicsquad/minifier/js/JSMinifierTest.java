package net.logicsquad.minifier.js;

import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Test;

import net.logicsquad.minifier.AbstractMinifierTest;
import net.logicsquad.minifier.Minifier;

/**
 * Unit tests on {@link JSMinifier} class.
 *
 * @author paulh
 */
public class JSMinifierTest extends AbstractMinifierTest {
	/**
	 * Count of input/output resources
	 */
	private static final int RESOURCE_COUNT = 24;

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

	@Test
	public void unterminatedCommentThrowsException() throws IOException {
		throwsOnMinify("exceptions/unterminated-comment.js", JSMinifier.UnterminatedCommentException.class);
		return;
	}

	@Override
	protected int resourceCount() {
		return RESOURCE_COUNT;
	}
}
