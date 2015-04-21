package org.codemucker.jmutate.generate.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;

import com.google.inject.Inject;

public class CachingModelExtractor<M> implements ModelExtractor<M> {

	private static final Logger LOG = LogManager.getLogger(CachingModelExtractor.class);
	
	private final ModelExtractor<M> modelExtractor;
	private final SourceLoader sourceLoader;

	private final Map<String, TypeHolder<M>> typeModels = new HashMap<String, TypeHolder<M>>();

	
	@Inject
	public CachingModelExtractor(SourceLoader sourceLoader,ModelExtractor<M> modelExtractor) {
		super();
		this.sourceLoader = sourceLoader;
		this.modelExtractor = modelExtractor;
	}

	@Override
	public Class<M> getModelClass() {
		return modelExtractor.getModelClass();
	}
	
	@Override
	public M extractModelFromClass(String fullName) {
		JType type = sourceLoader.loadTypeForClass(fullName);
		if (type != null) {
			return extractModelFromClass(type);
		}
		Class<?> klass = sourceLoader.loadClassOrNull(fullName);
		if (klass != null) {
			return extractModelFromClass(klass);
		}
		LOG.warn("couldn't load source or class for '" + fullName + "'");
		M model = createModel(fullName);
		if (model != null) {
			newHolder(model, fullName, 0);
		}
		if (model == null) {
			LOG.warn("couldn't load source or class for '" + fullName + "', no model created");
		} else {
			LOG.warn("couldn't load source or class for '" + fullName + "', created simple model");		
		}
		return model;
	}

	@Override
	public M extractModelFromClass(Class<?> type) {
		String fullName = NameUtil.compiledNameToSourceName(type.getName());

		TypeHolder<M> holder = getHolderFor(fullName, 0);
		if (holder == null) {
			holder = newHolder(createModel(type),fullName, 0);
		}
		return holder.getModel();
	}

	@Override
	public M extractModelFromClass(JType type) {
		String fullName = type.getFullName();
		long version = type.getAstNode().getAST().modificationCount();
		TypeHolder<M> holder = getHolderFor(fullName, version);
		if (holder == null) {
			holder = newHolder(createModel(type),fullName, version);
		}
		return holder.getModel();
	}
	
	private TypeHolder<M> getHolderFor(String fullName, long version){
		TypeHolder<M> holder = typeModels.get(fullName);
		if(holder != null){
			if(holder.getVersion() < version){
				typeModels.remove(fullName);
				holder = null;
			}
		}
		return holder;		
	}
	
	private TypeHolder<M> newHolder(M model,String fullName,long version){
		TypeHolder<M> holder = new TypeHolder<M>(model, version);
		typeModels.put(fullName, holder);
	
		return holder;
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

	private class TypeHolder<T> {
		private final T model;
		private final long version;

		public TypeHolder(T model, long version) {
			super();
			this.model = model;
			this.version = version;
		}

		public T getModel() {
			return model;
		}

		public long getVersion() {
			return version;
		}

	}


}
