package com.bertvanbrakel.test.bean.builder;

import com.bertvanbrakel.test.bean.BeanOptions;

public class BuilderOptions extends BeanOptions {

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

	public BuilderOptions setMarkeGeneratedFields(boolean markeGeneratedFields) {
		this.markeGeneratedFields = markeGeneratedFields;
		return this;
	}

	public boolean isMarkeGeneratedMethods() {
		return markeGeneratedMethods;
	}

	public BuilderOptions setMarkeGeneratedMethods(boolean markeGeneratedMethods) {
		this.markeGeneratedMethods = markeGeneratedMethods;
		return this;
	}

	public boolean isMarkPatternOnClass() {
		return markPatternOnClass;
	}

	public BuilderOptions setMarkPatternOnClass(boolean markPatternOnClass) {
		this.markPatternOnClass = markPatternOnClass;
		return this;
	}

	public BuilderOptions setMarkeGeneratedClass(boolean markeGenerated) {
		this.markeGeneratedClass = markeGenerated;
		return this;
	}

	public BuilderOptions setMarkPatternOnMethod(boolean markPattern) {
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
