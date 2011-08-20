package com.bertvanbrakel.test.bean.tester.hashcodeequals;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TstBeanOk {

	private String fieldOne;
	private long fieldTwo;

	public TstBeanOk() {
	}

	public TstBeanOk(String fieldOne, long fieldTwo) {
		super();
		this.fieldOne = fieldOne;
		this.fieldTwo = fieldTwo;
	}

	public String getFieldOne() {
		return fieldOne;
	}

	public void setFieldOne(String fieldOne) {
		this.fieldOne = fieldOne;
	}

	public long getFieldTwo() {
		return fieldTwo;
	}

	public void setFieldTwo(long fieldTwo) {
		this.fieldTwo = fieldTwo;
	}

	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(17, 7, this);
	}
}
