package org.codemucker.jmutate.ast;

public class MutateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public MutateException(String message, Throwable cause, Object... args) {
		super(String.format(message, args), cause);
	}

	public MutateException(String message, Object... args) {
		super(String.format(message, args), null);
	}

	public MutateException(String message, Throwable cause) {
		super(message, cause);
	}

	public MutateException(String message) {
		super(message);
	}
}
