/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.test.bean.random;

import java.math.BigDecimal;
import java.math.BigInteger;


public class TstBeanArray {
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