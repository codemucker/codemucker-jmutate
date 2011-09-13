package com.bertvanbrakel.codemucker.bean;

public class BeanBuilderOptions extends GeneratorOptions {

	private boolean cacheProperties = true;
	private boolean generateBeanCopy = false;
	private boolean generateBeanEquals = false;

	public boolean isGenerateBeanEquals() {
		return generateBeanEquals;
	}

	public BeanBuilderOptions setGenerateBeanEquals(boolean generateBeanEquals) {
		this.generateBeanEquals = generateBeanEquals;
		return this;
	}

	public boolean isCacheProperties() {
		return cacheProperties;
	}

	public BeanBuilderOptions setCacheProperties(boolean cacheProperties) {
		this.cacheProperties = cacheProperties;
		return this;
	}

	public boolean isGenerateBeanCopy() {
		return generateBeanCopy;
	}

	public BeanBuilderOptions setGenerateBeanCopy(boolean generateBeanCopy) {
		this.generateBeanCopy = generateBeanCopy;
		return this;
	}

}
