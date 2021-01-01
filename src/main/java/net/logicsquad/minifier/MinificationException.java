package net.logicsquad.minifier;

/**
 * Wrapper exception for problems during minification.
 * 
 * @author paulh
 */
public class MinificationException extends Exception {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor taking a message and causing {@link Throwable}.
	 * 
	 * @param message a message
	 * @param cause   underlying cause of failure
	 */
	public MinificationException(String message, Throwable cause) {
		super(message, cause);
	}
}
