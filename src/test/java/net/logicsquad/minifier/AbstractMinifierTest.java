package net.logicsquad.minifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.logicsquad.minifier.css.CSSMinifierTest;

/**
 * Parent class for tests that compare results from known input to expected
 * output.
 * 
 * @author paulh
 */
public abstract class AbstractMinifierTest {
	/**
	 * Resources directory
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
		return new InputStreamReader(CSSMinifierTest.class.getClassLoader().getResourceAsStream(sourceFile));
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

	/**
	 * Returns a {@link Minifier} for {@code reader}. Obviously, subclasses should
	 * return an instance of the {@link Minifier} they are testing.
	 * 
	 * @param reader a {@link Reader} supplying source content
	 * @return {@link Minifier} to test
	 */
	protected abstract Minifier miniferForReader(Reader reader);

	/**
	 * Returns a list of filename "indexes" that will be used to construct the input
	 * and expected output filenames.
	 * 
	 * @return list of filename "indexes"
	 */
	protected abstract List<String> resources();

	/**
	 * Loops over all filenames that can be constructed, reads in the source
	 * content, minifies it, and compares the result to expected output.
	 * 
	 * @throws IOException if there are any resource reading issues
	 */
	@Test
	public void actualOutputMatchesExpected() throws IOException {
		for (String index : resources()) {
			Writer out = new StringWriter();
			Minifier min = miniferForReader(readerForSourceFile(index));
			min.minify(out);
			String expected = stringForExpectedFile(index);
			// trim() here because there seems to be a difference in line endings
			assertEquals(expected.trim(), out.toString().trim());
		}
		return;
	}
}
