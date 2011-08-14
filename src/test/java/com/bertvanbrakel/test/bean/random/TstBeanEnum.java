package com.bertvanbrakel.test.bean.random;

public class TstBeanEnum {
	private static enum TstEnum {
		ONE, TWO, THREE;
	}

	TstEnum enumField;

	public TstEnum getEnumField() {
		return enumField;
	}

	public void setEnumField(TstBeanEnum.TstEnum enumField) {
		this.enumField = enumField;
	}
}