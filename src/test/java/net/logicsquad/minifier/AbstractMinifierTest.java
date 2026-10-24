package net.logicsquad.minifier;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

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
	 * Returns a {@link Minifier}. Obviously, subclasses should return an instance
	 * of the {@link Minifier} they are testing.
	 *
	 * @return {@link Minifier} to test
	 */
	protected abstract Minifier minifier();

	/**
	 * Returns the content of resource {@code filename}.
	 *
	 * @param filename a resource filename
	 * @return the resource's content
	 * @throws IOException if the resource can't be read
	 */
	protected String stringForResource(String filename) throws IOException {
		return new String(Files.readAllBytes(Paths.get(RESOURCES_DIR, filename)), StandardCharsets.UTF_8);
	}

	/**
	 * Returns the content of the source file with "index" {@code index}.
	 *
	 * @param index source file index
	 * @return the source file's content
	 * @throws IOException if the file can't be read
	 */
	protected String stringForSourceFile(String index) throws IOException {
		return stringForResource("input/test-" + index + "." + extension());
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
		return stringForResource("expected/test-" + index + "." + extension());
	}

	/**
	 * Returns the indexes of the input resources, in order.
	 *
	 * @return the indexes of the input resources
	 * @throws IOException if the input directory can't be listed
	 */
	private List<String> indexes() throws IOException {
		try (Stream<Path> files = Files.list(Paths.get(RESOURCES_DIR, "input"))) {
			return files.map(p -> p.getFileName().toString()).filter(f -> f.endsWith("." + extension())).sorted()
					.map(f -> f.substring("test-".length(), f.lastIndexOf('.'))).collect(Collectors.toList());
		}
	}

	/**
	 * Generates a {@link DynamicTest} for each input resource, minifying it and
	 * comparing the result against the corresponding expected output file. Inputs
	 * are discovered from the filesystem, so adding a new {@code input}/{@code
	 * expected} pair is all that's needed to extend coverage. A single
	 * {@link Minifier} minifies them all.
	 *
	 * @return a {@link DynamicTest} per input resource
	 * @throws IOException if the input directory can't be listed
	 */
	@TestFactory
	Stream<DynamicTest> minifiesEachResource() throws IOException {
		Minifier minifier = minifier();
		return indexes().stream().map(index -> DynamicTest.dynamicTest("test-" + index + "." + extension(), () -> {
			// trim() here because there seems to be a difference in line endings
			assertEquals(stringForExpectedFile(index).trim(), minifier.minify(stringForSourceFile(index)).trim());
		}));
	}

	/**
	 * Tests that a single {@link Minifier}, minifying every input resource several
	 * times over from several threads at once, gives the same results as it does
	 * from one.
	 *
	 * @throws Exception if minification fails
	 */
	@Test
	void minifiesFromSeveralThreads() throws Exception {
		Minifier minifier = minifier();
		List<String> sources = new ArrayList<>();
		List<String> expected = new ArrayList<>();
		for (String index : indexes()) {
			String source = stringForSourceFile(index);
			sources.add(source);
			expected.add(minifier.minify(source));
		}
		ExecutorService executor = Executors.newFixedThreadPool(8);
		try {
			List<Future<String>> results = new ArrayList<>();
			for (int round = 0; round < 20; round++) {
				for (String source : sources) {
					Callable<String> task = () -> minifier.minify(source);
					results.add(executor.submit(task));
				}
			}
			for (int i = 0; i < results.size(); i++) {
				assertEquals(expected.get(i % sources.size()), results.get(i).get());
			}
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * Tests that {@link Minifier#minify(Reader, Writer)} writes what
	 * {@link Minifier#minify(String)} returns, and closes neither stream.
	 *
	 * @throws Exception if minification fails
	 */
	@Test
	void minifyWithStreamsClosesNeitherStream() throws Exception {
		String source = stringForSourceFile("01");
		ClosingReader reader = new ClosingReader(source);
		ClosingWriter writer = new ClosingWriter();
		minifier().minify(reader, writer);
		assertEquals(minifier().minify(source), writer.toString());
		assertFalse(reader.closed);
		assertFalse(writer.closed);
	}

	/**
	 * Tests that {@link Minifier#minify(Reader, Writer)} writes nothing if
	 * minification fails.
	 *
	 * @throws IOException if the resource can't be read
	 */
	@Test
	void minifyWithStreamsWritesNothingOnFailure() throws IOException {
		Reader reader = new StringReader(stringForResource("exceptions/unterminated-comment." + extension()));
		StringWriter writer = new StringWriter();
		assertThrows(MinificationException.class, () -> minifier().minify(reader, writer));
		assertEquals("", writer.toString());
	}

	/**
	 * Tests that {@link Minifier#minify(Reader, Writer)} throws the
	 * {@link IOException} from a failing {@link Reader}, unchanged.
	 */
	@Test
	void minifyWithStreamsThrowsReadersException() {
		IOException failure = new IOException("Read failed");
		Reader reader = new Reader() {
			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				throw failure;
			}

			@Override
			public void close() {
			}
		};
		StringWriter writer = new StringWriter();
		assertSame(failure, assertThrows(IOException.class, () -> minifier().minify(reader, writer)));
		assertEquals("", writer.toString());
	}

	/**
	 * Tests that {@link Minifier#minify(Reader, Writer)} throws the
	 * {@link IOException} from a failing {@link Writer}, unchanged.
	 *
	 * @throws IOException if the resource can't be read
	 */
	@Test
	void minifyWithStreamsThrowsWritersException() throws IOException {
		IOException failure = new IOException("Disk full");
		Writer writer = new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				throw failure;
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		};
		Reader reader = new StringReader(stringForSourceFile("01"));
		assertSame(failure, assertThrows(IOException.class, () -> minifier().minify(reader, writer)));
	}

	/**
	 * Tests that resource {@code filename} throws a {@link MinificationException}
	 * caused by {@code expected}.
	 *
	 * @param filename a resource filename
	 * @param expected expected cause
	 * @throws IOException if the resource can't be read
	 */
	protected void throwsOnMinify(String filename, Class<? extends Exception> expected) throws IOException {
		String source = stringForResource(filename);
		MinificationException e = assertThrows(MinificationException.class, () -> minifier().minify(source));
		assertEquals(expected, e.getCause().getClass());
	}

	/**
	 * A {@link StringReader} that records whether it has been closed.
	 */
	private static class ClosingReader extends StringReader {
		/**
		 * Whether {@link #close()} has been called
		 */
		private boolean closed;

		/**
		 * Constructor taking the string to read.
		 *
		 * @param s a string
		 */
		ClosingReader(String s) {
			super(s);
		}

		@Override
		public void close() {
			closed = true;
			super.close();
		}
	}

	/**
	 * A {@link StringWriter} that records whether it has been closed.
	 */
	private static class ClosingWriter extends StringWriter {
		/**
		 * Whether {@link #close()} has been called
		 */
		private boolean closed;

		@Override
		public void close() throws IOException {
			closed = true;
			super.close();
		}
	}
}
