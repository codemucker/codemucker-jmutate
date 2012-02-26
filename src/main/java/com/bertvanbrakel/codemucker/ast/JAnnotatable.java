package com.bertvanbrakel.codemucker.ast;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface JAnnotatable {

    public <A extends Annotation> boolean hasAnnotationOfType(Class<A> annotationClass);

	public <A extends Annotation> JAnnotation getAnnotationOfType(Class<A> annotationClass);
	
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations();}
