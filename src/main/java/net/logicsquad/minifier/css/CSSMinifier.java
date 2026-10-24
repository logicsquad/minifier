package net.logicsquad.minifier.css;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.logicsquad.minifier.MinificationException;
import net.logicsquad.minifier.Minifier;

/**
 * Strips comments and whitespace from CSS input.
 *
 * @author paulh
 * @author Barry van Oudtshoorn
 */
public final class CSSMinifier implements Minifier {
	/**
	 * Symbolic colour names defined by HTML
	 */
	private static final String[] HTML_COLOUR_NAMES = { "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige",
			"bisque", "black", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse",
			"chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan",
			"darkgoldenrod", "darkgray", "darkgreen", "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange",
			"darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", "darkturquoise",
			"darkviolet", "deeppink", "deepskyblue", "dimgray", "dodgerblue", "firebrick", "floralwhite", "forestgreen",
			"fuchsia", "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "honeydew",
			"hotpink", "indianred ", "indigo ", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen",
			"lemonchiffon", "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgrey", "lightgreen",
			"lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightsteelblue",
			"lightyellow", "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue",
			"mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise",
			"mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace",
			"olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise",
			"palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "red",
			"rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver",
			"skyblue", "slateblue", "slategray", "snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato",
			"turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen" };

	/**
	 * Corresponding hex colour values
	 */
	private static final String[] HTML_COLOUR_VALUES = { "#f0f8ff", "#faebd7", "#00ffff", "#7fffd4", "#f0ffff", "#f5f5dc",
			"#ffe4c4", "#000", "#ffebcd", "#00f", "#8a2be2", "#a52a2a", "#deb887", "#5f9ea0", "#7fff00", "#d2691e",
			"#ff7f50", "#6495ed", "#fff8dc", "#dc143c", "#0ff", "#00008b", "#008b8b", "#b8860b", "#a9a9a9", "#006400",
			"#bdb76b", "#8b008b", "#556b2f", "#ff8c00", "#9932cc", "#8b0000", "#e9967a", "#8fbc8f", "#483d8b",
			"#2f4f4f", "#00ced1", "#9400d3", "#ff1493", "#00bfff", "#696969", "#1e90ff", "#b22222", "#fffaf0",
			"#228b22", "#f0f", "#dcdcdc", "#f8f8ff", "#ffd700", "#daa520", "#808080", "#008000", "#adff2f", "#f0fff0",
			"#ff69b4", "#cd5c5c", "#4b0082", "#fffff0", "#f0e68c", "#e6e6fa", "#fff0f5", "#7cfc00", "#fffacd",
			"#add8e6", "#f08080", "#e0ffff", "#fafad2", "#d3d3d3", "#90ee90", "#ffb6c1", "#ffa07a", "#20b2aa",
			"#87cefa", "#789", "#b0c4de", "#ffffe0", "#0f0", "#32cd32", "#faf0e6", "#f0f", "#800000", "#66cdaa",
			"#0000cd", "#ba55d3", "#9370d8", "#3cb371", "#7b68ee", "#00fa9a", "#48d1cc", "#c71585", "#191970",
			"#f5fffa", "#ffe4e1", "#ffe4b5", "#ffdead", "#000080", "#fdf5e6", "#808000", "#6b8e23", "#ffa500",
			"#ff4500", "#da70d6", "#eee8aa", "#98fb98", "#afeeee", "#d87093", "#ffefd5", "#ffdab9", "#cd853f",
			"#ffc0cb", "#dda0dd", "#b0e0e6", "#800080", "#f00", "#bc8f8f", "#4169e1", "#8b4513", "#fa8072", "#f4a460",
			"#2e8b57", "#fff5ee", "#a0522d", "#c0c0c0", "#87ceeb", "#6a5acd", "#708090", "#fffafa", "#00ff7f",
			"#4682b4", "#d2b48c", "#008080", "#d8bfd8", "#ff6347", "#40e0d0", "#ee82ee", "#f5deb3", "#fff", "#f5f5f5",
			"#ff0", "#9acd32" };

	/**
	 * Symbolic font weight names. The relative weights "bolder" and "lighter"
	 * depend on the inherited weight, so have no numeric equivalent.
	 */
	private static final String[] FONT_WEIGHT_NAMES = { "normal", "bold" };

	/**
	 * Corresponding numeric font weight values
	 */
	private static final String[] FONT_WEIGHT_VALUES = { "400", "700" };

	/**
	 * Properties whose values follow the margin pattern, in which a value can be left
	 * out if it repeats the value it would be copied from: "3px 3px" is equivalent to
	 * "3px", and "1px 2px 1px 2px" to "1px 2px". Repeated values are only removed from
	 * these properties.
	 */
	private static final Set<String> MARGIN_STYLE_PROPERTIES = new HashSet<>(Arrays.asList(
			// One to four values, for the sides (or, for border-radius, the corners) of a box
			"margin", "padding", "inset", "border-width", "border-style", "border-color", "border-radius",
			"scroll-margin", "scroll-padding", "border-image-outset", "border-image-slice", "border-image-width",
			// One or two values, for the start and end sides
			"margin-block", "margin-inline", "padding-block", "padding-inline", "inset-block", "inset-inline",
			"scroll-margin-block", "scroll-margin-inline", "scroll-padding-block", "scroll-padding-inline",
			"border-block-width", "border-block-style", "border-block-color", "border-inline-width",
			"border-inline-style", "border-inline-color",
			// One or two values, for the two axes
			"gap", "grid-gap", "border-spacing", "overflow", "overscroll-behavior", "background-repeat",
			"border-image-repeat"));

