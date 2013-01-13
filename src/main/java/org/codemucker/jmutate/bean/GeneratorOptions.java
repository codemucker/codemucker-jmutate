package org.codemucker.jmutate.bean;

import com.bertvanbrakel.test.bean.BeanOptions;

@Deprecated
public class GeneratorOptions extends BeanOptions {

	private boolean markeGeneratedClass = true;
	private boolean markeGeneratedFields = true;
	private boolean markeGeneratedMethods = true;

	private boolean markPatternOnMethod = true;
	private boolean markPatternOnClass = true;

	public boolean isMarkGeneratedAnything() {
		return markeGeneratedClass || markeGeneratedFields || markeGeneratedMethods;
	}

	public boolean isMarkPatternAnything() {
		return markPatternOnClass || markPatternOnMethod;
	}

	public boolean isMarkeGeneratedFields() {
		return markeGeneratedFields;
	}

	public GeneratorOptions setMarkeGeneratedFields(boolean markeGeneratedFields) {
		this.markeGeneratedFields = markeGeneratedFields;
		return this;
	}

	public boolean isMarkeGeneratedMethods() {
		return markeGeneratedMethods;
	}

	public GeneratorOptions setMarkeGeneratedMethods(boolean markeGeneratedMethods) {
		this.markeGeneratedMethods = markeGeneratedMethods;
		return this;
	}

	public boolean isMarkPatternOnClass() {
		return markPatternOnClass;
	}

	public GeneratorOptions setMarkPatternOnClass(boolean markPatternOnClass) {
		this.markPatternOnClass = markPatternOnClass;
		return this;
	}

	public GeneratorOptions setMarkeGeneratedClass(boolean markeGenerated) {
		this.markeGeneratedClass = markeGenerated;
		return this;
	}

	public GeneratorOptions setMarkPatternOnMethod(boolean markPattern) {
		this.markPatternOnMethod = markPattern;
		return this;
	}

	public boolean isMarkeGeneratedClass() {
		return markeGeneratedClass;
	}

	public boolean isMarkPatternOnMethod() {
		return markPatternOnMethod;
	}

}
