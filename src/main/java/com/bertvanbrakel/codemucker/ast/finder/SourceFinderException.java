package com.bertvanbrakel.codemucker.ast.finder;

public class SourceFinderException extends RuntimeException {

	public SourceFinderException(String message, Throwable cause) {
		super(message, cause);
	}

	public SourceFinderException(String message) {
		super(message);
	}

	public SourceFinderException(String message, Object... args) {
		super(String.format(message, args));
	}

}
