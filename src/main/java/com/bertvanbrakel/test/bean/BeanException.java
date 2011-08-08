package com.bertvanbrakel.test.bean;

public class BeanException extends RuntimeException {

    private static final long serialVersionUID = -6472991937598744481L;

    public BeanException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanException(String message) {
        super(message);
    }

    public BeanException(String message, Object... args) {
        super(String.format(message, args));
    }

    public BeanException(String message, Throwable cause, Object... args) {
        super(String.format(message, args), cause);
    }

}