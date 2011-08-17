package com.bertvanbrakel.test.bean.tester.hashcodeequals;

public class TstBeanHashCodeChangesPerInvocation {

	private String fieldA;

	private int fieldB = 0;

	public String getFieldA() {
		return fieldA;
	}

	public void setFieldA(String fieldA) {
		this.fieldA = fieldA;
	}

	@Override
	public int hashCode() {
		return fieldB++;
	}

}
