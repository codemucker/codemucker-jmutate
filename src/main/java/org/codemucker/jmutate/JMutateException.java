package org.codemucker.jmutate;

public class JMutateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static enum ErrorTYPE {
        DEFAULT,COMPILE;
    }
    
	public JMutateException(String message, Throwable cause, Object... args) {
		super(String.format(message, args), cause);
	}

	public JMutateException(String message, Object... args) {
		super(String.format(message, args), null);
	}

	public JMutateException(String message, Throwable cause) {
		super(message, cause);
	}

	public JMutateException(String message) {
		super(message);
	}
}
