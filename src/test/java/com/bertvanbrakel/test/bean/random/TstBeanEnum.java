package com.bertvanbrakel.test.bean.random;


class TstBeanEnum {
	private static enum TstEnum {
		ONE, TWO, THREE;
	}

	TstBeanEnum.TstEnum enumField;

	public TstBeanEnum.TstEnum getEnumField() {
		return enumField;
	}

	public void setEnumField(TstBeanEnum.TstEnum enumField) {
		this.enumField = enumField;
	}
}