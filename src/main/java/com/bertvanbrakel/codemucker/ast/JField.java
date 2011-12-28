package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class JField {

	private final JType parentType;
	private final FieldDeclaration fieldNode;
	
	public JField(JType parentType, FieldDeclaration fieldNode) {
		checkNotNull(parentType, "expect parent java type");
		checkNotNull(fieldNode, "expect java field node");

		this.parentType = parentType;
		this.fieldNode = fieldNode;
	}

	public JType getParentType() {
    	return parentType;
    }
	
	public FieldDeclaration getFieldNode() {
    	return fieldNode;
    }
	
	public boolean hasName(String name){
		return getNames().contains(name);
	}
	
	public boolean isType(JField field){
		return isType(field.getFieldNode().getType());
	}
	
	public boolean isType(Type type){
		return fieldNode.getType().equals(type) ;
	}
	
	

	public List<String> getNames(){
		final List<String> names = newArrayList();
		BaseASTVisitor visitor = new BaseASTVisitor(){
			@Override
            public boolean visit(VariableDeclarationFragment node) {
				names.add(node.getName().getIdentifier());
				return false;
            }
		};
		fieldNode.accept(visitor);
		return names;
	}

	public boolean isAccess(JAccess access) {
		return getJavaModifiers().asAccess().equals(access);
	}
	
	@SuppressWarnings("unchecked")
    public JModifiers getJavaModifiers(){
		return new JModifiers(fieldNode.getAST(),fieldNode.modifiers());
	}

	@SuppressWarnings("unchecked")
    public <A extends Annotation> boolean hasAnnotationOfType(Class<A> annotationClass) {
		return JAnnotation.hasAnnotation(annotationClass, fieldNode.modifiers());
	}

	public <A extends Annotation> JAnnotation getAnnotationOfType(Class<A> annotationClass) {
		return JAnnotation.getAnnotationOfType(fieldNode, JAnnotation.ANY_DEPTH, annotationClass);
	}
	
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations(){
		return JAnnotation.findAnnotations(fieldNode);
	}
}
