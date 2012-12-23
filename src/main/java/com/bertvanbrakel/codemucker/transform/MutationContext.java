package com.bertvanbrakel.codemucker.transform;

import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.google.inject.ImplementedBy;

@ImplementedBy(SimpleMutationContext.class)
public interface MutationContext {

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
