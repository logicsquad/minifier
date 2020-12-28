package net.logicsquad.minifier.js;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import net.logicsquad.minifier.AbstractMinifierTest;
import net.logicsquad.minifier.Minifier;

/**
 * Unit tests on {@link JSMinifier} class.
 * 
 * @author paulh
 */
public class JSMinifierTest extends AbstractMinifierTest {
	/**
	 * Indexes for input/output resources
	 */
	private static final List<String> RESOURCES = Arrays.asList("01", "02", "03");

	/**
	 * Extension for resource files
	 */
	private static final String EXTENSION = "js";

	@Override
	protected String extension() {
		return EXTENSION;
	}

	@Override
	protected Minifier miniferForReader(Reader reader) {
		return new JSMinifier(reader);
	}

	@Override
	protected List<String> resources() {
		return RESOURCES;
	}
}
