package org.codemucker.jmutate.transform;

import org.codemucker.jmutate.pattern.Pattern;

/**
 * Marks a class as providing the ability to provide a transform to an AST (change it). This is usually as part of a larger {@link Pattern} being applied
 */
public interface Transform {

    /**
     * Invoke the transform. Expected that all transform params have been set
     */
	public void transform();
}
