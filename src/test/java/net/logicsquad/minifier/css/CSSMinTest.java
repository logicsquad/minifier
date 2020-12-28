package net.logicsquad.minifier.css;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.logicsquad.minifier.AbstractTest;

/**
 * Unit tests on {@link Minifier} class.
 * 
 * @author paulh
 */
public class CSSMinTest extends AbstractTest {
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

	@Test
	public void actualOutputMatchesExpected() throws IOException {
		for (String index : RESOURCES) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			CSSMin.formatFile(readerForSourceFile(index), out);
			String expected = stringForExpectedFile(index);
			String actual = new String(out.toByteArray(), StandardCharsets.UTF_8);
			// trim() here because there seems to be a difference in line endings
			assertEquals(expected.trim(), actual.trim());
		}
		return;
	}
}
