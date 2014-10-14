package org.codemucker.jmutate;

import org.codemucker.jmutate.ast.DefaultMutateContext;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultMutateContext.class)
public interface JMutateContext {

	/**
	 * Shortcut for {@link #obtain(SourceTemplate.class)}
	 * 
	 * @return
	 */
	SourceTemplate newSourceTemplate();
	
	/**
	 * Obtain an instance of the given class. This may be a singleton or a new instance and may or may
	 * not be fully initialised depending on the class. This allows callers to be decoupled from
	 * much of the initialisation and hence the need to pass loads of parameters around
	 * 
	 * @param type
	 * @return
	 */
	<T> T obtain(Class<T> type);
}
