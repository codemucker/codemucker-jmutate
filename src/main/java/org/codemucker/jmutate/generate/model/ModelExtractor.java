package org.codemucker.jmutate.generate.model;

import org.codemucker.jmutate.ast.JType;

/**
 * 
 * @param <M> the model to extract
 */
public interface ModelExtractor<M> {

	public Class<M> getModelClass();
	public M extractModelFromClass(String fullName);		
	public M extractModelFromClass(JType type);		
	public M extractModelFromClass(Class<?> type);		
}
