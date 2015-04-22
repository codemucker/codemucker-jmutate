package org.codemucker.jmutate.generate.model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.JType;

import com.google.inject.Inject;

/**
 * I load a fullname to a source or compiled type
 * @param <M>
 */
public abstract class AbstractModelExtractor<M> implements ModelExtractor<M> {

	private static final Logger LOG = LogManager.getLogger(AbstractModelExtractor.class);
	
	private final SourceLoader sourceLoader;

	@Inject
	public AbstractModelExtractor(SourceLoader sourceLoader) {
		super();
		this.sourceLoader = sourceLoader;
	}

	@Override
	public final M extractModelFromClass(String fullName) {
		JType type = sourceLoader.loadTypeForClass(fullName);
		if (type != null) {
			return extractModelFromClass(type);
		}
		Class<?> klass = sourceLoader.getResourceLoader().loadClassOrNull(fullName);
		if (klass != null) {
			return extractModelFromClass(klass);
		}
		LOG.warn("couldn't load source or class for '" + fullName + "'");
		return extractModelFromClassNoSourceOrCompiled(fullName);
	}
	
	protected M extractModelFromClassNoSourceOrCompiled(String fullName){
		throw new RuntimeException("could not load source or compiled class for '" + fullName + "'");
	}
}
