package org.codemucker.jmutate.generate.bean;

import java.lang.annotation.Annotation;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.AnnotationConfiguration;
import org.codemucker.jmutate.generate.GenerateOptions;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jpattern.generate.ClashStrategy;

import com.google.common.base.Preconditions;

/**
 * Generation options shared by all bean generators. Property names must match those on the annotation
 * @param <T> the annotation which will be used to configure these options. The annotation attribute names
 * shoud map to this options property names
 */
public abstract class AbstractBeanOptions<T extends Annotation> extends GenerateOptions<T> {

	private final TypeModel type;

	private boolean inheritParentProperties;
	private ClashStrategy clashStrategy;
	private String fieldNames;

	public AbstractBeanOptions(T anon, JType pojoType) {
		this(new AnnotationConfiguration(anon), (Class<T>)anon.getClass(),pojoType);
	}

	public AbstractBeanOptions(Configuration config, Class<T> annotationClass, JType pojoType) {
		super(config, annotationClass);
		Preconditions.checkNotNull(pojoType, "expect beantype to be set");
		this.type = new TypeModel(pojoType);
	}
	

	public ClashStrategy getClashStrategy() {
		return clashStrategy;
	}

	public String getFieldNames() {
		return fieldNames;
	}

	public boolean isInheritParentProperties() {
		return inheritParentProperties;
	}

	public TypeModel getType() {
		return type;
	}

	public void setInheritParentProperties(boolean inheritParentProperties) {
		this.inheritParentProperties = inheritParentProperties;
	}

	public void setClashStrategy(ClashStrategy clashStrategy) {
		this.clashStrategy = clashStrategy;
	}

	public void setFieldNames(String fieldNames) {
		this.fieldNames = fieldNames;
	}

}