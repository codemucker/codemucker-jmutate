package com.bertvanbrakel.test.bean.builder;

public class BeanGenerationException extends RuntimeException {

	public BeanGenerationException(String message, Throwable cause, Object... args) {
		super(String.format(message, args), cause);
	}

	public BeanGenerationException(String message, Object... args) {
		super(String.format(message, args), null);
	}
	
	public BeanGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

	public BeanGenerationException(String message) {
		super(message);
	}

}
