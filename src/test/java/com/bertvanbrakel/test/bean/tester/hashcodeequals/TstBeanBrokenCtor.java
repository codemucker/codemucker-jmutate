package com.bertvanbrakel.test.bean.tester.hashcodeequals;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.bertvanbrakel.test.bean.annotation.BeanProperty;

public class TstBeanBrokenCtor {
	private String fieldA;
	private String fieldB;

	public TstBeanBrokenCtor() {

	}

	public TstBeanBrokenCtor(
			@BeanProperty(name="fieldA") String fieldA, 
			@BeanProperty(name="fieldB") String fieldB) {
		super();
		this.fieldA = fieldA;
		// broken assignment we are testing
		this.fieldB = fieldA;
	}

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

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(17, 7, this);
	}

}
