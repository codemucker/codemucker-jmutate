package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;

import org.apache.commons.configuration.Configuration;

public class GenerateOptions<T extends Annotation> {

	public GenerateOptions() {
	}
	
	public GenerateOptions(T annotation) {
		OptionsMapper.INSTANCE.mapFromTo(annotation, this);
	}
	
	public GenerateOptions(Configuration config,Class<T> annotationClass) {
		OptionsMapper.INSTANCE.mapFromTo(config, annotationClass, this);
	}
}
