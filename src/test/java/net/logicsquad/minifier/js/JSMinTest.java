package net.logicsquad.minifier.js;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.logicsquad.minifier.AbstractTest;
import net.logicsquad.minifier.js.JSMin.UnterminatedCommentException;
import net.logicsquad.minifier.js.JSMin.UnterminatedRegExpLiteralException;
import net.logicsquad.minifier.js.JSMin.UnterminatedStringLiteralException;

/**
 * Unit tests on {@link JSMin} class.
 * 
 * @author paulh
 */
public class JSMinTest extends AbstractTest {
	/**
	 * Indexes for input/output resources
	 */
	private static final List<String> RESOURCES = Arrays.asList("1", "2");

	/**
	 * Extension for resource files
	 */
	private static final String EXTENSION = "js";

	@Override
	protected String extension() {
		return EXTENSION;
	}

	@Test
	public void actualOutputMatchesExpected() throws IOException, UnterminatedRegExpLiteralException,
			UnterminatedCommentException, UnterminatedStringLiteralException {
		for (String index : RESOURCES) {
			Writer writer = new StringWriter();
			JSMin jsmin = new JSMin(readerForSourceFile(index), writer);
			jsmin.jsmin();
			String expected = stringForExpectedFile(index);
			assertEquals(expected.trim(), writer.toString().trim());
		}
		return;
	}
}
