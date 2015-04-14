package org.codemucker.jmutate.generate.builder;

import org.codemucker.jpattern.generate.ClashStrategy;

public class BuilderOptions {

	private String buildMethodName;
	private String builderCreateMethodNames;
	private boolean markGenerated;
	private boolean markCtorArgsAsProperties;
	private boolean supportSubclassing;
	private boolean generateStaticBuilderCreateMethod;
	private boolean generateAddRemoveMethodsForIndexProperties;
	private boolean generateCreateFromBean;
	private boolean inheritSuperBeanBuilder;
	private boolean inheritSuperBeanProperties;
	private boolean generateStaticBuilderCreateMethodOnBuilder;
	private ClashStrategy clashStrategy;
	private String fieldNames;

	public ClashStrategy getClashStrategy() {
		return clashStrategy;
	}

	public void setClashStrategy(ClashStrategy clashStrategy) {
		this.clashStrategy = clashStrategy;
	}

	public String getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(String fieldNames) {
		this.fieldNames = fieldNames;
	}

	public String getBuildMethodName() {
		return buildMethodName;
	}

	public void setBuildMethodName(String buildMethodName) {
		this.buildMethodName = buildMethodName;
	}

	public String getBuilderCreateMethodNames() {
		return builderCreateMethodNames;
	}

	public void setBuilderCreateMethodNames(String builderCreateMethodNames) {
		this.builderCreateMethodNames = builderCreateMethodNames;
	}

	public boolean isMarkGenerated() {
		return markGenerated;
	}

	public void setMarkGenerated(boolean markGenerated) {
		this.markGenerated = markGenerated;
	}

	public boolean isMarkCtorArgsAsProperties() {
		return markCtorArgsAsProperties;
	}

	public void setMarkCtorArgsAsProperties(boolean markCtorArgsAsProperties) {
		this.markCtorArgsAsProperties = markCtorArgsAsProperties;
	}

	public boolean isSupportSubclassing() {
		return supportSubclassing;
	}

	public void setSupportSubclassing(boolean supportSubclassing) {
		this.supportSubclassing = supportSubclassing;
	}

	public boolean isGenerateStaticBuilderCreateMethod() {
		return generateStaticBuilderCreateMethod;
	}

	public void setGenerateStaticBuilderCreateMethod(
			boolean generateStaticBuilderCreateMethod) {
		this.generateStaticBuilderCreateMethod = generateStaticBuilderCreateMethod;
	}

	public boolean isGenerateAddRemoveMethodsForIndexProperties() {
		return generateAddRemoveMethodsForIndexProperties;
	}

	public void setGenerateAddRemoveMethodsForIndexProperties(
			boolean generateAddRemoveMethodsForIndexProperties) {
		this.generateAddRemoveMethodsForIndexProperties = generateAddRemoveMethodsForIndexProperties;
	}

	public boolean isGenerateCreateFromBean() {
		return generateCreateFromBean;
	}

	public void setGenerateCreateFromBean(boolean generateCreateFromBean) {
		this.generateCreateFromBean = generateCreateFromBean;
	}

	public boolean isInheritSuperBeanBuilder() {
		return inheritSuperBeanBuilder;
	}

	public void setInheritSuperBeanBuilder(boolean inheritSuperBeanBuilder) {
		this.inheritSuperBeanBuilder = inheritSuperBeanBuilder;
	}

	public boolean isInheritSuperBeanProperties() {
		return inheritSuperBeanProperties;
	}

	public void setInheritSuperBeanProperties(boolean inheritSuperBeanProperties) {
		this.inheritSuperBeanProperties = inheritSuperBeanProperties;
	}

	public boolean isGenerateStaticBuilderCreateMethodOnBuilder() {
		return generateStaticBuilderCreateMethodOnBuilder;
	}

	public void setGenerateStaticBuilderCreateMethodOnBuilder(
			boolean generateStaticBuilderCreateMethodOnBuilder) {
		this.generateStaticBuilderCreateMethodOnBuilder = generateStaticBuilderCreateMethodOnBuilder;
	}

}
