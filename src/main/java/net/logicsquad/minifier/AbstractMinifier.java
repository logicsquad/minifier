package net.logicsquad.minifier;

import java.io.Reader;
import java.io.Writer;

public abstract class AbstractMinifier implements Minifier {
	protected final Reader reader;

	public AbstractMinifier(Reader reader) {
		this.reader = reader;
		return;
	}

	@Override
	public abstract void minify(Writer writer);
}
