package org.codemucker.jmutate.generate.model;

import java.lang.reflect.Method;

import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.model.pojo.CloneMethodExtractor;

public class TypeModelExtractor implements ModelExtractor<TypeModel> {

	private CloneMethodExtractor cloneExtractor = new CloneMethodExtractor();
	
	public static final String CLONE_KEY = "cloneMethod";
	
	@Override
	public Class<TypeModel> getModelClass() {
		return TypeModel.class;
	}
	
	@Override
	public TypeModel extractModelFromClass(String fullName) {
		return new TypeModel(fullName);
	}

	@Override
	public TypeModel extractModelFromClass(JType type) {
		TypeModel model = new TypeModel(type);
		JMethod cloneMethod = cloneExtractor.extractCloneMethodOrNull(type);
		if(cloneMethod != null){
			model.set(CLONE_KEY, new MethodModel(cloneMethod));
		}
		return model;
	}

	@Override
	public TypeModel extractModelFromClass(Class<?> type) {
		TypeModel model = new TypeModel(type);
		Method cloneMethod = cloneExtractor.extractCloneMethodOrNull(type);
		if(cloneMethod != null){
			model.set(CLONE_KEY, new MethodModel(cloneMethod));
		}
		return model;
	}
	
}
