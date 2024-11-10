package net.logicsquad.minifier.css;

import java.io.Reader;

import net.logicsquad.minifier.AbstractMinifierTest;
import net.logicsquad.minifier.Minifier;

/**
 * Unit tests on {@link Minifier} class.
 *
 * @author paulh
 */
public class CSSMinifierTest extends AbstractMinifierTest {
	/**
	 * Count of input/output resources
	 */
	private static final int RESOURCE_COUNT = 29;

	/**
	 * Extension for resource files
	 */
	private static final String EXTENSION = "css";

	@Override
	protected String extension() {
		return EXTENSION;
	}

	@Override
	protected Minifier miniferForReader(Reader reader) {
		return new CSSMinifier(reader);
	}

	@Override
	protected int resourceCount() {
		return RESOURCE_COUNT;
	}
}
