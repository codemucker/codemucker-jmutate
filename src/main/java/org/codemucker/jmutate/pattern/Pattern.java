package org.codemucker.jmutate.pattern;

import org.codemucker.jmutate.transform.Transform;

/**
 * Marks a class as providing the ability to apply a code pattern to a class. A pattern typically involves invoking a number of smaller {@link Transform}'s
 */
public interface Pattern {

    /**
     * Apply the pattern after all the pattern args have been set
     */
    public void apply();
}
