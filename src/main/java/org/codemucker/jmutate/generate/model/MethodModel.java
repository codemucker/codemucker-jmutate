package org.codemucker.jmutate.generate.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.util.NameUtil;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * Single place to merge all method meta information from both source and compiled code
 */
public class MethodModel extends ModelObject {

	private final String name;
	private final ModifierModel modifiers;
	private final String returnTypeFull;
	private final List<Parameter> parameters;
	
	private TypeModel returnType;

	public MethodModel(Method m){
		name = m.getName();
		returnTypeFull = m.getReturnType().getName();
		modifiers = new ModifierModel(m.getModifiers());
		parameters = new ArrayList<>(m.getParameterCount());
		//TODO:use @Property for name?
		for(Class<?> paramType:m.getParameterTypes()){
			addParam(null, paramType.getName());
		}
	}
	
	public MethodModel(JMethod m){
		name = m.getName();
		returnTypeFull = m.getReturnTypeFullName();
		modifiers = new ModifierModel(m.getModifiers());
		
		List<SingleVariableDeclaration> params = m.getParameters();
		parameters = new ArrayList<>(params.size());
		for(SingleVariableDeclaration param:params){
			addParam(param.getName().toString(), NameUtil.resolveQualifiedName(param.getType()));
		}
	}
	
	private void addParam(String name, String fullType) {
		parameters.add(new Parameter(parameters.size(), name, fullType));
	}

	public String getName() {
		return name;
	}

	public boolean isVoidReturn() {
		return returnTypeFull == null || returnTypeFull.equals("void")
				|| returnTypeFull.equals("Void");
	}

	public TypeModel getReturnType() {
		if(returnType == null && returnTypeFull != null){
			returnType = new TypeModel(returnTypeFull, null);
		}
		return returnType;
	}

	public Parameter getParameter(int pos) {
		return parameters.get(pos);
	}
	
	public List<Parameter> getParameters() {
		return new ArrayList<>(parameters);
	}

	public int getParameterCount() {
		return parameters == null ? 0 : parameters.size();
	}

	public ModifierModel getModifiers() {
		return modifiers;
	}

	public static class Parameter {
		private final String name;
		private final int position;
		private final String fullType;
		private TypeModel type;

		public Parameter(int position, String name, String fullType) {
			super();
			this.position = position;
			this.name = name;
			this.fullType = fullType;
		}

		public String getName() {
			return name == null?"name" + position:name;
		}
		
		public String getRealName() {
			return name;
		}
		

		public String getFullType() {
			return fullType;
		}

		public TypeModel getType() {
			if (type == null) {
				type = new TypeModel(fullType, null);
			}
			return type;
		}

		public int getPosition() {
			return position;
		}

	}
}