	/**
	 * An {@code @charset} statement in the only form that browsers honour, which they
	 * only do at the very start of a stylesheet
	 */
	private static final Pattern HONOURED_CHARSET = Pattern.compile("@charset \"[^\";]*\";");

	/**
	 * Creates a {@code CSSMinifier}, which can minify any number of stylesheets.
	 */
	public CSSMinifier() {
	}

	/**
	 * Returns {@code source}, minified.
	 *
	 * @param source a stylesheet
	 * @return {@code source}, minified
	 * @throws MinificationException if {@code source} can't be minified
	 */
	@Override
	public String minify(String source) throws MinificationException {
		int j, // Number of open braces
				n; // Current position in stream
		char curr;

		// A line break is whitespace, which can separate tokens, so line breaks are
		// kept, as "\n". One at the very end isn't needed, and would end up inside a
		// string or url() left open at the end.
		String input = source.replace("\r\n", "\n").replace('\r', '\n');
		if (input.endsWith("\n")) {
			input = input.substring(0, input.length() - 1);
		}
		String css = removeComments(input);
		List<Selector> selectors = new ArrayList<>();
		// When a rule is skipped, the statements and retained comments in front of it
		// are kept, and output in front of whatever comes next.
		String pending = "";
		// An @import or @namespace statement that comes after a rule is invalid, so an
		// empty rule in front of one is kept: dropping it could make the statement
		// valid. This finds the last mention of either, even in a string or comment,
		// which at worst keeps an empty rule that could have been dropped.
		int lastImport = lastImportOrNamespace(css);
		// Scan for top-level rules, skipping strings, url() tokens and (retained)
		// comments so that braces inside them are not mistaken for rule boundaries.
		// (removeComments() has checked that each "}" has a "{" to close.)
		n = 0;
		j = 0;
		int i = 0;
		while (i < css.length()) {
			curr = css.charAt(i);
			if (curr == '"' || curr == '\'') {
				i = Selector.consumeString(css, i, null);
			} else if (Selector.isUrlStart(css, i)) {
				i = Selector.consumeUrl(css, i, null);
			} else if (css.startsWith("/*", i)) {
				i = Selector.consumeComment(css, i, null);
			} else if (curr == '{') {
				j++;
				i++;
			} else if (curr == '}') {
				j--;
				i++;
				if (j == 0) {
					pending = addSelector(selectors, pending + css.substring(n, i), i <= lastImport);
					n = i;
				}
			} else {
				i++;
			}
		}
		if (j > 0) {
			// The input ends inside a rule that was never closed. Browsers close any
			// blocks still open at the end of a stylesheet, so do the same.
			StringBuilder rule = new StringBuilder(pending + css.substring(n));
			for (; j > 0; j--) {
				rule.append('}');
			}
			pending = addSelector(selectors, rule.toString(), false); // nothing comes after it
			n = css.length();
		}

		StringBuilder output = new StringBuilder();
		for (Selector selector : selectors) {
			output.append(selector);
		}
		// Anything after the last rule, such as a retained comment or an @layer
		// statement, has no rule to be output with, so output it here, along with
		// anything still pending from a rule that was skipped.
		output.append(Selector.minifySelector(pending + css.substring(n)));
		return withoutIgnoredCharset(output.toString(), input);
	}

	/**
	 * Parses {@code rule} as a {@link Selector} and adds it to {@code selectors}. A
	 * rule with an empty body is skipped, unless it's an {@code @layer}
	 * block or {@code keepEmpty} is set. The statements and retained comments in
	 * front of a skipped rule aren't skipped, though: they're returned, to be output
	 * in front of whatever comes next.
	 *
	 * @param selectors list to add the parsed rule to
	 * @param rule      a whole rule, from any statements and retained comments in
	 *                  front of it to its closing brace
	 * @param keepEmpty whether to keep {@code rule} even if its body is empty
	 * @return the statements and retained comments in front of {@code rule} if it
	 *         was skipped, or an empty string if it was added
	 */
	private static String addSelector(List<Selector> selectors, String rule, boolean keepEmpty) {
		int brace = Selector.openingBrace(rule);
		String header = rule.substring(0, brace);
		// The body, without the closing brace that rule ends with
		String body = rule.substring(brace + 1, rule.length() - 1).trim();
		// The caller can ask for an empty rule to be kept, and an empty @layer block
		// always is: it still declares its layer, which fixes where the layer comes in
		// the layer order.
		if (!body.isEmpty() || keepEmpty || Selector.isLayerBlock(header)) {
			selectors.add(new Selector(header, body));
			return "";
		}
		return header.substring(0, Selector.preludeStart(header));
	}

