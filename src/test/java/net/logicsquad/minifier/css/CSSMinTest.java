package net.logicsquad.minifier.css;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link Minifier} class.
 * 
 * @author paulh
 */
public class CSSMinTest {
	/**
	 * Resource directory
	 */
	private static final String RESOURCES_DIR = "src/test/resources";

	/**
	 * Input
	 */
	private static final String INPUT_1 = "input/test-1.css";

	/**
	 * Input
	 */
	private static final String INPUT_2 = "input/test-2.css";

	/**
	 * Expected output
	 */
	private static final String OUTPUT_1 = "output/test-1.css";

	/**
	 * Expected output
	 */
	private static final String OUTPUT_2 = "output/test-2.css";

	@Test
	public void test1() throws IOException {
		assertExpected(OUTPUT_1, INPUT_1);
		return;
	}

	@Test
	public void test2() throws IOException {
		assertExpected(OUTPUT_2, INPUT_2);
		return;
	}

	/**
	 * Minifies CSS in {@code sourceFile} and compares result to content in
	 * {@code expectedFile}.
	 * 
	 * @param expectedFile filename for expected result
	 * @param sourceFile   filename for source input
	 * @throws IOException if any issues loading resources
	 */
	private void assertExpected(String expectedFile, String sourceFile) throws IOException {
		Reader reader = new InputStreamReader(CSSMinTest.class.getClassLoader().getResourceAsStream(sourceFile));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CSSMin.formatFile(reader, out);
		String expected = new String(Files.readAllBytes(Paths.get(RESOURCES_DIR, expectedFile)));
		String actual = new String(out.toByteArray(), StandardCharsets.UTF_8);
		// trim() here because there seems to be a difference in line endings
		assertEquals(expected.trim(), actual.trim());
		return;
	}
}
