package com.bertvanbrakel.test.bean.tester.hashcodeequals;

import org.apache.commons.lang.builder.EqualsBuilder;

public class TstBeanNonEqualHashcode {

	private String fieldA;
	private String fieldB;

	public String getFieldA() {
		return fieldA;
	}

	public void setFieldA(String fieldA) {
		this.fieldA = fieldA;
	}

	public String getFieldB() {
		return fieldB;
	}

	public void setFieldB(String fieldB) {
		this.fieldB = fieldB;
	}

	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other);
	}
}
