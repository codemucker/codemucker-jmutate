package com.bertvanbrakel.test.bean.random;

class TstBeanIgnoreProperty {
	private String fieldA;
	private String fieldB;
	private TstBeanIgnoreProperty fieldC;

	public TstBeanIgnoreProperty getFieldC() {
		return fieldC;
	}

	public void setFieldC(TstBeanIgnoreProperty fieldC) {
		this.fieldC = fieldC;
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

}