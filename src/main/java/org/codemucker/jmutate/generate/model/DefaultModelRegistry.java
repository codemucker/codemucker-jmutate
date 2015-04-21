package org.codemucker.jmutate.generate.model;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.codemucker.jmutate.SourceLoader;

import com.google.inject.Inject;

public class DefaultModelRegistry implements ModelRegistry {

	private Map<String, ModelExtractor<?>> extractorsByModelTypeName = new HashMap<>();

	private final SourceLoader sourceLoader;
	private final boolean wrapExtractors = true;

	@Inject
	public DefaultModelRegistry(SourceLoader sourceLoader) {
		this.sourceLoader = sourceLoader;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <M> ModelExtractor<M> getExtractorForModel(Class<M> modelClass) {
		ModelExtractor<?> extractor = extractorsByModelTypeName.get(modelClass
				.getName());
		if (extractor == null) {
			throw new NoSuchElementException(
					"No extractor registered for model " + modelClass.getName());
		}
		return (ModelExtractor<M>) extractor;
	}

	public <M> void registerExtractor(ModelExtractor<M> extractor) {
		extractorsByModelTypeName.put(extractor.getModelClass().getName(),
				wrapExtractor(extractor));
	}

	private <M> ModelExtractor<M> wrapExtractor(ModelExtractor<M> extractor) {
		if (wrapExtractors) {
			return new CachingModelExtractor<>(sourceLoader, extractor);
		} else {
			return extractor;
		}
	}

}
