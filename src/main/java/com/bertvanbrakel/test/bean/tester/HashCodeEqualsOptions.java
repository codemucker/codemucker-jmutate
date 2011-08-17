package com.bertvanbrakel.test.bean.tester;

import com.bertvanbrakel.test.bean.random.RandomOptions;

public class HashCodeEqualsOptions extends RandomOptions {
	private boolean testCtors = true;
	private boolean testCtorsModifyEquals = true;
	private boolean testProperties = true;
	private boolean testCtorsArgsMatchProperties = true;
	
	public boolean isTestProperties() {
		return testProperties;
	}

	public HashCodeEqualsOptions testProperties(boolean testProperties) {
		this.testProperties = testProperties;
		return this;
	}

	public boolean isTestCtors() {
		return testCtors;
	}

	public HashCodeEqualsOptions testCtors(boolean testCtors) {
		this.testCtors = testCtors;
		return this;
	}

	public boolean isTestCtorsModifyEquals() {
		return testCtorsModifyEquals;
	}

	public HashCodeEqualsOptions testCtorsModifyEquals(boolean testCtorsModifyEquals) {
		this.testCtorsModifyEquals = testCtorsModifyEquals;
		return this;
	}

	public boolean isTestCtorsArgsMatchProperties() {
		return testCtorsArgsMatchProperties;
	}

	public HashCodeEqualsOptions testCtorsArgsMatchProperties(boolean testCtorsArgsMatchProperties) {
		this.testCtorsArgsMatchProperties = testCtorsArgsMatchProperties;
		return this;
	}

}
