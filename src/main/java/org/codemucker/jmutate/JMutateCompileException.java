package org.codemucker.jmutate;

public class JMutateCompileException extends JMutateException {

    private static final long serialVersionUID = 1L;

    public JMutateCompileException(String message, Object... args) {
        super(message, args);
    }

    public JMutateCompileException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }

    public JMutateCompileException(String message, Throwable cause) {
        super(message, cause);
    }

    public JMutateCompileException(String message) {
        super(message);
    }

}
