package net.logicsquad.minifier;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.logicsquad.minifier.css.CSSMinifierTest;

/**
 * Parent class for tests that compare results from known input to expected output.
 *
 * @author paulh
 */
public abstract class AbstractMinifierTest {
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AbstractMinifierTest.class);

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
	 * Returns a {@link Reader} for {@code filename}.
	 * 
	 * @param filename a resource filename
	 * @return {@link Reader}
	 */
	protected Reader readerForSourceFile(String filename) {
		return new InputStreamReader(CSSMinifierTest.class.getClassLoader().getResourceAsStream(filename));
	}

	/**
	 * Returns a {@link Reader} for source file with "index" {@code index}.
	 *
	 * @param index source file index
	 * @return {@link Reader}
	 */
	protected Reader readerForIndex(String index) {
		return readerForSourceFile("input/test-" + index + "." + extension());
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
	 * Returns count of test resources for subclass.
	 * 
	 * @return count of test resources
	 */
	protected abstract int resourceCount();

	/**
	 * Returns a list of filename "indexes" that will be used to construct the input
	 * and expected output filenames.
	 *
	 * @return list of filename "indexes"
	 */
	protected List<String> resources() {
		List<String> result = new ArrayList<>();
		for (int i = 1; i <= resourceCount(); i++) {
			result.add(String.format("%02d", i));
		}
		return result;
	}

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
			Minifier min = miniferForReader(readerForIndex(index));
			try {
				min.minify(out);
			} catch (MinificationException e) {
				LOG.error("MinificationException with cause: {}", e.getCause().getClass().getName());
				fail(e);
			}
			String expected = stringForExpectedFile(index);
			// trim() here because there seems to be a difference in line endings
			assertEquals(expected.trim(), out.toString().trim(), getClass().getName() + " failed on index " + index);
		}
		return;
	}

	/**
	 * Tests that input for {@code index} throws {@code expected} {@link Exception}.
	 * 
	 * @param index input resource index
	 * @param expected expected {@link Exception}
	 * @throws IOException if there are any resource reading issues
	 */
	protected void throwsOnMinify(String filename, Class<? extends Exception> expected) throws IOException {
		Writer out = new StringWriter();
		Minifier min = miniferForReader(readerForSourceFile(filename));
		try {
			min.minify(out);
		} catch (Exception e) {
			assertEquals(MinificationException.class, e.getClass());
			assertEquals(expected, e.getCause().getClass());
			return;
		}
		fail("Expected: " + expected.getClass().getName());
		return;
	}
}
