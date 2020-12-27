package net.logicsquad.minifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.logicsquad.minifier.css.CSSMinTest;

/**
 * Parent class for tests that compare results from known input to expected
 * output.
 * 
 * @author paulh
 */
public abstract class AbstractTest {
	/**
	 * Resource directory
	 */
	private static final String RESOURCES_DIR = "src/test/resources";

	/**
	 * Returns file extension to look for.
	 * 
	 * @return file extension
	 */
	protected abstract String extension();

	/**
	 * Returns a {@link Reader} for source file with "index" {@code index}.
	 * 
	 * @param index source file index
	 * @return {@link Reader}
	 */
	protected Reader readerForSourceFile(String index) {
		String sourceFile = "input/test-" + index + "." + extension();
		return new InputStreamReader(CSSMinTest.class.getClassLoader().getResourceAsStream(sourceFile));
	}

	/**
	 * Returns string containing the content of expected output with index
	 * {@code index}.
	 * 
	 * @param index expected output file index
	 * @return expected output
	 * @throws IOException if file can't be read
	 */
	protected String stringForExpectedFile(String index) throws IOException {
		String expectedFile = "output/test-" + index + "." + extension();
		return new String(Files.readAllBytes(Paths.get(RESOURCES_DIR, expectedFile)));
	}
}
