package com.bertvanbrakel.test.bean.random;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class TstBeanSetters {
	Map<String, Object> fieldToValues = new HashMap<String, Object>();

	private int setterInvokeCount = 0;

	private void set(String fieldName, Object val) {
		fieldToValues.put(fieldName, val);
		setterInvokeCount++;
	}

	int testGetSetterInvokeCount() {
		return setterInvokeCount;
	}

	public void setPrimitiveBoolean(boolean val) {
		set("boolean", val);
	}

	public void setPrimitiveByte(byte val) {
		set("byte", val);
	}

	public void setPrimitiveChar(char val) {
		set("char", val);
	}

	public void setPrimitiveShort(short val) {
		set("short", val);
	}

	public void setPrimitiveInt(int val) {
		set("int", val);
	}

	public void setPrimitiveLong(long val) {
		set("long", val);
	}

	public void setPrimitiveFloat(float val) {
		set("float", val);
	}

	public void setPrimitiveDouble(double val) {
		set("double", val);
	}

	public void setString(String val) {
		set("String", val);
	}

	public void setBoolean(Boolean val) {
		set("Boolean", val);
	}

	public void setByte(Byte val) {
		set("Byte", val);
	}

	public void setCharacter(Character val) {
		set("Character", val);
	}

	public void setShort(Short val) {
		set("Short", val);
	}

	public void setInteger(Integer val) {
		set("Integer", val);
	}

	public void setLong(Long val) {
		set("Long", val);
	}

	public void setFloat(Float val) {
		set("Float", val);
	}

	public void setDouble(Double val) {
		set("Double", val);
	}

	public void setBigDecimal(BigDecimal val) {
		set("BigDecimal", val);
	}

	public void setBigInteger(BigInteger val) {
		set("BigInteger", val);
	}
}