package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;

import org.apache.commons.configuration.Configuration;

public class GenerateOptions<T extends Annotation> {

	private boolean enabled;
	private boolean markGenerated;

	public GenerateOptions() {
	}
	
	public GenerateOptions(T annotation) {
		OptionsMapper.INSTANCE.mapFromTo(annotation, this);
	}
	
	public GenerateOptions(Configuration config,Class<T> annotationClass) {
		OptionsMapper.INSTANCE.mapFromTo(config, annotationClass, this);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isMarkGenerated() {
		return markGenerated;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setMarkGenerated(boolean markGenerated) {
		this.markGenerated = markGenerated;
	}

}
