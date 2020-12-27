package net.logicsquad.minifier.js;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;

/**
 * Strips comments and whitespace from Javascript input.
 *
 * @author paulh
 * @author Daniel Galán y Martins
 * @author John Reilly
 * @author Douglas Crockford
 */
public class JSMin {
	/**
	 * End of file
	 */
	private static final int EOF = -1;

	/**
	 * {@link PushbackReader} to wrap {@link Reader} supplied to constructor
	 */
	private final PushbackReader in;

	/**
	 * {@link Writer} for minified output
	 */
	private final Writer out;

	/**
	 * First character in two-character "cursor" over source
	 */
	private int theA;

	/**
	 * Second character in two-character "cursor" over source
	 */
	private int theB;

	/**
	 * Constructor taking a {@link Reader} and a {@link Writer}.
	 * 
	 * @param in  {@link Reader} providing Javascript input
	 * @param out destination {@link Writer} for stripped output
	 */
	public JSMin(final Reader in, final Writer out) {
		this.in = new PushbackReader(in);
		this.out = out;
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
	 * <p>
	 * Performs an action based on {@code d}:
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
	 * @param d type of action
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
	void action(final int d) throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
			UnterminatedStringLiteralException {
		switch (d) {
		case 1:
			out.write(theA);
		case 2:
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
		case 3:
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
		}
	}

	/**
	 * <p>
	 * Copies the input to the output, deleting the characters which are
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
	public void jsmin() throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
			UnterminatedStringLiteralException {
		theA = '\n';
		action(3);
		while (theA != EOF) {
			switch (theA) {
			case ' ':
				if (isAlphanum(theB)) {
					action(1);
				} else {
					action(2);
				}
				break;
			case '\n':
				switch (theB) {
				case '{':
				case '[':
				case '(':
				case '+':
				case '-':
					action(1);
					break;
				case ' ':
					action(3);
					break;
				default:
					if (isAlphanum(theB)) {
						action(1);
					} else {
						action(2);
					}
				}
				break;
			default:
				switch (theB) {
				case ' ':
					if (isAlphanum(theA)) {
						action(1);
						break;
					}
					action(3);
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
						action(1);
						break;
					default:
						if (isAlphanum(theA)) {
							action(1);
						} else {
							action(3);
						}
					}
					break;
				default:
					action(1);
					break;
				}
			}
		}
		out.flush();
	}

	/**
	 * Exception representing an unterminated comment.
	 */
	class UnterminatedCommentException extends Exception {
		private static final long serialVersionUID = 7971352218559346169L;
	}

	/**
	 * Exception representing an unterminated string literal.
	 */
	class UnterminatedStringLiteralException extends Exception {
		private static final long serialVersionUID = 3813645314180522143L;
	}

	/**
	 * Exception representing an unterminated regular expression literal.
	 */
	class UnterminatedRegExpLiteralException extends Exception {
		private static final long serialVersionUID = -5088088141334641219L;
	}
}
