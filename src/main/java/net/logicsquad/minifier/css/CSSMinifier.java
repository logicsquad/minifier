package net.logicsquad.minifier.css;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.logicsquad.minifier.AbstractMinifier;
import net.logicsquad.minifier.MinificationException;

/**
 * Strips comments and whitespace from CSS input.
 *
 * @author paulh
 * @author Barry van Oudtshoorn
 */
public class CSSMinifier extends AbstractMinifier {
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CSSMinifier.class);

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
	 * Symbolic font weight names
	 */
	private static final String[] FONT_WEIGHT_NAMES = { "normal", "bold", "bolder", "lighter" };

	/**
	 * Corresponding numeric font weight values
	 */
	private static final String[] FONT_WEIGHT_VALUES = { "400", "700", "900", "100" };

	/**
	 * Constructor taking a {@link Reader} that will provide the input resource.
	 *
	 * @param reader a {@link Reader}
	 */
	public CSSMinifier(Reader reader) {
		super(reader);
	}

	/**
	 * Copies from input to {@code writer}, minifying CSS content.
	 * 
	 * @param writer {@link Writer} for output
	 * @throws MinificationException if minification fails
	 */
	@Override
	public void minify(Writer writer) throws MinificationException {
		try (BufferedReader br = new BufferedReader(reader()); PrintWriter pout = new PrintWriter(writer)) {
			int k, j, // Number of open braces
					n; // Current position in stream
			char curr;

			StringBuffer sb = new StringBuffer();
			String s;
			while ((s = br.readLine()) != null) {
				if (s.trim().equals("")) {
					continue;
				}
				sb.append(s);
			}

			LOG.debug("Removing comments...");
			// Find the start of the comment
			n = 0;
			while ((n = sb.indexOf("/*", n)) != -1) {
				// Here we retain "Javadoc-style" comments. We're looking for "/**", but need to exclude "/**/".
				// https://github.com/logicsquad/minifier/issues/4
				if (sb.charAt(n + 2) == '*' && sb.charAt(n + 3) != '/') {
					n += 2;
					continue;
				}
				k = sb.indexOf("*/", n + 2);
				if (k == -1) {
					throw new UnterminatedCommentException();
				}
				sb.delete(n, k + 2);
			}
			LOG.debug("Parsing and processing selectors...");
			List<Selector> selectors = new ArrayList<>();
			// Scan for top-level rules, skipping strings and url() tokens so that braces
			// inside them are not mistaken for rule boundaries.
			String css = sb.toString();
			n = 0;
			j = 0;
			int i = 0;
			while (i < css.length()) {
				curr = css.charAt(i);
				if (curr == '"' || curr == '\'') {
					i = Selector.consumeString(css, i, null);
				} else if (Selector.isUrlStart(css, i)) {
					i = Selector.consumeUrl(css, i, null);
				} else if (curr == '{') {
					j++;
					i++;
				} else if (curr == '}') {
					j--;
					i++;
					if (j < 0) {
						throw new UnbalancedBracesException();
					}
					if (j == 0) {
						try {
							selectors.add(new Selector(css.substring(n, i)));
						} catch (UnterminatedSelectorException usex) {
							LOG.debug("Unterminated selector: {}", usex.getMessage());
						} catch (EmptySelectorBodyException ebex) {
							LOG.debug("Empty selector body: {}", ebex.getMessage());
						}
						n = i;
					}
				} else {
					i++;
				}
			}

			for (Selector selector : selectors) {
				pout.print(selector.toString());
			}
			pout.print("\r\n");
			LOG.debug("Process completed successfully.");
		} catch (UnterminatedCommentException | UnbalancedBracesException | IncompleteSelectorException
				| IOException e) {
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
	 * Represents a CSS selector.
	 */
	private static class Selector {
		private Property[] properties = null;
		private List<Selector> subSelectors = null;
		private String selector;

		/**
		 * Creates a new Selector using the supplied strings.
		 *
		 * @param selector The selector; for example, "div { border: solid 1px red;
		 *                 color: blue; }"
		 * @throws Exception If the selector is incomplete and cannot be parsed.
		 */
		public Selector(String selector)
				throws IncompleteSelectorException, UnterminatedSelectorException, EmptySelectorBodyException {
			int brace = selector.indexOf('{');
			if (brace < 0) {
				throw new IncompleteSelectorException(selector);
			}
			String body = selector.substring(brace + 1).trim();
			// The body must be brace-terminated, and must contain more than just "}".
			if (body.isEmpty() || body.charAt(body.length() - 1) != '}') {
				throw new UnterminatedSelectorException(selector);
			}
			if (body.length() == 1) {
				throw new EmptySelectorBodyException(selector);
			}
			LOG.debug("Parsing selector: {}", selector.substring(0, brace).trim());
			init(selector.substring(0, brace), body.substring(0, body.length() - 1));
		}

		/**
		 * Creates a new Selector from a pre-split header and body. Used when recursing
		 * into nested rules, where the surrounding braces have already been stripped.
		 *
		 * @param header the selector text, for example "div" or "@media screen"
		 * @param body   the selector body, without its enclosing braces
		 * @throws IncompleteSelectorException  if a nested selector cannot be parsed
		 * @throws UnterminatedSelectorException if a nested selector is unterminated
		 * @throws EmptySelectorBodyException   if a nested selector has an empty body
		 */
		private Selector(String header, String body)
				throws IncompleteSelectorException, UnterminatedSelectorException, EmptySelectorBodyException {
			init(header, body);
		}

		/**
		 * Initialises this Selector from its (raw) header and its body, parsing the
		 * body into nested selectors and/or properties. Handles arbitrarily deep CSS
		 * nesting via recursion.
		 *
		 * @param header the raw selector text
		 * @param body   the selector body, without its enclosing braces
		 */
		private void init(String header, String body)
				throws IncompleteSelectorException, UnterminatedSelectorException, EmptySelectorBodyException {
			this.selector = header.trim().replaceAll("\\s?(\\+|~|,|=|~=|\\^=|\\$=|\\*=|\\|=|>)\\s?", "$1");
			body = body.trim();
			// Drop a single trailing semicolon so the final declaration parses cleanly.
			if (body.endsWith(";")) {
				body = body.substring(0, body.length() - 1);
			}
			this.subSelectors = new ArrayList<>();
			List<Property> props = new ArrayList<>();
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
				} else if (c == '{') {
					// A nested rule starts here. Declarations were flushed at their ';',
					// so "pending" now holds only this nested selector's header.
					int j = matchingBrace(body, i);
					this.subSelectors.add(new Selector(pending.toString(), body.substring(i + 1, j - 1)));
					pending.setLength(0);
					i = j;
				} else if (c == ';') {
					addProperty(props, pending.toString());
					pending.setLength(0);
					i++;
				} else {
					pending.append(c);
					i++;
				}
			}
			// A trailing declaration with no terminating semicolon.
			addProperty(props, pending.toString());
			this.properties = props.toArray(new Property[0]);
			sortProperties(this.properties);
		}

		/**
		 * Consumes a quoted string starting at {@code i} (the opening quote), appending
		 * the consumed characters to {@code sb} (which may be {@code null} to skip
		 * without collecting). Backslash escapes are honoured, so an escaped quote does
		 * not terminate the string.
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
				if (sb != null) {
					sb.append(c);
				}
				i++;
				if (c == '\\' && i < s.length()) {
					if (sb != null) {
						sb.append(s.charAt(i)); // escaped character, taken verbatim
					}
					i++;
				} else if (c == quote) {
					break;
				}
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
		 * a {@code )} inside such a string does not terminate the URL.
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
						break;
					}
				}
			}
			return i;
		}

		/**
		 * Given {@code i} pointing at an opening brace, returns the index just past the
		 * matching closing brace. Braces inside strings and {@code url()} tokens are
		 * ignored.
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
		 * Parses {@code declaration} as a {@link Property} and adds it to
		 * {@code props}. Blank declarations are ignored, and incomplete ones are
		 * logged and skipped.
		 *
		 * @param props       list to add the parsed property to
		 * @param declaration a single "name: value" declaration
		 */
		private void addProperty(List<Property> props, String declaration) {
			if (declaration.trim().isEmpty()) {
				return;
			}
			try {
				props.add(new Property(declaration));
			} catch (IncompletePropertyException ipex) {
				LOG.debug("Incomplete property in selector '{}': {}", selector, ipex.getMessage());
			}
		}

		/**
		 * Prints out this selector and its contents nicely, with the contents sorted
		 * alphabetically.
		 *
		 * @returns A string representing this selector, minified.
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.selector).append("{");
			if (this.subSelectors != null) {
				for (Selector s : this.subSelectors) {
					sb.append(s.toString());
				}
			}
			if (this.properties != null) {
				for (Property p : this.properties) {
					sb.append(p.toString());
				}
			}
			if (sb.charAt(sb.length() - 1) == ';') {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("}");
			return sb.toString();
		}

		/**
		 * Sorts the properties array to enhance gzipping.
		 *
		 * @param properties The array to be sorted.
		 */
		private void sortProperties(Property[] properties) {
			Arrays.sort(properties);
		}
	}

	/**
	 * Represents a CSS property.
	 */
	private static class Property implements Comparable<Property> {
		protected String property;
		protected Part[] parts;

		/**
		 * Creates a new Property using the supplied strings. Parses out the values of
		 * the property selector.
		 *
		 * @param property The property; for example, "border: solid 1px red;" or
		 *                 "-moz-box-shadow: 3px 3px 3px rgba(255, 255, 0, 0.5);".
		 * @throws Exception If the property is incomplete and cannot be parsed.
		 */
		public Property(String property) throws IncompletePropertyException {
			ArrayList<String> parts = new ArrayList<String>();
			boolean bCanSplit = true;
			int j = 0;
			String substr;
			LOG.debug("\t\tExamining property: {}", property);
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
				throw new IncompletePropertyException(property);
			}

			String prop = parts.get(0).trim();
			if (!(prop.length() > 2 && prop.substring(0, 2).equals("--"))) {
				prop = prop.toLowerCase();
			}
			this.property = prop;
			this.parts = parseValues(simplifyColours(parts.get(1).trim().replaceAll(", ", ",")));
		}

		/**
		 * Prints out this property nicely.
		 *
		 * @returns A string representing this property, minified.
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.property).append(":");
			for (Part p : this.parts) {
				if (p != null) {
					sb.append(p.toString()).append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1); // Delete the trailing comma.
			sb.append(";");
			return sb.toString();
		}

		/**
		 * Compare this property with another.
		 */
		public int compareTo(Property other) {
			// We can't just use String.compareTo(), because we need to sort properties that
			// have hack prefixes last -- eg, *display should come after display.
			String thisProp = this.property;
			String thatProp = other.property;

			if (thisProp.charAt(0) == '-') {
				thisProp = thisProp.substring(1);
				thisProp = thisProp.substring(thisProp.indexOf('-') + 1);
			} else if (thisProp.charAt(0) < 65) {
				thisProp = thisProp.substring(1);
			}

			if (thatProp.charAt(0) == '-') {
				thatProp = thatProp.substring(1);
				thatProp = thatProp.substring(thatProp.indexOf('-') + 1);
			} else if (thatProp.charAt(0) < 65) {
				thatProp = thatProp.substring(1);
			}

			return thisProp.compareTo(thatProp);
		}

		/**
		 * Parse the values out of a property.
		 *
		 * @param contents The property to parse
		 * @returns An array of Parts
		 */
		private Part[] parseValues(String contents) {
			String[] parts = contents.split(",");
			List<Part> results = new ArrayList<>(parts.length);

			for (int i = 0; i < parts.length; i++) {
				try {
					results.add(new Part(parts[i], property));
				} catch (Exception e) {
					// Drop a part we can't parse rather than retaining a null, which
					// toString() would later dereference.
					LOG.debug("Exception in parseValues().", e);
				}
			}

			return results.toArray(new Part[0]);
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
				try {
					for (int i = 0; i < rgbColours.length; i++) {
						colourValue = Integer.parseInt(rgbColours[i]);
						if (colourValue < 16) {
							hexColour.append("0");
						}
						hexColour.append(Integer.toHexString(colourValue));
					}
				} catch (NumberFormatException e) {
					// A component is out of int range (or otherwise unparseable). Leave this
					// colour untouched rather than aborting minification, consistent with how
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
		 * @throws Exception If the part cannot be parsed.
		 */
		public Part(String contents, String property) throws Exception {
			// Many of these regular expressions are adapted from those used in the YUI CSS
			// Compressor.

			// For simpler regexes.
			this.contents = " " + contents;
			this.property = property;

			simplify();
		}

		private void simplify() {
			// !important doesn't need to be spaced
			this.contents = this.contents.replaceAll(" !important", "!important");

			// Replace 0in, 0cm, etc. with just 0
			this.contents = this.contents.replaceAll("(\\s)(0)(px|em|%|in|cm|mm|pc|pt|ex)", "$1$2");

			// Replace 0.6 with .6
			// Disabled, as it actually makes compression worse! People use rgba(0,0,0,0)
			// and rgba(0,0,0,0.x) a lot.
			// this.contents = this.contents.replaceAll("(\\s)0+\\.(\\d+)", "$1.$2");

			this.contents = this.contents.trim();

			// Simplify multiple zeroes
			if (this.contents.equals("0 0 0 0")) {
				this.contents = "0";
			}
			if (this.contents.equals("0 0 0")) {
				this.contents = "0";
			}
			if (this.contents.equals("0 0")) {
				this.contents = "0";
			}

			// Simplify multiple-parameter properties
			simplifyParameters();

			// Simplify font weights
			simplifyFontWeights();

			// Strip unnecessary quotes from url() and single-word parts, and make as much
			// lowercase as possible.
			simplifyQuotesAndCaps();

			// Simplify colours
			simplifyColourNames();
			simplifyHexColours();
		}

		private void simplifyParameters() {
			if (this.property.equals("background-size") || this.property.equals("quotes")
					|| this.property.equals("transform-origin") || this.property.equals("grid-template-columns")
					|| this.property.equals("grid-template-rows"))
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
				this.contents = this.contents.replaceAll("(?i)url\\(('|\")?(.*?)\\1\\)", "url($2)");
			} else if ((this.contents.length() > 4) && (this.contents.substring(0, 4).equalsIgnoreCase("var("))) {
				// We can't just remove all whitespace in the line, but we can ensure there's a maximum of one space in any run.
				// https://github.com/logicsquad/minifier/issues/5
				this.contents = this.contents.replaceAll("\\s{2,}", " ").trim();
			} else {
				String[] words = this.contents.split("\\s");
				if (words.length == 1) {
					if (!this.property.equalsIgnoreCase("animation-name")) {
						this.contents = this.contents.toLowerCase();
					}
					this.contents = this.contents.replaceAll("('|\")?(.*?)\1", "$2");
				}
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

		private void simplifyHexColours() {
			StringBuffer newContents = new StringBuffer();

			Pattern pattern = Pattern
					.compile("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])");
			Matcher matcher = pattern.matcher(this.contents);

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

			this.contents = newContents.toString();
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

	/**
	 * Exception representing an unterminated comment.
	 */
	private static class UnterminatedCommentException extends Exception {
		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Exception representing unbalanced braces.
	 */
	private static class UnbalancedBracesException extends Exception {
		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Exception representing an incomplete property.
	 */
	private static class IncompletePropertyException extends Exception {
		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor taking a message
		 *
		 * @param message a message
		 */
		public IncompletePropertyException(String message) {
			super(message);
		}
	}

	/**
	 * Exception representing an empty selector body.
	 */
	private static class EmptySelectorBodyException extends Exception {
		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor taking a message
		 *
		 * @param message a message
		 */
		public EmptySelectorBodyException(String message) {
			super(message);
		}
	}

	/**
	 * Exception representing an unterminated selector.
	 */
	private static class UnterminatedSelectorException extends Exception {
		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor taking a message
		 *
		 * @param message a message
		 */
		public UnterminatedSelectorException(String message) {
			super(message);
		}
	}

	/**
	 * Exception representing an incomplete selector.
	 */
	private static class IncompleteSelectorException extends Exception {
		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor taking a message
		 *
		 * @param message a message
		 */
		public IncompleteSelectorException(String message) {
			super(message);
		}
	}
}
