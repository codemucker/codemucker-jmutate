package com.bertvanbrakel.codemucker.ast;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import com.bertvanbrakel.codemucker.util.JavaNameUtil;
import com.google.common.collect.Lists;

public class JAnnotation {
	
	public static int ANY_DEPTH = -1;
	public static int DIRECT_DEPTH = 1;

	private final Annotation annotation;

	public static boolean isAnnotationNode(ASTNode node){
		return node instanceof Annotation;
	}
	
	public static JAnnotation from(Annotation node){
		return new JAnnotation(node);
	}
	
	private JAnnotation(Annotation annotation) {
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
				JAnnotation anon = JAnnotation.from((Annotation)m);
				if(anon.isOfType(annotationClass)){
					return true;
				}
			}
		}
		return false;
	}

	public static <A extends java.lang.annotation.Annotation> JAnnotation getAnnotationOfType(ASTNode node, int maxDepth, Class<A> annotationClass) {
		for(Annotation a:JAnnotation.findAnnotations(node, maxDepth)){
			JAnnotation found = JAnnotation.from(a);
			if( found.isOfType(annotationClass)){
				return found;
			}
		}
		return null;
	}
	
	public static List<Annotation> findAnnotations(ASTNode node){
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
	public static List<Annotation> findAnnotations(ASTNode node, final int maxDepth){
		final List<Annotation> found = Lists.newArrayList();
		ASTVisitor visitor = new BaseASTVisitor(){
			int depth = -1;
			@Override
			public boolean visitNode(ASTNode node) {
				depth++;
				if( maxDepth >-1 && depth > maxDepth ){
					return false;
				}
				//if( depth > 0){ //in children
					if(node instanceof ImportDeclaration){
						return false;
					}
					
					if(node instanceof Annotation){
						found.add((Annotation)node);
					}
				//}
				return true;
			}
			
			@Override
			public void endVisitNode(ASTNode node) {
				depth--;
			}
		};
		node.accept(visitor);
		return found;
	}

	public String getValueForAttribute(String name){
		return getValueForAttribute(name,"");
	}
	
	public String getValueForAttribute(String name,String defaultValue){
		//TODO:handle nested annotations
		SimpleName val = null;
		if( annotation.isMarkerAnnotation()){
			val = null;
		}
		if( annotation instanceof SingleMemberAnnotation){
			if( "value".equals(name)){
				val = (SimpleName)((SingleMemberAnnotation)annotation).getValue();
			}	
		}
		if( annotation instanceof NormalAnnotation){
			NormalAnnotation normal = (NormalAnnotation)annotation;
			List<MemberValuePair> values = normal.values();
			for( MemberValuePair pair:values){
				if( name.equals(pair.getName().getIdentifier())){
					val = (SimpleName)pair.getValue();
					break;
				}
			}
		}
		
		return val==null?defaultValue:val.getIdentifier();
	}
}
