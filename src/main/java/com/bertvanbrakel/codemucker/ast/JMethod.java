package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class JMethod {

	private final JType parentType;
	private final MethodDeclaration methodNode;

	public JMethod(JType parentType, MethodDeclaration methodNode) {
		checkNotNull(parentType, "expect parent java type");
		checkNotNull(methodNode, "expect java method node");

		this.parentType = parentType;
		this.methodNode = methodNode;
	}

	public JType getParentType() {
    	return parentType;
    }

	public MethodDeclaration getMethodNode() {
    	return methodNode;
    }
	
	public String getName(){
		return methodNode.getName().getIdentifier();
	}

	public boolean isConstructor() {
	    return methodNode.isConstructor();
    }
	
	@SuppressWarnings("unchecked")
    public JModifiers getJavaModifiers(){
		return new JModifiers(methodNode.getAST(),methodNode.modifiers());
	}

	@SuppressWarnings("unchecked")
    public <A extends Annotation> boolean hasParameterAnnotationOfType(Class<A> annotationClass) {
        List<SingleVariableDeclaration> parameters = methodNode.parameters();
		for( SingleVariableDeclaration param:parameters){
			if( JAnnotation.hasAnnotation(annotationClass, param.modifiers())){
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
    public <A extends Annotation> boolean hasAnnotationOfType(Class<A> annotationClass) {
		return JAnnotation.hasAnnotation(annotationClass, methodNode.modifiers());
	}

	public <A extends Annotation> JAnnotation getAnnotationOfType(Class<A> annotationClass) {
		return JAnnotation.getAnnotationOfType(methodNode, JAnnotation.DIRECT_DEPTH, annotationClass);
	}
	
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations(){
		return JAnnotation.findAnnotations(methodNode, JAnnotation.DIRECT_DEPTH);
	}
}
