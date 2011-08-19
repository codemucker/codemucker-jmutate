package com.bertvanbrakel.test.finder;

public class ClassFinderException extends RuntimeException {

	public ClassFinderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClassFinderException(String message) {
		super(message);
	}

	public ClassFinderException(String message, Object... args) {
		super(String.format(message, args));
	}

}
