package com.bertvanbrakel.test.bean.random;

public class TstBeanMultiArgCtor {
	private final String fieldA;
	private final Integer fieldB;

	public TstBeanMultiArgCtor(String fieldA, int fieldB) {
		super();
		this.fieldA = fieldA;
		this.fieldB = fieldB;
	}

	public String getFieldA() {
		return fieldA;
	}

	public Integer getFieldB() {
		return fieldB;
	}
}