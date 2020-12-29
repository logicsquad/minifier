package net.logicsquad.minifier;

import java.io.Reader;
import java.io.Writer;

/**
 * Abstract parent class that {@link Minifier} implementations can extend.
 * Provides a constructor taking a {@link Reader}.
 *
 * @author paulh
 */
public abstract class AbstractMinifier implements Minifier {
	/**
	 * {@link Reader} supplying source content
	 */
	private final Reader reader;

	/**
	 * Constructor taking a {@link Reader} that will provide the input resource.
	 *
	 * @param reader a {@link Reader}
	 */
	public AbstractMinifier(Reader reader) {
		this.reader = reader;
		return;
	}

	@Override
	public abstract void minify(Writer writer);

	/**
	 * Returns {@link Reader} supplying source content for this object.
	 * 
	 * @return {@link Reader}
	 */
	protected Reader reader() {
		return reader;
	}
}
