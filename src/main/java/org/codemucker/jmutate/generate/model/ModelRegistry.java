package org.codemucker.jmutate.generate.model;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultModelRegistry.class)
public interface ModelRegistry {

	<M> ModelExtractor<M> getExtractorForModel(Class<M> modelClass);

}