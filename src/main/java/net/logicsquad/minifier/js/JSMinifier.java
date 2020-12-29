package net.logicsquad.minifier.js;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;

import net.logicsquad.minifier.AbstractMinifier;

/**
 * Strips comments and whitespace from Javascript input.
 *
 * @author paulh
 * @author Daniel Galán y Martins
 * @author John Reilly
 * @author Douglas Crockford
 */
public class JSMinifier extends AbstractMinifier {
	/**
	 * End of file
	 */
	private static final int EOF = -1;

	/**
	 * {@link PushbackReader} to wrap {@link Reader} supplied to constructor
	 */
	private final PushbackReader in;

	/**
	 * First character in two-character "cursor" over source
	 */
	private int theA;

	/**
	 * Second character in two-character "cursor" over source
	 */
	private int theB;

	/**
	 * Constructor taking a {@link Reader} that will provide the input resource.
	 *
	 * @param reader a {@link Reader}
	 */
	public JSMinifier(Reader reader) {
		super(reader);
		this.in = new PushbackReader(reader);
		return;
	}

	/**
	 * Is {@code c} a letter, digit, underscore, dollar sign, or non-ASCII
	 * character?
	 *
	 * @param c a character
	 * @return {@code true} if {@code c} is alphanumeric, otherwise {@code false}
	 */
	static boolean isAlphanum(final int c) {
		return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$'
				|| c == '\\' || c > 126);
	}

	/**
	 * <p>
	 * Returns next character from {@link #in}. If the character is a control
	 * character, translates:
	 * </p>
	 *
	 * <ul>
	 * <li>{@code \r} to {@code \n}; and</li>
	 * <li>everything else to a space.</li>
	 * </ul>
	 *
	 * @return next (translated) character from {@link #in}
	 * @throws IOException if unable to {@link Reader#read()}
	 */
	int get() throws IOException {
		int c = in.read();
		if (c >= ' ' || c == '\n' || c == EOF) {
			return c;
		}
		if (c == '\r') {
			return '\n';
		}
		return ' ';
	}

	/**
	 * Returns next character from {@link #in} (with no translation), without
	 * removing it from {@link #in}. (That is, a subsequent call to {@link #get()}
	 * will return the character again.)
	 *
	 * @return next character
	 * @throws IOException if unable to {@link Reader#read()} or
	 *                     {@link PushbackReader#unread(char[])}
	 */
	int peek() throws IOException {
		int lookaheadChar = in.read();
		in.unread(lookaheadChar);
		return lookaheadChar;
	}

	/**
	 * Returns the next character from {@link #in}, <em>excluding comments</em>.
	 *
	 * @return next character (excluding comments)
	 * @throws IOException                  if thrown by {@link #get()} or
	 *                                      {@link #peek()}
	 * @throws UnterminatedCommentException if comment was unterminated
	 */
	int next() throws IOException, UnterminatedCommentException {
		int c = get();
		if (c == '/') {
			switch (peek()) {
			case '/':
				for (;;) {
					c = get();
					if (c <= '\n') {
						return c;
					}
				}
			case '*':
				get();
				for (;;) {
					switch (get()) {
					case '*':
						if (peek() == '/') {
							get();
							return ' ';
						}
						break;
					case EOF:
						throw new UnterminatedCommentException();
					}
				}
			default:
				return c;
			}
		}
		return c;
	}

	/**
	 * Represents an action for {@link #action(Action, Writer)} method.
	 */
	private enum Action {
		/**
		 * Output A, copy B to A, get the next B
		 */
		OUTPUT_COPY_GET,

		/**
		 * Copy B to A, get the next B
		 */
		COPY_GET,

		/**
		 * Get the next B
		 */
		GET;
	}

	/**
	 * <p>
	 * Performs an action based on {@code action}:
	 * </p>
	 *
	 * <dl>
	 * <dt>1</dt>
	 * <dd>Output A. Copy B to A. Get the next B.</dd>
	 * <dt>2</dt>
	 * <dd>Copy B to A. Get the next B. (Delete A).</dd>
	 * <dt>3</dt>
	 * <dd>Get the next B. (Delete B).</dd>
	 * </dl>
	 *
	 * <p>
	 * String and regular expressions are treated as "single characters", in that
	 * they are pushed through the pipeline in their entirety by a single call to
	 * this method.
	 * <p>
	 *
	 * @param action type of action
	 * @param out    {@link Writer} for minified output
	 * @throws IOException                        if thrown by {@link Reader} or
	 *                                            {@link Writer}
	 * @throws UnterminatedRegExpLiteralException if this method encounters an
	 *                                            unterminated regular expression
	 *                                            literal
	 * @throws UnterminatedCommentException       if this method encounters an
	 *                                            unterminated comment
	 * @throws UnterminatedStringLiteralException if this method encounters an
	 *                                            unterminated string literal
	 */
	void action(Action action, Writer out) throws IOException, UnterminatedRegExpLiteralException,
			UnterminatedCommentException, UnterminatedStringLiteralException {
		switch (action) {
		case OUTPUT_COPY_GET:
			out.write(theA);
		case COPY_GET:
			theA = theB;
			if (theA == '\'' || theA == '"') {
				for (;;) {
					out.write(theA);
					theA = get();
					if (theA == theB) {
						break;
					}
					if (theA <= '\n') {
						throw new UnterminatedStringLiteralException();
					}
					if (theA == '\\') {
						out.write(theA);
						theA = get();
					}
				}
			}
		case GET:
			theB = next();
			if (theB == '/' && (theA == '(' || theA == ',' || theA == '=' || theA == ':' || theA == '[' || theA == '!'
					|| theA == '&' || theA == '|' || theA == '?' || theA == '{' || theA == '}' || theA == ';'
					|| theA == '\n')) {
				out.write(theA);
				out.write(theB);
				for (;;) {
					theA = get();
					if (theA == '/') {
						break;
					} else if (theA == '\\') {
						out.write(theA);
						theA = get();
					} else if (theA <= '\n') {
						throw new UnterminatedRegExpLiteralException();
					}
					out.write(theA);
				}
				theB = next();
			}
		default:
			throw new IllegalStateException("Unknown action: " + action);
		}
	}

	/**
	 * <p>
	 * Copies from input to {@code writer}, deleting the characters which are
	 * insignificant to Javascript. Specifically:
	 * </p>
	 *
	 * <ul>
	 * <li>comments will be removed;</li>
	 * <li>tabs will be replaced with spaces;</li>
	 * <li>carriage returns will be replaced with linefeeds; and</li>
	 * <li>most spaces and linefeeds will be removed.</li>
	 * </ul>
	 *
	 * @param writer {@link Writer} for output
	 */
	@Override
	public void minify(Writer writer) {
		try {
			theA = '\n';
			action(Action.GET, writer);
			while (theA != EOF) {
				switch (theA) {
				case ' ':
					if (isAlphanum(theB)) {
						action(Action.OUTPUT_COPY_GET, writer);
					} else {
						action(Action.COPY_GET, writer);
					}
					break;
				case '\n':
					switch (theB) {
					case '{':
					case '[':
					case '(':
					case '+':
					case '-':
						action(Action.OUTPUT_COPY_GET, writer);
						break;
					case ' ':
						action(Action.GET, writer);
						break;
					default:
						if (isAlphanum(theB)) {
							action(Action.OUTPUT_COPY_GET, writer);
						} else {
							action(Action.COPY_GET, writer);
						}
					}
					break;
				default:
					switch (theB) {
					case ' ':
						if (isAlphanum(theA)) {
							action(Action.OUTPUT_COPY_GET, writer);
							break;
						}
						action(Action.GET, writer);
						break;
					case '\n':
						switch (theA) {
						case '}':
						case ']':
						case ')':
						case '+':
						case '-':
						case '"':
						case '\'':
							action(Action.OUTPUT_COPY_GET, writer);
							break;
						default:
							if (isAlphanum(theA)) {
								action(Action.OUTPUT_COPY_GET, writer);
							} else {
								action(Action.GET, writer);
							}
						}
						break;
					default:
						action(Action.OUTPUT_COPY_GET, writer);
						break;
					}
				}
			}
			writer.flush();
		} catch (Exception e) {
			// TODO...
		}
	}

	/**
	 * Exception representing an unterminated comment.
	 */
	private static class UnterminatedCommentException extends Exception {
		private static final long serialVersionUID = 7971352218559346169L;
	}

	/**
	 * Exception representing an unterminated string literal.
	 */
	private static class UnterminatedStringLiteralException extends Exception {
		private static final long serialVersionUID = 3813645314180522143L;
	}

	/**
	 * Exception representing an unterminated regular expression literal.
	 */
	private static class UnterminatedRegExpLiteralException extends Exception {
		private static final long serialVersionUID = -5088088141334641219L;
	}
}
