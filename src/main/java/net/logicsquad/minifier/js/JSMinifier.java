package net.logicsquad.minifier.js;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

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
public final class JSMinifier implements Minifier {
	/**
	 * End of file
	 */
	private static final int EOF = -1;

	/**
	 * Keywords that a regular expression literal can follow
	 */
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("case", "delete", "do", "else", "in",
			"instanceof", "new", "return", "throw", "typeof", "void"));

	/**
	 * Keywords whose condition, in parentheses, a regular expression literal can
	 * follow, as in {@code if (x) /re/.test(s)}
	 */
	private static final Set<String> CONDITIONS = new HashSet<>(Arrays.asList("for", "if", "while", "with"));

	/**
	 * Number of characters of output that {@link Minification#regExpCanFollow()}
	 * looks at, enough for the longest of {@link #KEYWORDS} and the character in
	 * front of it
	 */
	private static final int LOOKBEHIND = 16;

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
	 * Returns the word that ends at {@code end} in {@code s}, or an empty string
	 * if there isn't one, or it's a property, after a ".", which can have the
	 * same name as a keyword, as in {@code a.return / 2}.
	 *
	 * @param s   characters written
	 * @param end an index into {@code s}
	 * @return the word that ends at {@code end}, unless it's a property
	 */
	private static String wordBefore(CharSequence s, int end) {
		int start = end;
		while (start > 0 && isAlphanum(s.charAt(start - 1))) {
			start--;
		}
		return start > 0 && s.charAt(start - 1) == '.' ? "" : s.subSequence(start, end).toString();
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
		 * Last character returned by {@link #next()}
		 */
		private int theX = EOF;

		/**
		 * Character returned by {@link #next()} before {@link #theX}, which shows
		 * whether whitespace came between A and B
		 */
		private int theY = EOF;

		/**
		 * Number of "{"s read in code, less the number of "}"s, not counting those
		 * that start and end template literal substitutions
		 */
		private int depth;

		/**
		 * For each template literal substitution open at this point, innermost first,
		 * the value of {@link #depth} where it starts
		 */
		private final Deque<Integer> substitutions = new ArrayDeque<>();

		/**
		 * For each of {@link #substitutions}, the index in {@link #in} of the
		 * backtick that opens its template literal, for reporting where it starts
		 */
		private final Deque<Integer> templateStarts = new ArrayDeque<>();

		/**
		 * Whether {@link #theA} is the "/" that ends a regular expression literal,
		 * or whitespace after one. Whitespace has to keep the literal apart from a
		 * word after it, as it would an identifier: in {@code /re/in}, "in" would be
		 * the literal's flags. And a "/" after it is division.
		 */
		private boolean afterRegExp;

		/**
		 * For each "(" open at this point, innermost first, whether it starts the
		 * condition of one of {@link #CONDITIONS}
		 */
		private final Deque<Boolean> parentheses = new ArrayDeque<>();

		/**
		 * Whether {@link #theA} is the ")" that ends the condition of one of
		 * {@link #CONDITIONS}, or whitespace after one, which a regular expression
		 * literal can follow
		 */
		private boolean afterCondition;

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
		 * Returns next character from {@link #in}, reading a {@code \r\n} or
		 * {@code \r} line break as {@code \n}. The characters in a literal are read
		 * with this method, so that they're copied as they are.
		 *
		 * @return next character from {@link #in}, or {@link #EOF} at the end
		 */
		int read() {
			if (pos >= in.length()) {
				return EOF;
			}
			int c = in.charAt(pos++);
			if (c == '\r') {
				if (pos < in.length() && in.charAt(pos) == '\n') {
					pos++;
				}
				return '\n';
			}
			return c;
		}

		/**
		 * Returns next character from {@link #in}, as {@link #read()} does, but with
		 * any other control character, such as a tab, translated to a space.
		 *
		 * @return next (translated) character from {@link #in}, or {@link #EOF} at
		 *         the end
		 */
		int get() {
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
		 * @return next character, or {@link #EOF} at the end
		 */
		int peek() {
			return pos < in.length() ? in.charAt(pos) : EOF;
		}

		/**
		 * Returns the next character from {@link #in}, <em>excluding comments</em>:
		 * a block comment is returned as a space, and a line comment as the line
		 * break that ends it. The character is also remembered in {@link #theX}, and
		 * the one before it in {@link #theY}.
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
							throw new MinificationException("Unterminated comment", in, start);
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
		 * Appends {@code c} to the output.
		 *
		 * @param c a character
		 */
		private void write(int c) {
			out.append((char) c);
		}

		/**
		 * Returns whether a regular expression literal can start after
		 * {@link #theA}: whether what comes before it, skipping whitespace, is the
		 * start of the input, one of {@link #KEYWORDS}, the condition of one of
		 * {@link #CONDITIONS}, or an operator or other punctuator that an expression
		 * can follow. After anything else, such as an identifier, another ")" or a
		 * regular expression literal, a "/" is division.
		 *
		 * @return {@code true} if a "/" after {@link #theA} starts a regular
		 *         expression literal
		 */
		private boolean regExpCanFollow() {
			if (afterRegExp) {
				return false;
			}
			StringBuilder before = new StringBuilder(LOOKBEHIND + 1);
			before.append(out, Math.max(0, out.length() - LOOKBEHIND), out.length());
			if (theA != ' ' && theA != '\n') {
				before.append((char) theA);
			}
			int end = before.length();
			if (end == 0) {
				return true;
			}
			char c = before.charAt(end - 1);
			if (isAlphanum(c)) {
				return KEYWORDS.contains(wordBefore(before, end));
			}
			if (c == ')') {
				return afterCondition;
			}
			if ((c == '+' || c == '-') && end > 1 && before.charAt(end - 2) == c) {
				// The "++" or "--" after an operand, as in "a++ / 2"
				return false;
			}
			return "(,=:[!&|?+-~*/{};<>%^".indexOf(c) >= 0;
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
		 * @throws MinificationException if this method encounters an unterminated
		 *                               comment, string literal or regular
		 *                               expression literal
		 */
		void action(Action action) throws MinificationException {
			switch (action) {
			case OUTPUT_COPY_GET:
				write(theA);
				// Two of "+", "-", "*" and "/" can run together into a different token, as
				// "+ +" would into "++", so keep the whitespace that came between them.
				if ((theY == ' ' || theY == '\n') && (theA == '+' || theA == '-' || theA == '*' || theA == '/')
						&& (theB == '+' || theB == '-' || theB == '*' || theB == '/')) {
					write(theY);
				}
			case COPY_GET:
				afterRegExp = afterRegExp && (theB == ' ' || theB == '\n');
				afterCondition = afterCondition && (theB == ' ' || theB == '\n');
				theA = theB;
				if (theA == '\'' || theA == '"') {
					int start = bIndex;
					for (;;) {
						write(theA);
						theA = read();
						if (theA == theB) {
							break;
						}
						if (theA == '\n' || theA == EOF) {
							throw new MinificationException("Unterminated string literal", in, start);
						}
						if (theA == '\\') {
							write(theA);
							theA = read();
						}
					}
				} else if (theA == '`') {
					copyTemplate(bIndex);
				} else if (theA == '}' && !substitutions.isEmpty() && substitutions.peek() == depth) {
					substitutions.pop();
					copyTemplate(templateStarts.pop());
				} else if (theA == '{') {
					depth++;
				} else if (theA == '}') {
					depth--;
				} else if (theA == '(') {
					int end = out.length();
					while (end > 0 && (out.charAt(end - 1) == ' ' || out.charAt(end - 1) == '\n')) {
						end--;
					}
					parentheses.push(CONDITIONS.contains(wordBefore(out, end)));
				} else if (theA == ')') {
					afterCondition = !parentheses.isEmpty() && parentheses.pop();
				}
			case GET:
				theB = next();
				bIndex = pos - 1;
				if (theB == '/' && regExpCanFollow()) {
					int start = bIndex;
					// A space after a keyword can go, but a line break can end a statement.
					if (theA != ' ') {
						write(theA);
					}
					// Keep "/" and "*" apart from the "/", as "/ /" would run together into "//".
					if (theA == '/' || theA == '*') {
						write(' ');
					}
					write(theB);
					for (;;) {
						theA = read();
						if (theA == '[') {
							// A "/" in a character class doesn't end the literal.
							for (;;) {
								write(theA);
								theA = read();
								if (theA == ']') {
									break;
								}
								if (theA == '\\') {
									write(theA);
									theA = read();
								}
								if (theA == '\n' || theA == EOF) {
									throw new MinificationException("Unterminated regular expression literal", in,
											start);
								}
							}
						} else if (theA == '/') {
							break;
						} else if (theA == '\\') {
							write(theA);
							theA = read();
						}
						if (theA == '\n' || theA == EOF) {
							throw new MinificationException("Unterminated regular expression literal", in, start);
						}
						write(theA);
					}
					afterRegExp = true;
					theB = next();
					bIndex = pos - 1;
				}
				break;
			default:
				throw new IllegalStateException("Unknown action: " + action);
			}
		}

		/**
		 * Copies the text of a template literal to the output, from {@link #theA},
		 * the backtick that opens it or the "}" that ends a substitution in it, up to
		 * the backtick that closes it or the "${" that starts a substitution. That
		 * backtick or "{" is left in {@link #theA}, and the code in a substitution is
		 * minified as any other code is.
		 *
		 * @param start index in {@link #in} of the backtick that opens the template
		 *              literal
		 * @throws MinificationException if the template literal is unterminated
		 */
		private void copyTemplate(int start) throws MinificationException {
			for (;;) {
				write(theA);
				theA = read();
				if (theA == '`') {
					return;
				}
				if (theA == '$' && peek() == '{') {
					write(theA);
					theA = read();
					substitutions.push(depth);
					templateStarts.push(start);
					return;
				}
				if (theA == '\\') {
					write(theA);
					theA = read();
				}
				if (theA == EOF) {
					throw new MinificationException("Unterminated string literal", in, start);
				}
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
					case '!':
					case '~':
					case '"':
					case '\'':
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
						if (isAlphanum(theA) || afterRegExp) {
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
						case '`':
							action(Action.OUTPUT_COPY_GET);
							break;
						default:
							if (isAlphanum(theA) || afterRegExp) {
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
			if (!substitutions.isEmpty()) {
				// The input ends in a template literal substitution.
				throw new MinificationException("Unterminated string literal", in, templateStarts.peek());
			}
			// JSMin starts with a line break as its "previous character", and outputs it
			// in front of the first token. A line break at either end does nothing.
			int start = 0;
			int end = out.length();
			while (start < end && out.charAt(start) == '\n') {
				start++;
			}
			while (end > start && out.charAt(end - 1) == '\n') {
				end--;
			}
			return out.substring(start, end);
		}
	}
}
