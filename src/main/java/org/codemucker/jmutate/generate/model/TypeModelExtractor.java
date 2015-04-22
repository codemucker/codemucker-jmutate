package org.codemucker.jmutate.generate.model;

import java.lang.reflect.Method;

import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.model.pojo.CloneMethodExtractor;

import com.google.inject.Inject;

public class TypeModelExtractor extends AbstractCachingModelExtractor<TypeModel> {

	@Inject
	public TypeModelExtractor(SourceLoader sourceLoader) {
		super(sourceLoader);
	}

	private CloneMethodExtractor cloneExtractor = new CloneMethodExtractor();
	
	public static final String CLONE_KEY = "cloneMethod";
	
	@Override
	public Class<TypeModel> getModelClass() {
		return TypeModel.class;
	}
	
	@Override
	protected TypeModel createModel(String fullName) {
		return new TypeModel(fullName);
	}

	@Override
	protected TypeModel createModel(JType type) {
		TypeModel model = new TypeModel(type);
		JMethod cloneMethod = cloneExtractor.extractCloneMethodOrNull(type);
		if(cloneMethod != null){
			model.set(CLONE_KEY, new MethodModel(cloneMethod));
		}
		return model;
	}

	@Override
	protected TypeModel createModel(Class<?> type) {
		TypeModel model = new TypeModel(type);
		Method cloneMethod = cloneExtractor.extractCloneMethodOrNull(type);
		if(cloneMethod != null){
			model.set(CLONE_KEY, new MethodModel(cloneMethod));
		}
		return model;
	}
	
}
