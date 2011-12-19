package com.bertvanbrakel.codemucker.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import com.bertvanbrakel.codemucker.util.JavaNameUtil;

public class JAnnotation {
	
	public static int ANY_DEPTH = -1;
	public static int DIRECT_DEPTH = 0;

	private final Annotation annotation;

	public JAnnotation(Annotation annotation) {
		super();
		this.annotation = annotation;
	}

	public <A extends java.lang.annotation.Annotation> boolean isOfType(Class<A> annotationClass) {
		String fqn = getQualifiedName();
		//TODO:we don't have an '$' in the name when anon is a member/static type of a class!
		return annotationClass.getName().equals(fqn);
	}

	public String getQualifiedName() {
		return JavaNameUtil.getQualifiedName(annotation.getTypeName());
	}
	
	public static <A extends java.lang.annotation.Annotation> boolean hasAnnotation(Class<A> annotationClass, List<IExtendedModifier> modifiers){
		for( IExtendedModifier m:modifiers){
			if( m instanceof Annotation){
				JAnnotation anon = new JAnnotation((Annotation)m);
				if( anon.isOfType(annotationClass)){
					return true;
				}
			}
		}
		return false;
	}

	public static <A extends java.lang.annotation.Annotation> JAnnotation getAnnotationOfType(ASTNode node, int maxDepth, Class<A> annotationClass) {
		for(org.eclipse.jdt.core.dom.Annotation a:JAnnotation.findAnnotations(node, maxDepth)){
			JAnnotation found = new JAnnotation(a);
			if( found.isOfType(annotationClass)){
				return found;
			}
		}
		return null;
	}
	
	public static List<org.eclipse.jdt.core.dom.Annotation> findAnnotations(ASTNode node){
		return JAnnotation.findAnnotations(node, ANY_DEPTH);
	}

	/**
	 * Find all the annotations on the given node, looking up to maxDepth deep for annotation.
	 * 
	 * @param node
	 * @param maxDepth how deep to search for. Useful if you want to exclude nested classes in the 
	 * search. -1 = any depth, 0=directly on current node only 
	 * @return
	 */
	public static List<org.eclipse.jdt.core.dom.Annotation> findAnnotations(ASTNode node, int maxDepth){
		final List<org.eclipse.jdt.core.dom.Annotation> annons = new ArrayList<org.eclipse.jdt.core.dom.Annotation>();
		BaseASTVisitor visitor = new IgnoreableChildTypesVisitor(maxDepth){	
			@Override
            public boolean visit(ImportDeclaration node) {
	    		return false;
			}
			
			@Override
			public boolean visit(MarkerAnnotation node) {
				annons.add(node);
				return false;
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				annons.add(node);
				return false;
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				annons.add(node);
				return false;
			}
		};
		node.accept(visitor);
		return annons;
	}
	
}
