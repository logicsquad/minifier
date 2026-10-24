package net.logicsquad.minifier;

/**
 * Exception thrown when a web resource can't be minified. Its message says what
 * went wrong, and where, such as "Unterminated comment at line 12, column 5", and
 * {@link #line()} and {@link #column()} return where.
 *
 * @author paulh
 */
public class MinificationException extends Exception {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Line at which minification failed, numbered from 1
	 */
	private final int line;

	/**
	 * Column at which minification failed, numbered from 1
	 */
	private final int column;

	/**
	 * Constructor taking what went wrong, and where.
	 *
	 * @param message what went wrong, such as "Unterminated comment"
	 * @param line    the line at which it went wrong, numbered from 1
	 * @param column  the column at which it went wrong, numbered from 1
	 */
	public MinificationException(String message, int line, int column) {
		super(message + " at line " + line + ", column " + column);
		this.line = line;
		this.column = column;
	}

	/**
	 * Constructor taking what went wrong, and where, as an index into the source
	 * being minified. The line and column are worked out from {@code index},
	 * counting "\n", "\r\n" and "\r" as line breaks.
	 *
	 * @param message what went wrong, such as "Unterminated comment"
	 * @param source  the source being minified
	 * @param index   the index in {@code source} at which it went wrong
	 * @throws IndexOutOfBoundsException if {@code index} is negative, or greater
	 *                                   than the length of {@code source}
	 */
	public MinificationException(String message, String source, int index) {
		this(message, position(source, index));
	}

	/**
	 * Constructor taking what went wrong, and where, as a line and a column.
	 *
	 * @param message  what went wrong
	 * @param position the line and the column at which it went wrong
	 */
	private MinificationException(String message, int[] position) {
		this(message, position[0], position[1]);
	}

	/**
	 * Returns the line and column of {@code index} in {@code source}, both numbered
	 * from 1, counting "\n", "\r\n" and "\r" as line breaks.
	 *
	 * @param source a string
	 * @param index  an index in {@code source}
	 * @return the line and the column of {@code index}
	 * @throws IndexOutOfBoundsException if {@code index} is negative, or greater
	 *                                   than the length of {@code source}
	 */
	private static int[] position(String source, int index) {
		if (index < 0 || index > source.length()) {
			throw new IndexOutOfBoundsException("Index " + index + " is outside a source of length " + source.length());
		}
		int line = 1;
		int lineStart = 0;
		for (int i = 0; i < index; i++) {
			char c = source.charAt(i);
			// "\r\n" is one line break, which ends with its "\n".
			if (c == '\n' || (c == '\r' && (i + 1 == source.length() || source.charAt(i + 1) != '\n'))) {
				line++;
				lineStart = i + 1;
			}
		}
		return new int[] { line, index - lineStart + 1 };
	}

	/**
	 * Returns the line at which minification failed, numbered from 1.
	 *
	 * @return the line at which minification failed
	 */
	public int line() {
		return line;
	}

	/**
	 * Returns the column at which minification failed, numbered from 1.
	 *
	 * @return the column at which minification failed
	 */
	public int column() {
		return column;
	}
}
