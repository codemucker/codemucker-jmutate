package org.codemucker.jmutate.bean;

@Deprecated
public class BeanBuilderOptions extends GeneratorOptions {

	/**
	 * If true then the builder will cache properties before creating the bean. Else sets
	 * on bean directly
	 */
	private boolean cacheProperties = true;
	
	/**
	 * If true then generate a bean copy method
	 */
	private boolean generateBeanCopy = false;
	
	/**
	 * If true then generate a bean equals method
	 */
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
