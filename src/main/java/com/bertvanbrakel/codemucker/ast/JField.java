package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class JField implements JAnnotatable, AstNodeProvider<FieldDeclaration> {

	private final FieldDeclaration fieldNode;
	
	public JField(FieldDeclaration fieldNode) {
		checkNotNull(fieldNode, "expect field declaration");
		this.fieldNode = fieldNode;
	}

	@Override
	public FieldDeclaration getAstNode(){
		return fieldNode;
	}

	public boolean hasName(String name){
		return getNames().contains(name);
	}
	
	public boolean isType(JField field){
		return isType(field.getAstNode().getType());
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
	@Override
    public <A extends Annotation> boolean hasAnnotationOfType(Class<A> annotationClass) {
		return JAnnotation.hasAnnotation(annotationClass, fieldNode.modifiers());
	}

	@Override
	public <A extends Annotation> JAnnotation getAnnotationOfType(Class<A> annotationClass) {
		return JAnnotation.getAnnotationOfType(fieldNode, JAnnotation.ANY_DEPTH, annotationClass);
	}
	
	@Override
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations(){
		return JAnnotation.findAnnotations(fieldNode);
	}
}
