package org.codemucker.jmutate.generate.model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.JType;

import com.google.inject.Inject;

public class CachingModelExtractor<M> extends AbstractCachingModelExtractor<M> {

	private static final Logger LOG = LogManager.getLogger(CachingModelExtractor.class);
	
	private final ModelExtractor<M> modelExtractor;
	
	@Inject
	public CachingModelExtractor(SourceLoader sourceLoader,ModelExtractor<M> modelExtractor) {
		super(sourceLoader);
		this.modelExtractor = modelExtractor;
	}

	@Override
	public Class<M> getModelClass() {
		return modelExtractor.getModelClass();
	}
	protected M createModel(JType type){
		return modelExtractor.extractModelFromClass(type);
	}
	
	protected M createModel(Class<?> type){
		return modelExtractor.extractModelFromClass(type);
	}
	
	protected M createModel(String fullTypeName){
		return modelExtractor.extractModelFromClass(fullTypeName);
	}

}
