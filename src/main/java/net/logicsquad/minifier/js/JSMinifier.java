package net.logicsquad.minifier.js;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

import net.logicsquad.minifier.AbstractMinifier;
import net.logicsquad.minifier.MinificationException;

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
	 * Last character returned by {@link #next()}
	 */
	private int theX = EOF;

	/**
	 * Character returned by {@link #next()} before {@link #theX}, which shows
	 * whether whitespace came between A and B
	 */
	private int theY = EOF;

	/**
	 * Number of "{"s read in code, less the number of "}"s, not counting those that
	 * start and end template literal substitutions
	 */
	private int depth;

	/**
	 * For each template literal substitution open at this point, innermost first,
	 * the value of {@link #depth} where it starts
	 */
	private final Deque<Integer> substitutions = new ArrayDeque<>();

	/**
	 * Whether {@link #theA} is the "/" that ends a regular expression literal,
	 * which whitespace has to keep apart from a word after it, as it would an
	 * identifier: in {@code /re/in}, "in" would be the literal's flags
	 */
	private boolean theAEndsRegExp;

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
	 * Returns next character from {@link #in}, reading a {@code \r\n} or
	 * {@code \r} line break as {@code \n}. The characters in a literal are read
	 * with this method, so that they're copied as they are.
	 *
	 * @return next character from {@link #in}
	 * @throws IOException if unable to {@link Reader#read()} or
	 *                     {@link PushbackReader#unread(int)}
	 */
	int read() throws IOException {
		int c = in.read();
		if (c == '\r') {
			int d = in.read();
			if (d != '\n' && d != EOF) {
				in.unread(d);
			}
			return '\n';
		}
		return c;
	}

	/**
	 * Returns next character from {@link #in}, as {@link #read()} does, but with
	 * any other control character, such as a tab, translated to a space.
	 *
	 * @return next (translated) character from {@link #in}
	 * @throws IOException if unable to {@link Reader#read()} or
	 *                     {@link PushbackReader#unread(int)}
	 */
	int get() throws IOException {
		int c = read();
		if (c >= ' ' || c == '\n' || c == EOF) {
			return c;
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
	 * Returns the next character from {@link #in}, <em>excluding comments</em>: a
	 * block comment is returned as a space, and a line comment as the line break
	 * that ends it. The character is also remembered in {@link #theX}, and the one
	 * before it in {@link #theY}.
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
				do {
					c = get();
				} while (c > '\n');
				break;
			case '*':
				get();
				while (c != ' ') {
					switch (get()) {
					case '*':
						if (peek() == '/') {
							get();
							c = ' ';
						}
						break;
					case EOF:
						throw new UnterminatedCommentException();
					}
				}
				break;
			}
		}
		theY = theX;
		theX = c;
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
	 * Strings, regular expressions and the text of template literals are treated
	 * as "single characters", in that they are pushed through the pipeline in
	 * their entirety by a single call to this method.
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
			// Two of "+", "-", "*" and "/" can run together into a different token, as
			// "+ +" would into "++", so keep the whitespace that came between them.
			if ((theY == ' ' || theY == '\n') && (theA == '+' || theA == '-' || theA == '*' || theA == '/')
					&& (theB == '+' || theB == '-' || theB == '*' || theB == '/')) {
				out.write(theY);
			}
		case COPY_GET:
			theA = theB;
			theAEndsRegExp = false;
			if (theA == '\'' || theA == '"') {
				for (;;) {
					out.write(theA);
					theA = read();
					if (theA == theB) {
						break;
					}
					if (theA == '\n' || theA == EOF) {
						throw new UnterminatedStringLiteralException();
					}
					if (theA == '\\') {
						out.write(theA);
						theA = read();
					}
				}
			} else if (theA == '`' || theA == '}' && !substitutions.isEmpty() && substitutions.peek() == depth) {
				if (theA == '}') {
					substitutions.pop();
				}
				copyTemplate(out);
			} else if (theA == '{') {
				depth++;
			} else if (theA == '}') {
				depth--;
			}
		case GET:
			theB = next();
			if (theB == '/' && (theA == '(' || theA == ',' || theA == '=' || theA == ':' || theA == '[' || theA == '!'
					|| theA == '&' || theA == '|' || theA == '?' || theA == '{' || theA == '}' || theA == ';'
					|| theA == '\n')) {
				out.write(theA);
				out.write(theB);
				for (;;) {
					theA = read();
					if (theA == '[') {
						// A "/" in a character class doesn't end the literal.
						for (;;) {
							out.write(theA);
							theA = read();
							if (theA == ']') {
								break;
							}
							if (theA == '\\') {
								out.write(theA);
								theA = read();
							}
							if (theA == '\n' || theA == EOF) {
								throw new UnterminatedRegExpLiteralException();
							}
						}
					} else if (theA == '/') {
						break;
					} else if (theA == '\\') {
						out.write(theA);
						theA = read();
					}
					if (theA == '\n' || theA == EOF) {
						throw new UnterminatedRegExpLiteralException();
					}
					out.write(theA);
				}
				theAEndsRegExp = true;
				theB = next();
			}
			break;
		default:
			throw new IllegalStateException("Unknown action: " + action);
		}
	}

	/**
	 * Copies the text of a template literal to {@code out}, from {@link #theA},
	 * the backtick that opens it or the "}" that ends a substitution in it, up to
	 * the backtick that closes it or the "${" that starts a substitution. That
	 * backtick or "{" is left in {@link #theA}, and the code in a substitution is
	 * minified as any other code is.
	 *
	 * @param out {@link Writer} for minified output
	 * @throws IOException                        if thrown by {@link Reader} or
	 *                                            {@link Writer}
	 * @throws UnterminatedStringLiteralException if the template literal is
	 *                                            unterminated
	 */
	private void copyTemplate(Writer out) throws IOException, UnterminatedStringLiteralException {
		for (;;) {
			out.write(theA);
			theA = read();
			if (theA == '`') {
				return;
			}
			if (theA == '$' && peek() == '{') {
				out.write(theA);
				theA = read();
				substitutions.push(depth);
				return;
			}
			if (theA == '\\') {
				out.write(theA);
				theA = read();
			}
			if (theA == EOF) {
				throw new UnterminatedStringLiteralException();
			}
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
	 * @throws MinificationException if minification fails
	 */
	@Override
	public void minify(Writer writer) throws MinificationException {
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
					case '!':
					case '~':
					case '"':
					case '\'':
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
						if (isAlphanum(theA) || theAEndsRegExp) {
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
						case '`':
							action(Action.OUTPUT_COPY_GET, writer);
							break;
						default:
							if (isAlphanum(theA) || theAEndsRegExp) {
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
			if (!substitutions.isEmpty()) {
				// The input ends in a template literal substitution.
				throw new UnterminatedStringLiteralException();
			}
		} catch (IOException | UnterminatedCommentException | UnterminatedRegExpLiteralException
				| UnterminatedStringLiteralException e) {
			throw new MinificationException("Minification failed due to Exception.", e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				throw new MinificationException("Minification failed due to Exception.", e);
			}
		}
	}

	/**
	 * Exception representing an unterminated comment.
	 */
	static class UnterminatedCommentException extends Exception {
		private static final long serialVersionUID = 7971352218559346169L;
	}

	/**
	 * Exception representing an unterminated string literal.
	 */
	static class UnterminatedStringLiteralException extends Exception {
		private static final long serialVersionUID = 3813645314180522143L;
	}

	/**
	 * Exception representing an unterminated regular expression literal.
	 */
	static class UnterminatedRegExpLiteralException extends Exception {
		private static final long serialVersionUID = -5088088141334641219L;
	}
}
