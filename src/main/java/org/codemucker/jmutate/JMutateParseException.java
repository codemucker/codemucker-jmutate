package org.codemucker.jmutate;

public class JMutateParseException extends JMutateException {

    private static final long serialVersionUID = 1L;

    public JMutateParseException(String message, Object... args) {
        super(message, args);
    }

    public JMutateParseException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }

    public JMutateParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JMutateParseException(String message) {
        super(message);
    }

}
