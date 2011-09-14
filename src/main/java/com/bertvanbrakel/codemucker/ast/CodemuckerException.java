package com.bertvanbrakel.codemucker.ast;

public class CodemuckerException extends RuntimeException {
	public CodemuckerException(String message, Throwable cause, Object... args) {
		super(String.format(message, args), cause);
	}

	public CodemuckerException(String message, Object... args) {
		super(String.format(message, args), null);
	}

	public CodemuckerException(String message, Throwable cause) {
		super(message, cause);
	}

	public CodemuckerException(String message) {
		super(message);
	}
}
