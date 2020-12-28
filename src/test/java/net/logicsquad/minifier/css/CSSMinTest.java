package net.logicsquad.minifier.css;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import net.logicsquad.minifier.AbstractMinifierTest;
import net.logicsquad.minifier.Minifier;

/**
 * Unit tests on {@link Minifier} class.
 * 
 * @author paulh
 */
public class CSSMinTest extends AbstractMinifierTest {
	/**
	 * Indexes for input/output resources
	 */
	private static final List<String> RESOURCES = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09",
			"10", "11", "12", "13", "14", "15", "16", "17", "18");

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
		return new CSSMin(reader);
	}

	@Override
	protected List<String> resources() {
		return RESOURCES;
	}
}
