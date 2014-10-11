package org.codemucker.jmutate;

/**
 * How to resolve the situation when there is already an existing matching AST node when we try to add a new one
 */
public enum ClashStrategy {
    /** Replace the existing node*/
	REPLACE,
	/**
	 * Ignore the new node (don't add it)
	 */
	IGNORE,
	/**
	 * Throw an error
	 */
	ERROR;
}
