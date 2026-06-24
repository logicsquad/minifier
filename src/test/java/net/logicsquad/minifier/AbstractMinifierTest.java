package net.logicsquad.minifier;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import net.logicsquad.minifier.css.CSSMinifierTest;

/**
 * Parent class for tests that compare results from known input to expected output.
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
		String expectedFile = "expected/test-" + index + "." + extension();
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
	 * Generates a {@link DynamicTest} for each input resource, minifying it and
	 * comparing the result against the corresponding expected output file. Inputs
	 * are discovered from the filesystem, so adding a new {@code input}/{@code
	 * expected} pair is all that's needed to extend coverage.
	 *
	 * @return a {@link DynamicTest} per input resource
	 * @throws IOException if the input directory can't be listed
	 */
	@TestFactory
	Stream<DynamicTest> minifiesEachResource() throws IOException {
		List<Path> inputs;
		try (Stream<Path> files = Files.list(Paths.get(RESOURCES_DIR, "input"))) {
			inputs = files.filter(p -> p.getFileName().toString().endsWith("." + extension())).sorted()
					.collect(Collectors.toList());
		}
		return inputs.stream().map(input -> {
			String filename = input.getFileName().toString();
			String index = filename.substring("test-".length(), filename.lastIndexOf('.'));
			return DynamicTest.dynamicTest(filename, () -> {
				Writer out = new StringWriter();
				miniferForReader(readerForIndex(index)).minify(out);
				// trim() here because there seems to be a difference in line endings
				assertEquals(stringForExpectedFile(index).trim(), out.toString().trim());
			});
		});
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
