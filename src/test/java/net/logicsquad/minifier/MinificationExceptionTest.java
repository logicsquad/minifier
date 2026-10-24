package net.logicsquad.minifier;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests on {@link MinificationException} class.
 *
 * @author paulh
 */
public class MinificationExceptionTest {
	@Test
	public void reportsWhatWentWrongAndWhere() {
		MinificationException e = new MinificationException("Unterminated comment", 12, 5);
		assertEquals("Unterminated comment at line 12, column 5", e.getMessage());
		assertEquals(12, e.line());
		assertEquals(5, e.column());
	}

	/**
	 * Tests that the line and column are worked out from an index, with "\n",
	 * "\r\n" and "\r" each counting as one line break.
	 */
	@Test
	public void worksOutLineAndColumnFromIndex() {
		String source = "ab\ncd\r\nef\rgh";
		assertPosition(source, 0, 1, 1);
		assertPosition(source, 1, 1, 2);
		assertPosition(source, 2, 1, 3);
		assertPosition(source, 3, 2, 1);
		assertPosition(source, 5, 2, 3);
		assertPosition(source, 6, 2, 4);
		assertPosition(source, 7, 3, 1);
		assertPosition(source, 9, 3, 3);
		assertPosition(source, 10, 4, 1);
		assertPosition(source, 12, 4, 3);
	}

	@Test
	public void lineBreakAtEndStartsNewLine() {
		assertPosition("a\n", 2, 2, 1);
		assertPosition("a\r", 2, 2, 1);
		assertPosition("a\r\n", 3, 2, 1);
	}

	@Test
	public void messageIncludesWorkedOutPosition() {
		MinificationException e = new MinificationException("Unmatched \"}\"", "a {}\n}", 5);
		assertEquals("Unmatched \"}\" at line 2, column 1", e.getMessage());
	}

	@Test
	public void rejectsIndexOutsideSource() {
		assertThrows(IndexOutOfBoundsException.class, () -> new MinificationException("Problem", "abc", -1));
		assertThrows(IndexOutOfBoundsException.class, () -> new MinificationException("Problem", "abc", 4));
	}

	/**
	 * Asserts that {@code index} in {@code source} is at {@code line} and
	 * {@code column}.
	 *
	 * @param source a source
	 * @param index  an index in {@code source}
	 * @param line   the expected line
	 * @param column the expected column
	 */
	private static void assertPosition(String source, int index, int line, int column) {
		MinificationException e = new MinificationException("Problem", source, index);
		assertEquals(line, e.line(), "line of index " + index);
		assertEquals(column, e.column(), "column of index " + index);
	}
}
