package org.codemucker.jmutate.generate.matcher;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.TreeSet;

import org.codemucker.jpattern.generate.ClashStrategy;

/**
 * 
 * @param <T>
 *            type of the annotation used to populate this class
 */
public class AbstractMatcherModel<T extends Annotation> {

	public boolean enabled;
	public boolean markGenerated;
	public boolean inheritParentProperties;
	public ClashStrategy clashStrategy;
	public String fieldNames;
	public boolean keepInSync;
	public Set<String> staticBuilderMethodNames = new TreeSet<>();

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isMarkGenerated() {
		return markGenerated;
	}

	public boolean isInheritParentProperties() {
		return inheritParentProperties;
	}

	public ClashStrategy getClashStrategy() {
		return clashStrategy;
	}

	public String getFieldNames() {
		return fieldNames;
	}

	public boolean isKeepInSync() {
		return keepInSync;
	}

	public Set<String> getStaticBuilderMethodNames() {
		return staticBuilderMethodNames;
	}

	public void setStaticBuilderMethodNames(Set<String> staticBuilderMethodNames) {
		this.staticBuilderMethodNames = staticBuilderMethodNames;
	}

}
