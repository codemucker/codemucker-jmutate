package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
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
		for(org.eclipse.jdt.core.dom.Annotation a:getAnnotations()){
			JAnnotation found = new JAnnotation(a);
			if( found.isOfType(annotationClass)){
				return found;
			}
		}
		return null;
	}
	
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations(){
		final List<org.eclipse.jdt.core.dom.Annotation> annons = new ArrayList<org.eclipse.jdt.core.dom.Annotation>();
		
		BaseASTVisitor visitor = new BaseASTVisitor(){
			@Override
            public boolean visit(ImportDeclaration node) {
	            //super.visit(node);
				return false;
			}
			
			@Override
			public boolean visit(MarkerAnnotation node) {
				// return super.visit(node);
				annons.add(node);
				return false;
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				// return super.visit(node);
				annons.add(node);
				return false;
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				// return super.visit(node);
				annons.add(node);
				return false;
			}
		};
		methodNode.accept(visitor);
		return annons;
	}
}
