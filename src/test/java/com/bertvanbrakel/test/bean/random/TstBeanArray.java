package com.bertvanbrakel.test.bean.random;

import java.math.BigDecimal;
import java.math.BigInteger;


class TstBeanArray {
	private String[] stringArray;
	private Float[] floatArray;
	private Character[] charArray;

	public Float[] getFloatArray() {
		return floatArray;
	}

	public void setFloatArray(Float[] floatArray) {
		this.floatArray = floatArray;
	}

	public Integer[] getIntegerArray() {
		return integerArray;
	}

	public void setIntegerArray(Integer[] integerArray) {
		this.integerArray = integerArray;
	}

	public Boolean[] getBooleanArray() {
		return booleanArray;
	}

	public void setBooleanArray(Boolean[] booleanArray) {
		this.booleanArray = booleanArray;
	}

	public Long[] getLongArray() {
		return longArray;
	}

	public void setLongArray(Long[] longArray) {
		this.longArray = longArray;
	}

	public Double[] getDoubleArray() {
		return doubleArray;
	}

	public void setDoubleArray(Double[] doubleArray) {
		this.doubleArray = doubleArray;
	}

	public BigDecimal[] getBigDecimalArray() {
		return bigDecimalArray;
	}

	public void setBigDecimalArray(BigDecimal[] bigDecimalArray) {
		this.bigDecimalArray = bigDecimalArray;
	}

	public BigInteger[] getBigIntegerArray() {
		return bigIntegerArray;
	}

	public void setBigIntegerArray(BigInteger[] bigIntegerArray) {
		this.bigIntegerArray = bigIntegerArray;
	}

	private Integer[] integerArray;
	private Boolean[] booleanArray;
	private Long[] longArray;
	private Double[] doubleArray;
	private BigDecimal[] bigDecimalArray;
	private BigInteger[] bigIntegerArray;

	private TstBeanNoArgCtor[] beanArray;

	public String[] getStringArray() {
		return stringArray;
	}

	public void setStringArray(String[] stringArray) {
		this.stringArray = stringArray;
	}

	public Character[] getCharArray() {
		return charArray;
	}

	public void setCharArray(Character[] charArray) {
		this.charArray = charArray;
	}

	public TstBeanNoArgCtor[] getBeanArray() {
		return beanArray;
	}

	public void setBeanArray(TstBeanNoArgCtor[] beanArray) {
		this.beanArray = beanArray;
	}

}