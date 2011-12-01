package com.bertvanbrakel.codemucker.ast;

import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;

import com.bertvanbrakel.codemucker.util.JavaNameUtil;

public class JAnnotation {
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
}
