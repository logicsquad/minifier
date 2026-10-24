package net.logicsquad.minifier.js;

import net.logicsquad.minifier.MinificationException;
import net.logicsquad.minifier.Minifier;

/**
 * Strips comments and whitespace from Javascript input.
 *
 * @author paulh
 * @author Daniel Galán y Martins
 * @author John Reilly
 * @author Douglas Crockford
 */
public class JSMinifier implements Minifier {
	/**
	 * End of file
	 */
	private static final int EOF = -1;

	/**
	 * Creates a {@code JSMinifier}, which can minify any number of scripts.
	 */
	public JSMinifier() {
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
	 * Returns {@code source}, minified, by deleting the characters which are
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
	 * @param source a script
	 * @return {@code source}, minified
	 * @throws MinificationException if {@code source} can't be minified
	 */
	@Override
	public String minify(String source) throws MinificationException {
		return new Minification(source).minify();
	}

	/**
	 * Represents an action for {@link Minification#action(Action)}.
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
	 * A single minification: its input, how far it has read, its two-character
	 * "cursor" over the input, and its output so far. Each call to
	 * {@link JSMinifier#minify(String)} has its own, so a {@code JSMinifier} keeps
	 * no state between calls.
	 */
	private static class Minification {
		/**
		 * Source being minified
		 */
		private final String in;

		/**
		 * Index in {@link #in} of the next character to read
		 */
		private int pos;

		/**
		 * First character in two-character "cursor" over source
		 */
		private int theA;

		/**
		 * Second character in two-character "cursor" over source
		 */
		private int theB;

		/**
		 * Index in {@link #in} of {@link #theB}, for reporting where a string or
		 * regular expression literal starts
		 */
		private int bIndex;

		/**
		 * Minified output
		 */
		private final StringBuilder out;

		/**
		 * Constructor taking the source to minify.
		 *
		 * @param in source to minify
		 */
		Minification(String in) {
			this.in = in;
			this.out = new StringBuilder(in.length());
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
		 * @return next (translated) character from {@link #in}, or {@link #EOF} at
		 *         the end
		 */
		int get() {
			if (pos >= in.length()) {
				return EOF;
			}
			int c = in.charAt(pos++);
			if (c >= ' ' || c == '\n') {
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
		 * @return next character, or {@link #EOF} at the end
		 */
		int peek() {
			return pos < in.length() ? in.charAt(pos) : EOF;
		}

		/**
		 * Returns the next character from {@link #in}, <em>excluding comments</em>.
		 *
		 * @return next character (excluding comments)
		 * @throws MinificationException if comment was unterminated
		 */
		int next() throws MinificationException {
			int c = get();
			if (c == '/') {
				int start = pos - 1;
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
							throw new MinificationException("Unterminated comment", in, start);
						}
					}
				default:
					return c;
				}
			}
			return c;
		}

		/**
		 * Appends {@code c} to the output.
		 *
		 * @param c a character
		 */
		private void write(int c) {
			out.append((char) c);
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
		 * @throws MinificationException if this method encounters an unterminated
		 *                               comment, string literal or regular
		 *                               expression literal
		 */
		void action(Action action) throws MinificationException {
			switch (action) {
			case OUTPUT_COPY_GET:
				write(theA);
			case COPY_GET:
				theA = theB;
				if (theA == '\'' || theA == '`' || theA == '"') {
					int start = bIndex;
					for (;;) {
						write(theA);
						theA = get();
						if (theA == theB) {
							break;
						}
						if (theA <= '\n') {
							throw new MinificationException("Unterminated string literal", in, start);
						}
						if (theA == '\\') {
							write(theA);
							theA = get();
						}
					}
				}
			case GET:
				theB = next();
				bIndex = pos - 1;
				if (theB == '/' && (theA == '(' || theA == ',' || theA == '=' || theA == ':' || theA == '['
						|| theA == '!' || theA == '&' || theA == '|' || theA == '?' || theA == '{' || theA == '}'
						|| theA == ';' || theA == '\n')) {
					int start = bIndex;
					write(theA);
					write(theB);
					for (;;) {
						theA = get();
						if (theA == '/') {
							break;
						} else if (theA == '\\') {
							write(theA);
							theA = get();
						} else if (theA <= '\n') {
							throw new MinificationException("Unterminated regular expression literal", in, start);
						}
						write(theA);
					}
					theB = next();
					bIndex = pos - 1;
				}
				break;
			default:
				throw new IllegalStateException("Unknown action: " + action);
			}
		}

		/**
		 * Minifies {@link #in}, deleting the characters which are insignificant to
		 * Javascript.
		 *
		 * @return {@link #in}, minified
		 * @throws MinificationException if {@link #in} has an unterminated comment,
		 *                               string literal or regular expression literal
		 */
		String minify() throws MinificationException {
			theA = '\n';
			action(Action.GET);
			while (theA != EOF) {
				switch (theA) {
				case ' ':
					if (isAlphanum(theB)) {
						action(Action.OUTPUT_COPY_GET);
					} else {
						action(Action.COPY_GET);
					}
					break;
				case '\n':
					switch (theB) {
					case '{':
					case '[':
					case '(':
					case '+':
					case '-':
						action(Action.OUTPUT_COPY_GET);
						break;
					case ' ':
						action(Action.GET);
						break;
					default:
						if (isAlphanum(theB)) {
							action(Action.OUTPUT_COPY_GET);
						} else {
							action(Action.COPY_GET);
						}
					}
					break;
				default:
					switch (theB) {
					case ' ':
						if (isAlphanum(theA)) {
							action(Action.OUTPUT_COPY_GET);
							break;
						}
						action(Action.GET);
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
							action(Action.OUTPUT_COPY_GET);
							break;
						default:
							if (isAlphanum(theA)) {
								action(Action.OUTPUT_COPY_GET);
							} else {
								action(Action.GET);
							}
						}
						break;
					default:
						action(Action.OUTPUT_COPY_GET);
						break;
					}
				}
			}
			return out.toString();
		}
	}
}
