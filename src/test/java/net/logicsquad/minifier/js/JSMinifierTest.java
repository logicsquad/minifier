package net.logicsquad.minifier.js;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

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
	 * Indexes for input/output resources
	 */
	private static final List<String> RESOURCES = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09",
			"10", "11", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");

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

	@Override
	protected List<String> resources() {
		return RESOURCES;
	}

	@Test
	public void unterminatedCommentThrowsException() throws IOException {
		throwsOnMinify("12", JSMinifier.UnterminatedCommentException.class);
		return;
	}
}