	/**
	 * Returns the index of the last "@import" or "@namespace" in {@code css},
	 * ignoring case, or -1 if there is neither.
	 *
	 * @param css the CSS to search
	 * @return the index of the last "@import" or "@namespace", or -1
	 */
	private static int lastImportOrNamespace(String css) {
		for (int i = css.lastIndexOf('@'); i >= 0; i = css.lastIndexOf('@', i - 1)) {
			if (css.regionMatches(true, i, "@import", 0, 7) || css.regionMatches(true, i, "@namespace", 0, 10)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns {@code output} without any {@code @charset} statements at its start
	 * that {@code input} doesn't start with. Browsers only honour {@code @charset}
	 * at the very start of a stylesheet, written exactly as {@code @charset "name";},
	 * and ignore it anywhere else, so one that minifying has moved to the start, or
	 * rewritten into that form, would change how they decode the stylesheet. Since
	 * browsers ignored it, dropping it changes nothing.
	 *
	 * @param output the minified stylesheet
	 * @param input  the stylesheet before minifying
	 * @return {@code output}, without any {@code @charset} statements at its start
	 *         that {@code input} doesn't start with
	 */
	private static String withoutIgnoredCharset(String output, String input) {
		Matcher m = HONOURED_CHARSET.matcher(output);
		while (m.lookingAt() && !input.startsWith(m.group())) {
			output = output.substring(m.end());
			m = HONOURED_CHARSET.matcher(output);
		}
		return output;
	}

	/**
	 * Returns {@code css} with its comments removed, apart from those that start
	 * with "/*!", which mark a licence or copyright notice, and are retained.
	 * Strings and {@code url()} tokens are skipped, so
	 * that the start of a comment inside one isn't taken for a comment, and a
	 * retained comment is copied whole, so that the start of a comment inside it
	 * isn't either. A string or {@code url()} token left open at the end of the input
	 * is closed, as a browser closes it, and a line break that isn't inside a string,
	 * {@code url()} token or retained comment becomes a space. Where a comment is all
	 * that keeps the tokens on either side of it apart, as it keeps "0" and "auto"
	 * from running together into "0auto", it leaves a space in its place, or, in a
	 * selector, where a space would be a descendant combinator, an empty comment.
	 * Since positions in {@code css} are still those of the input, this is also
	 * where each "}" is checked for a "{" to close.
	 *
	 * @param css the CSS to remove comments from
	 * @return {@code css}, without any comments that aren't retained
	 * @throws MinificationException if a comment is unterminated, or a "}" has no
	 *                               "{" to close
	 */
	private static String removeComments(String css) throws MinificationException {
		StringBuilder sb = new StringBuilder(css.length());
		// Where in sb the current statement, declaration or rule prelude starts
		int start = 0;
		// Number of open braces
		int depth = 0;
		int i = 0;
		while (i < css.length()) {
			char c = css.charAt(i);
			if (c == '"' || c == '\'') {
				i = Selector.consumeString(css, i, sb);
			} else if (Selector.isUrlStart(css, i)) {
				i = Selector.consumeUrl(css, i, sb);
			} else if (css.startsWith("/*", i)) {
				int end = css.indexOf("*/", i + 2);
				if (end == -1) {
					throw new MinificationException("Unterminated comment", css, i);
				}
				if (css.startsWith("/*!", i)) {
					sb.append(css, i, end + 2);
				} else if (wouldJoin(sb, css, end + 2)) {
					sb.append(inSelector(sb.substring(start), css, end + 2) ? "/**/" : " ");
				}
				i = end + 2;
			} else if (c == '\n') {
				// A line break here is just whitespace.
				sb.append(' ');
				i++;
			} else {
				if (c == '{') {
					depth++;
				} else if (c == '}' && --depth < 0) {
					throw new MinificationException("Unmatched \"}\"", css, i);
				}
				sb.append(c);
				i++;
				if (c == '{' || c == '}' || c == ';') {
					start = sb.length();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Returns whether the last character of {@code sb} and the text at {@code i} in
	 * {@code css} would run together into one token with nothing between them, as
	 * "0" and "auto" would into the dimension "0auto". These are the pairs of tokens
	 * that CSS Syntax says need a comment between them when serialized, worked out
	 * from the characters on either side, so a few pairs that wouldn't run
	 * together, such as a number and "(", are kept apart too.
	 *
	 * @param sb  the text in front of a removed comment
	 * @param css the CSS that the comment was removed from
	 * @param i   the index in {@code css} just past the comment
	 * @return whether the text on either side of the comment would run together
	 */
	private static boolean wouldJoin(StringBuilder sb, String css, int i) {
		if (sb.length() == 0 || i >= css.length()) {
			return false;
		}
		char before = sb.charAt(sb.length() - 1);
		char after = css.charAt(i);
		boolean number = isDigit(after) || (after == '.' && i + 1 < css.length() && isDigit(css.charAt(i + 1)));
		if (isNameChar(before)) {
			// An ident, at-keyword, hash, number or dimension would run on into a name or
			// an escape, and an ident into "(", making it a function. A number or "-"
			// would also run on into a number such as ".5", and a number into "%".
			return isNameChar(after) || after == '\\' || after == '('
					|| ((isDigit(before) || before == '-') && number) || (isDigit(before) && after == '%');
		}
		switch (before) {
		case '#':
		case '@':
			return isNameChar(after) || after == '\\';
		case '.':
			return isDigit(after);
		case '+':
			return number;
		case '/':
			// "/" and "*" would start a comment.
			return after == '*';
		default:
			return false;
		}
	}

	/**
	 * Returns whether {@code i} in {@code css} is in a selector: that is, whether
	 * the text it's in, which starts with {@code text}, is the prelude of a rule
	 * other than an at-rule, rather than a statement or a declaration.
	 *
	 * @param text the text in front of {@code i}, from the start of the statement,
	 *             declaration or rule prelude that it's in
	 * @param css  the CSS being scanned
	 * @param i    an index in {@code css}
	 * @return whether {@code i} is in a selector
	 */
	private static boolean inSelector(String text, String css, int i) {
		if (text.startsWith("@", Selector.preludeStart(text))) {
			return false;
		}
		// A selector ends at "{", but a statement or declaration at ";" or "}".
		while (i < css.length()) {
			char c = css.charAt(i);
			if (c == '"' || c == '\'') {
				i = Selector.consumeString(css, i, null);
			} else if (Selector.isUrlStart(css, i)) {
				i = Selector.consumeUrl(css, i, null);
			} else if (css.startsWith("/*", i)) {
				i = Selector.consumeComment(css, i, null);
			} else if (c == '{') {
				return true;
			} else if (c == ';' || c == '}') {
				return false;
			} else {
				i++;
			}
		}
		return false;
	}

	/**
	 * Returns whether {@code c} is an ASCII digit.
	 *
	 * @param c a character
	 * @return whether {@code c} is an ASCII digit
	 */
	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	/**
	 * Returns whether {@code c} can be part of a name, such as an ident: a letter,
	 * digit, "_", "-", or any non-ASCII character.
	 *
	 * @param c a character
	 * @return whether {@code c} can be part of a name
	 */
	private static boolean isNameChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || isDigit(c) || c == '_' || c == '-' || c >= 0x80;
	}

	/**
	 * Represents a CSS selector.
	 */
	private static class Selector {
		// The rule's contents, in their original order: declarations (each a Property),
		// nested rules (each a Selector) and statements (each a String, such as
		// "@layer a,b;")
		private final List<Object> contents = new ArrayList<>();
		private String selector;

		/**
		 * Creates a new Selector from its header and body.
		 *
		 * @param header the selector text, for example "div" or "@media screen"
		 * @param body   the selector body, without its enclosing braces
		 */
		Selector(String header, String body) {
			init(header, body);
		}

		/**
		 * Initialises this Selector from its (raw) header and its body, parsing the
		 * body into its contents: declarations, nested selectors and statements, in
		 * their original order. Handles arbitrarily deep CSS nesting via recursion.
		 *
		 * @param header the raw selector text
		 * @param body   the selector body, without its enclosing braces
		 */
		private void init(String header, String body) {
			this.selector = minifySelector(header);
			body = body.trim();
			// Drop a single trailing semicolon so the final declaration parses cleanly.
			if (body.endsWith(";")) {
				body = body.substring(0, body.length() - 1);
			}
			StringBuilder pending = new StringBuilder();
			int i = 0;
			while (i < body.length()) {
				char c = body.charAt(i);
				if (c == '"' || c == '\'') {
					// A string: consume it whole so that ';', '{' or '}' inside it are
					// not mistaken for boundaries.
					i = consumeString(body, i, pending);
				} else if (isUrlStart(body, i)) {
					// A url(...) token: consume it whole, skipping strings within it so a
					// ')' inside a quoted URL doesn't close it early.
					i = consumeUrl(body, i, pending);
				} else if (body.startsWith("/*", i)) {
					// A retained comment: consume it whole, like a string, so that nothing
					// inside it is mistaken for a boundary.
					i = consumeComment(body, i, pending);
				} else if (c == '{') {
					// A nested rule starts here. Declarations and statements were flushed at
					// their ';', so "pending" now holds only this nested selector's header.
					int j = matchingBrace(body, i);
					this.contents.add(new Selector(pending.toString(), body.substring(i + 1, j - 1)));
					pending.setLength(0);
					i = j;
				} else if (c == ';') {
					addStatementOrProperty(pending.toString());
					pending.setLength(0);
					i++;
				} else {
					pending.append(c);
					i++;
				}
			}
			// A trailing declaration or statement with no terminating semicolon.
			addStatementOrProperty(pending.toString());
		}

		/**
		 * Minifies a selector, or other text outside a rule's body such as an at-rule
		 * statement. Each run of whitespace is reduced to a single space, which is then
		 * removed around combinators, attribute operators and the ";" that ends a
		 * statement, and after any comments and statements in front of the selector.
		 * Strings, {@code url()} tokens and comments are left as they are.
		 *
		 * @param text the text to minify
		 * @return the minified text
		 */
		private static String minifySelector(String text) {
			String minified = outsideStringsAndUrls(text.trim(), s -> s.replaceAll("\\s+", " ")
					.replaceAll("\\s?(\\+|~|,|=|~=|\\^=|\\$=|\\*=|\\|=|>|;)\\s?", "$1"));
			// Whitespace after a comment that comes before anything else, or after a
			// statement, isn't needed. (Elsewhere, as in "div /* c */ p", it can be.)
			StringBuilder sb = new StringBuilder(minified.length());
			boolean leading = true;
			int i = 0;
			while (i < minified.length()) {
				char c = minified.charAt(i);
				if (minified.startsWith("/*", i)) {
					i = consumeComment(minified, i, sb);
				} else if (c == ' ' && leading) {
					i++;
				} else if (c == '"' || c == '\'') {
					i = consumeString(minified, i, sb);
					leading = false;
				} else if (isUrlStart(minified, i)) {
					i = consumeUrl(minified, i, sb);
					leading = false;
				} else {
					sb.append(c);
					leading = c == ';';
					i++;
				}
			}
			return sb.toString();
		}

		/**
		 * Consumes a quoted string starting at {@code i} (the opening quote), appending
		 * the consumed characters to {@code sb} (which may be {@code null} to skip
		 * without collecting). Backslash escapes are honoured, so an escaped quote does
		 * not terminate the string. A string still open at the end of {@code s} is
		 * closed in {@code sb}, as a browser closes one left open at the end of a
		 * stylesheet, and a backslash at the very end, which escapes nothing, is
		 * dropped.
		 *
		 * @param s  the string being scanned
		 * @param i  index of the opening quote
		 * @param sb buffer to append consumed characters to, or {@code null}
		 * @return the index just past the closing quote, or the length of {@code s} if
		 *         the string is unterminated
		 */
		private static int consumeString(String s, int i, StringBuilder sb) {
			char quote = s.charAt(i);
			if (sb != null) {
				sb.append(quote);
			}
			i++;
			while (i < s.length()) {
				char c = s.charAt(i);
				i++;
				if (c == '\\' && i == s.length()) {
					break; // a backslash at the very end escapes nothing
				}
				if (sb != null) {
					sb.append(c);
				}
				if (c == '\\') {
					if (sb != null) {
						sb.append(s.charAt(i)); // escaped character, taken verbatim
					}
					i++;
				} else if (c == quote) {
					return i;
				}
			}
			if (sb != null) {
				sb.append(quote); // close the unterminated string
			}
			return i;
		}

		/**
		 * Returns {@code true} if a {@code url(} function token begins at {@code i}
		 * (case-insensitive) and is not part of a longer identifier.
		 *
		 * @param s the string being scanned
		 * @param i candidate index
		 * @return whether a {@code url(} token starts at {@code i}
		 */
		private static boolean isUrlStart(String s, int i) {
			if (!s.regionMatches(true, i, "url(", 0, 4)) {
				return false;
			}
			if (i > 0) {
				char prev = s.charAt(i - 1);
				if (Character.isLetterOrDigit(prev) || prev == '-' || prev == '_') {
					return false;
				}
			}
			return true;
		}

		/**
		 * Consumes a {@code url(...)} token starting at {@code i} (the {@code u}),
		 * appending the consumed characters to {@code sb} (which may be {@code null} to
		 * skip without collecting). Quoted strings within the URL are skipped whole, so
		 * a {@code )} inside such a string does not terminate the URL. A URL still open
		 * at the end of {@code s} is closed in {@code sb}, as a browser closes one left
		 * open at the end of a stylesheet.
		 *
		 * @param s  the string being scanned
		 * @param i  index of the leading {@code u} of {@code url(}
		 * @param sb buffer to append consumed characters to, or {@code null}
		 * @return the index just past the closing {@code )}, or the length of {@code s}
		 *         if the URL is unterminated
		 */
		private static int consumeUrl(String s, int i, StringBuilder sb) {
			if (sb != null) {
				sb.append(s, i, i + 4); // "url("
			}
			i += 4;
			while (i < s.length()) {
				char c = s.charAt(i);
				if (c == '"' || c == '\'') {
					i = consumeString(s, i, sb);
				} else {
					if (sb != null) {
						sb.append(c);
					}
					i++;
					if (c == ')') {
						return i;
					}
				}
			}
			if (sb != null) {
				sb.append(')'); // close the unterminated URL
			}
			return i;
		}

		/**
		 * Consumes a comment starting at {@code i} (the {@code /} that opens it),
		 * appending the consumed characters to {@code sb} (which may be {@code null} to
		 * skip without collecting).
		 *
		 * @param s  the string being scanned
		 * @param i  index of the start of the comment
		 * @param sb buffer to append consumed characters to, or {@code null}
		 * @return the index just past the end of the comment, or the length of
		 *         {@code s} if the comment is unterminated
		 */
		private static int consumeComment(String s, int i, StringBuilder sb) {
			int end = s.indexOf("*/", i + 2);
			end = end == -1 ? s.length() : end + 2;
			if (sb != null) {
				sb.append(s, i, end);
			}
			return end;
		}

		/**
		 * Given {@code i} pointing at an opening brace, returns the index just past the
		 * matching closing brace. Braces inside strings, {@code url()} tokens and
		 * comments are ignored.
		 *
		 * @param s the string being scanned
		 * @param i index of the opening brace
		 * @return the index just past the matching closing brace, or the length of
		 *         {@code s} if unbalanced
		 */
		private static int matchingBrace(String s, int i) {
			int depth = 0;
			while (i < s.length()) {
				char c = s.charAt(i);
				if (c == '"' || c == '\'') {
					i = consumeString(s, i, null);
				} else if (isUrlStart(s, i)) {
					i = consumeUrl(s, i, null);
				} else if (s.startsWith("/*", i)) {
					i = consumeComment(s, i, null);
				} else {
					i++;
					if (c == '{') {
						depth++;
					} else if (c == '}' && --depth == 0) {
						return i;
					}
				}
			}
			return i;
		}

		/**
		 * Returns the index of the first opening brace in {@code s} that isn't inside a
		 * string, {@code url()} token or comment.
		 *
		 * @param s the string being scanned
		 * @return the index of the opening brace, or -1 if there is none
		 */
		private static int openingBrace(String s) {
			int i = 0;
			while (i < s.length()) {
				char c = s.charAt(i);
				if (c == '"' || c == '\'') {
					i = consumeString(s, i, null);
				} else if (isUrlStart(s, i)) {
					i = consumeUrl(s, i, null);
				} else if (s.startsWith("/*", i)) {
					i = consumeComment(s, i, null);
				} else if (c == '{') {
					return i;
				} else {
					i++;
				}
			}
			return -1;
		}

		/**
		 * Returns the index in {@code header}, the text in front of a rule's opening
		 * brace, at which the rule's own prelude starts, after any statements and
		 * retained comments in front of it. Strings, {@code url()} tokens and comments
		 * are skipped, so that a ";" inside one isn't taken for the end of a statement.
		 *
		 * @param header the text in front of a rule's opening brace
		 * @return the index at which the rule's prelude starts
		 */
		private static int preludeStart(String header) {
			// The prelude starts after the last statement...
			int start = 0;
			int i = 0;
			while (i < header.length()) {
				char c = header.charAt(i);
				if (c == '"' || c == '\'') {
					i = consumeString(header, i, null);
				} else if (isUrlStart(header, i)) {
					i = consumeUrl(header, i, null);
				} else if (header.startsWith("/*", i)) {
					i = consumeComment(header, i, null);
				} else {
					i++;
					if (c == ';') {
						start = i;
					}
				}
			}
			// ...and after any whitespace and comments in front of it.
			i = start;
			while (i < header.length()) {
				if (header.startsWith("/*", i)) {
					i = consumeComment(header, i, null);
				} else if (Character.isWhitespace(header.charAt(i))) {
					i++;
				} else {
					break;
				}
			}
			return i;
		}

		/**
		 * Returns {@code true} if {@code header}, the text in front of a rule's
		 * opening brace, is the prelude of an {@code @layer} block, after any
		 * statements and retained comments in front of it.
		 *
		 * @param header the text in front of a rule's opening brace
		 * @return whether the rule is an {@code @layer} block
		 */
		private static boolean isLayerBlock(String header) {
			int i = preludeStart(header);
			if (!header.regionMatches(true, i, "@layer", 0, 6)) {
				return false;
			}
			// "@layer" must be the whole at-keyword, not the start of a longer one.
			i += 6;
			if (i < header.length()) {
				char next = header.charAt(i);
				return !(Character.isLetterOrDigit(next) || next == '-' || next == '_');
			}
			return true;
		}

		/**
		 * Applies {@code f} to each run of {@code s} outside strings, {@code url()}
		 * tokens and comments, copying those unchanged so that a simplification can't
		 * alter their contents.
		 *
		 * @param s the string to simplify
		 * @param f the simplification to apply
		 * @return {@code s}, with {@code f} applied outside strings, {@code url()}
		 *         tokens and comments
		 */
		private static String outsideStringsAndUrls(String s, UnaryOperator<String> f) {
			StringBuilder sb = new StringBuilder(s.length());
			StringBuilder run = new StringBuilder();
			int i = 0;
			while (i < s.length()) {
				char c = s.charAt(i);
				if (c == '"' || c == '\'') {
					sb.append(f.apply(run.toString()));
					run.setLength(0);
					i = consumeString(s, i, sb);
				} else if (isUrlStart(s, i)) {
					sb.append(f.apply(run.toString()));
					run.setLength(0);
					i = consumeUrl(s, i, sb);
				} else if (s.startsWith("/*", i)) {
					sb.append(f.apply(run.toString()));
					run.setLength(0);
					i = consumeComment(s, i, sb);
				} else {
					run.append(c);
					i++;
				}
			}
			sb.append(f.apply(run.toString()));
			return sb.toString();
		}

		/**
		 * Adds {@code text}, from a rule's body, to this Selector's contents: as a
		 * statement if it's an at-rule statement, such as "@layer a, b", or else as a
		 * declaration.
		 *
		 * @param text a single statement or declaration, without its ";"
		 */
		private void addStatementOrProperty(String text) {
			int start = preludeStart(text);
			if (start < text.length() && text.charAt(start) == '@') {
				this.contents.add(minifySelector(text + ";"));
			} else {
				addProperty(text);
			}
		}

		/**
		 * Parses {@code declaration} as a {@link Property} and adds it to this
		 * Selector's contents. Blank and incomplete declarations are skipped.
		 *
		 * @param declaration a single "name: value" declaration
		 */
		private void addProperty(String declaration) {
			if (declaration.trim().isEmpty()) {
				return;
			}
			Property property = Property.parse(declaration);
			if (property != null) {
				this.contents.add(property);
			} else {
				// An incomplete declaration is skipped, but not the retained comments in
				// front of it, such as one after the last declaration.
				String text = declaration.trim();
				String comments = text.substring(0, Property.afterComments(text)).trim();
				if (!comments.isEmpty()) {
					this.contents.add(comments);
				}
			}
		}

		/**
		 * Prints out this selector and its contents, in their original order. (A later
		 * declaration wins over an earlier one, so reordering them could change which
		 * value applies.)
		 *
		 * @returns A string representing this selector, minified.
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.selector).append("{");
			for (Object item : this.contents) {
				sb.append(item);
			}
			// The last declaration doesn't need its ";". (A statement keeps its own,
			// since ending one with the "}" is a parse error.)
			if (!this.contents.isEmpty() && this.contents.get(this.contents.size() - 1) instanceof Property) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("}");
			return sb.toString();
		}
	}

	/**
	 * Represents a CSS property.
	 */
	private static class Property {
		protected String property;
		protected Part[] parts;
		// Any retained comments in front of the declaration, copied through unchanged
		protected String comments;

		/**
		 * Parses {@code declaration} as a Property.
		 *
		 * @param declaration The declaration; for example, "border: solid 1px red;" or
		 *                    "-moz-box-shadow: 3px 3px 3px rgba(255, 255, 0, 0.5);".
		 * @return the declaration as a Property, or {@code null} if it's incomplete:
		 *         if it has no name or no value
		 */
		static Property parse(String declaration) {
			// Retained comments in front of the declaration are kept in front of it,
			// unchanged, rather than being parsed as part of the property name.
			String property = declaration.trim();
			int start = afterComments(property);
			String comments = property.substring(0, start).trim();
			property = property.substring(start);

			ArrayList<String> parts = new ArrayList<String>();
			boolean bCanSplit = true;
			int j = 0;
			String substr;
			for (int i = 0; i < property.length(); i++) {
				if (!bCanSplit) { // If we're inside a string
					bCanSplit = (property.charAt(i) == '"');
				} else if (property.charAt(i) == '"') {
					bCanSplit = false;
				} else if (property.charAt(i) == ':' && parts.size() < 1) {
					substr = property.substring(j, i);
					if (!(substr.trim().equals("") || (substr == null))) {
						parts.add(substr);
					}
					j = i + 1;
				}
			}
			substr = property.substring(j, property.length());
			if (!substr.trim().equals("")) {
				parts.add(substr);
			}
			if (parts.size() < 2) {
				return null;
			}
			return new Property(comments, parts.get(0), parts.get(1));
		}

		/**
		 * Returns the index in {@code declaration} at which the retained comments in
		 * front of it, and any whitespace after them, end.
		 *
		 * @param declaration a declaration, without whitespace in front of it
		 * @return the index of what follows the retained comments in front of
		 *         {@code declaration}
		 */
		static int afterComments(String declaration) {
			int start = 0;
			while (declaration.startsWith("/*", start)) {
				start = Selector.consumeComment(declaration, start, null);
				while (start < declaration.length() && Character.isWhitespace(declaration.charAt(start))) {
					start++;
				}
			}
			return start;
		}

		/**
		 * Creates a new Property from its name and value. Parses out the parts of the
		 * value.
		 *
		 * @param comments any retained comments in front of the declaration
		 * @param name     the property's name
		 * @param value    the property's value
		 */
		private Property(String comments, String name, String value) {
			this.comments = comments;
			String prop = name.trim();
			if (!(prop.length() > 2 && prop.substring(0, 2).equals("--"))) {
				prop = prop.toLowerCase();
			}
			this.property = prop;
			this.parts = parseValues(Selector.outsideStringsAndUrls(value.trim(),
					s -> simplifyColours(s.replaceAll("\\s+", " ").replaceAll(", ", ","))));
		}

		/**
		 * Prints out this property nicely.
		 *
		 * @returns A string representing this property, minified.
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.comments).append(this.property).append(":");
			for (Part p : this.parts) {
				sb.append(p.toString()).append(",");
			}
			sb.deleteCharAt(sb.length() - 1); // Delete the trailing comma.
			sb.append(";");
			return sb.toString();
		}

		/**
		 * Parse the values out of a property.
		 *
		 * @param contents The property to parse
		 * @returns An array of Parts
		 */
		private Part[] parseValues(String contents) {
			String[] parts = splitValues(contents);
			List<Part> results = new ArrayList<>(parts.length);

			for (int i = 0; i < parts.length; i++) {
				results.add(new Part(parts[i], property));
			}

			return results.toArray(new Part[0]);
		}

		/**
		 * Splits {@code contents} at each comma, except for commas inside a string, a
		 * {@code url()} token (such as a data URI) or a comment, which belong to the
		 * value that contains them.
		 *
		 * @param contents the property value to split
		 * @return the comma-separated values
		 */
		private static String[] splitValues(String contents) {
			List<String> values = new ArrayList<>();
			StringBuilder value = new StringBuilder();
			int i = 0;
			while (i < contents.length()) {
				char c = contents.charAt(i);
				if (c == '"' || c == '\'') {
					i = Selector.consumeString(contents, i, value);
				} else if (Selector.isUrlStart(contents, i)) {
					i = Selector.consumeUrl(contents, i, value);
				} else if (contents.startsWith("/*", i)) {
					i = Selector.consumeComment(contents, i, value);
				} else if (c == ',') {
					values.add(value.toString());
					value.setLength(0);
					i++;
				} else {
					value.append(c);
					i++;
				}
			}
			values.add(value.toString());
			return values.toArray(new String[0]);
		}

		private String simplifyColours(String contents) {
			// This replacement, although it results in a smaller uncompressed file,
			// actually makes the gzipped file bigger -- people tend to use rgba(0,0,0,0.x)
			// quite a lot, which means that rgba(0,0,0,0) has its first eight or so
			// characters
			// compressed really efficiently; much more so than "transparent".
			// contents = contents.replaceAll("rgba\\(0,0,0,0\\)", "transparent");

			return simplifyRGBColours(contents);
		}

		// Convert rgb(51,102,153) to #336699 (this code largely based on YUI code)
		private String simplifyRGBColours(String contents) {
			StringBuffer newContents = new StringBuffer();
			StringBuffer hexColour;
			String[] rgbColours;
			int colourValue;

			Pattern pattern = Pattern.compile("rgb\\s*\\(\\s*([0-9,\\s]+)\\s*\\)");
			Matcher matcher = pattern.matcher(contents);

			while (matcher.find()) {
				hexColour = new StringBuffer("#");
				rgbColours = matcher.group(1).split(",");
				if (rgbColours.length != 3) {
					// Only rgb(r,g,b) has a hex equivalent here. A fourth (alpha) component is
					// not a byte (rgb(255,0,0,1) is opaque), so leave the colour untouched.
					matcher.appendReplacement(newContents, Matcher.quoteReplacement(matcher.group()));
					continue;
				}
				try {
					for (int i = 0; i < rgbColours.length; i++) {
						colourValue = Integer.parseInt(rgbColours[i]);
						if (colourValue > 255) {
							// Valid CSS (browsers clamp it to 255), but it has no two-digit hex form.
							throw new NumberFormatException("rgb() component above 255: " + colourValue);
						}
						if (colourValue < 16) {
							hexColour.append("0");
						}
						hexColour.append(Integer.toHexString(colourValue));
					}
				} catch (NumberFormatException e) {
					// A component is above 255, out of int range, or otherwise unparseable. Leave
					// this colour untouched rather than aborting minification, consistent with how
					// non-numeric rgb() values are left alone.
					matcher.appendReplacement(newContents, Matcher.quoteReplacement(matcher.group()));
					continue;
				}
				matcher.appendReplacement(newContents, hexColour.toString());
			}
			matcher.appendTail(newContents);

			return newContents.toString();
		}
	}

	/**
	 * Represents a part of a CSS property.
	 */
	private static class Part {
		String contents;
		String property;

		/**
		 * Create a new property by parsing the given string.
		 *
		 * @param contents The string to parse.
		 */
		public Part(String contents, String property) {
			// Many of these regular expressions are adapted from those used in the YUI CSS
			// Compressor.

			// For simpler regexes.
			this.contents = " " + contents;
			this.property = property;

			simplify();
		}

		private void simplify() {
			// !important doesn't need to be spaced
			this.contents = Selector.outsideStringsAndUrls(this.contents, s -> s.replaceAll(" !important", "!important"));

			// Replace 0in, 0cm, etc. with just 0
			this.contents = Selector.outsideStringsAndUrls(this.contents,
					s -> s.replaceAll("(\\s)(0)(px|em|%|in|cm|mm|pc|pt|ex)", "$1$2"));

			// Replace 0.6 with .6
			// Disabled, as it actually makes compression worse! People use rgba(0,0,0,0)
			// and rgba(0,0,0,0.x) a lot.
			// this.contents = this.contents.replaceAll("(\\s)0+\\.(\\d+)", "$1.$2");

			this.contents = this.contents.trim();

			// Simplify multiple-parameter properties (which also reduces multiple zeroes,
			// such as "0 0 0 0", to "0")
			simplifyParameters();

			// Simplify font weights
			simplifyFontWeights();

			// Strip unnecessary quotes from url(), and excess whitespace from var(). Values
			// are deliberately not lowercased: some, such as custom property values, are
			// case-sensitive.
			simplifyQuotesAndCaps();

			// Simplify colours
			simplifyColourNames();
			this.contents = Selector.outsideStringsAndUrls(this.contents, Part::simplifyHexColours);
		}

		private void simplifyParameters() {
			// A border-radius with a "/" has separate vertical radii, so doesn't follow the
			// margin pattern as a whole.
			if (!MARGIN_STYLE_PROPERTIES.contains(this.property)
					|| (this.property.equals("border-radius") && this.contents.contains("/")))
				return;

			StringBuffer newContents = new StringBuffer();

			String[] params = this.contents.split(" ");
			if (params.length == 4) {
				// We can drop off the fourth item if the second and fourth items match
				// ie turn 3px 0 3px 0 into 3px 0 3px
				if (params[1].equalsIgnoreCase(params[3])) {
					params = Arrays.copyOf(params, 3);
				}
			}
			if (params.length == 3) {
				// We can drop off the third item if the first and third items match
				// ie turn 3px 0 3px into 3px 0
				if (params[0].equalsIgnoreCase(params[2])) {
					params = Arrays.copyOf(params, 2);
				}
			}
			if (params.length == 2) {
				// We can drop off the second item if the first and second items match
				// ie turn 3px 3px into 3px
				if (params[0].equalsIgnoreCase(params[1])) {
					params = Arrays.copyOf(params, 1);
				}
			}

			for (int i = 0; i < params.length; i++) {
				newContents.append(params[i] + " ");
			}
			newContents.deleteCharAt(newContents.length() - 1); // Delete the trailing space

			this.contents = newContents.toString();
		}

		private void simplifyFontWeights() {
			if (!this.property.equals("font-weight"))
				return;

			String lcContents = this.contents.toLowerCase();

			for (int i = 0; i < FONT_WEIGHT_NAMES.length; i++) {
				if (lcContents.equals(FONT_WEIGHT_NAMES[i])) {
					this.contents = FONT_WEIGHT_VALUES[i];
					break;
				}
			}
		}

		private void simplifyQuotesAndCaps() {
			// Strip quotes from URLs
			if ((this.contents.length() > 4) && (this.contents.substring(0, 4).equalsIgnoreCase("url("))) {
				// Only strip the quotes when the unquoted form is equivalent. A URL whose
				// content contains whitespace, parentheses, quotes or a backslash must keep
				// its quotes to remain valid.
				Matcher matcher = Pattern.compile("(?i)url\\(('|\")?(.*?)\\1\\)").matcher(this.contents);
				StringBuffer sb = new StringBuffer();
				while (matcher.find()) {
					String inner = matcher.group(2);
					String replacement;
					if (matcher.group(1) != null && Pattern.compile("[\\s\"'()\\\\]").matcher(inner).find()) {
						replacement = matcher.group(); // quotes are required; leave untouched
					} else {
						replacement = "url(" + inner + ")";
					}
					matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
				}
				matcher.appendTail(sb);
				this.contents = sb.toString();
			} else if ((this.contents.length() > 4) && (this.contents.substring(0, 4).equalsIgnoreCase("var("))) {
				// We can't just remove all whitespace in the line, but we can ensure there's a maximum of one space in any run.
				// https://github.com/logicsquad/minifier/issues/5
				this.contents = this.contents.replaceAll("\\s{2,}", " ").trim();
			}
		}

		private void simplifyColourNames() {
			String lcContents = this.contents.toLowerCase();

			for (int i = 0; i < HTML_COLOUR_NAMES.length; i++) {
				if (lcContents.equals(HTML_COLOUR_NAMES[i])) {
					if (HTML_COLOUR_VALUES[i].length() < HTML_COLOUR_NAMES[i].length()) {
						this.contents = HTML_COLOUR_VALUES[i];
					}
					break;
				} else if (lcContents.equals(HTML_COLOUR_VALUES[i])) {
					if (HTML_COLOUR_NAMES[i].length() < HTML_COLOUR_VALUES[i].length()) {
						this.contents = HTML_COLOUR_NAMES[i];
					}
				}
			}
		}

		private static String simplifyHexColours(String contents) {
			StringBuffer newContents = new StringBuffer();

			// The lookahead stops this matching the first six digits of an eight-digit
			// (#rrggbbaa) colour.
			Pattern pattern = Pattern
					.compile("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])"
							+ "(?![0-9a-fA-F])");
			Matcher matcher = pattern.matcher(contents);

			while (matcher.find()) {
				if (matcher.group(1).equalsIgnoreCase(matcher.group(2))
						&& matcher.group(3).equalsIgnoreCase(matcher.group(4))
						&& matcher.group(5).equalsIgnoreCase(matcher.group(6))) {
					matcher.appendReplacement(newContents, "#" + matcher.group(1).toLowerCase()
							+ matcher.group(3).toLowerCase() + matcher.group(5).toLowerCase());
				} else {
					matcher.appendReplacement(newContents, matcher.group().toLowerCase());
				}
			}
			matcher.appendTail(newContents);

			return newContents.toString();
		}

		/**
		 * Returns itself.
		 *
		 * @returns this part's string representation.
		 */
		public String toString() {
			return this.contents;
		}
	}
}
