package org.codemucker.jmutate.generate.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;

import com.google.inject.Inject;

public abstract class AbstractCachingModelExtractor<M> extends AbstractModelExtractor<M> {

	private static final Logger LOG = LogManager.getLogger(AbstractCachingModelExtractor.class);

	private final Map<String, TypeHolder<M>> typeModels = new HashMap<String, TypeHolder<M>>();

	
	@Inject
	public AbstractCachingModelExtractor(SourceLoader sourceLoader) {
		super(sourceLoader);
	}

	@Override
	public final M extractModelFromClass(Class<?> type) {
		String fullName = NameUtil.compiledNameToSourceName(type.getName());

		TypeHolder<M> holder = getHolderFor(fullName, 0);
		if (holder == null) {
			holder = newHolder(createModel(type),fullName, 0);
		}
		return holder.getModel();
	}

	@Override
	public final M extractModelFromClass(JType type) {
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
	
	protected abstract M createModel(JType type);
	
	protected abstract M createModel(Class<?> type);
	
	protected abstract M createModel(String fullTypeName);
	

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
