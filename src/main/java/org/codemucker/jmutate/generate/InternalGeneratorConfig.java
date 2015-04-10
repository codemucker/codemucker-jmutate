package org.codemucker.jmutate.generate;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.ast.JAnnotation;
import org.eclipse.jdt.core.dom.Annotation;

/**
 * Extracts the config from either a source or compiled annotation
 */
class InternalGeneratorConfig {
	
	private Configuration config;
	private final String annotationType;
	
	public InternalGeneratorConfig(Annotation a){
		JAnnotation annotation = JAnnotation.from(a);
		this.annotationType = annotation.getQualifiedName();
		this.config = new AnnotationConfiguration(a);
	}

	public InternalGeneratorConfig(java.lang.annotation.Annotation a){
		this.annotationType = a.getClass().getName();
		this.config = new AnnotationConfiguration(a);
	}

	public void addParentConfig(Configuration parent){
		CompositeConfiguration combined = new CompositeConfiguration();
		//first one wins
		combined.addConfiguration(config);
		combined.addConfiguration(parent);
		
		this.config = combined;
	}
	
	public Configuration getConfig() {
		return config;
	}

	public String getAnnotationType() {
		return annotationType;
	}
}