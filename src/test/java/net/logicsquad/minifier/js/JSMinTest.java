package net.logicsquad.minifier.js;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import net.logicsquad.minifier.js.JSMin.UnterminatedCommentException;
import net.logicsquad.minifier.js.JSMin.UnterminatedRegExpLiteralException;
import net.logicsquad.minifier.js.JSMin.UnterminatedStringLiteralException;

/**
 * Unit tests on {@link JSMin} class.
 * 
 * @author paulh
 */
public class JSMinTest {
	/**
	 * Resource directory
	 */
	private static final String RESOURCES_DIR = "src/test/resources";

	/**
	 * Input
	 */
	private static final String INPUT_1 = "input/test-1.js";

	/**
	 * Input
	 */
	private static final String INPUT_2 = "input/test-2.js";

	/**
	 * Expected output
	 */
	private static final String OUTPUT_1 = "output/test-1.js";

	/**
	 * Expected output
	 */
	private static final String OUTPUT_2 = "output/test-2.js";

	@Test
	public void test1() throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
			UnterminatedStringLiteralException {
		assertExpected(OUTPUT_1, INPUT_1);
		return;
	}

	@Test
	public void test2() throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
			UnterminatedStringLiteralException {
		assertExpected(OUTPUT_2, INPUT_2);
		return;
	}

	/**
	 * Minifies Javascript in {@code sourceFile} and compares result to content in
	 * {@code expectedFile}.
	 * 
	 * @param expectedFile filename for expected result
	 * @param sourceFile   filename for source input
	 * @throws IOException                        if any issues loading resources
	 * @throws UnterminatedRegExpLiteralException from {@link JSMin}
	 * @throws UnterminatedCommentException       from {@link JSMin}
	 * @throws UnterminatedStringLiteralException from {@link JSMin}
	 */
	private void assertExpected(String expectedFile, String sourceFile) throws IOException,
			UnterminatedRegExpLiteralException, UnterminatedCommentException, UnterminatedStringLiteralException {
		Reader reader = new InputStreamReader(JSMinTest.class.getClassLoader().getResourceAsStream(sourceFile));
		Writer writer = new StringWriter();
		JSMin jsmin = new JSMin(reader, writer);
		jsmin.jsmin();
		String expected = new String(Files.readAllBytes(Paths.get(RESOURCES_DIR, expectedFile)));
		assertEquals(expected.trim(), writer.toString().trim());
		return;
	}
}
