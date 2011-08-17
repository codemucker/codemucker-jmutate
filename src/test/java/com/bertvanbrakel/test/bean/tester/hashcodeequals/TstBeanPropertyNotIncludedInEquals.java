package com.bertvanbrakel.test.bean.tester.hashcodeequals;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class TstBeanPropertyNotIncludedInEquals {
	private String fieldA;
	private String fieldIgnore;

	public String getFieldA() {
		return fieldA;
	}

	public void setFieldA(String fieldA) {
		this.fieldA = fieldA;
	}

	public String getFieldIgnore() {
		return fieldIgnore;
	}

	public void setFieldIgnore(String fieldB) {
		this.fieldIgnore = fieldB;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(11, 23, this);
	}

	@Override
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other, new String[] { "fieldIgnore" });
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
