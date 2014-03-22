package org.codemucker.jmutate.ast.finder;

public class JSourceFinderException extends RuntimeException {

	public JSourceFinderException(String message, Throwable cause) {
		super(message, cause);
	}

	public JSourceFinderException(String message) {
		super(message);
	}

	public JSourceFinderException(String message, Object... args) {
		super(String.format(message, args));
	}

}
