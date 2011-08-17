package com.bertvanbrakel.test.bean;

import java.lang.reflect.Type;

public class CtorArgDefinition {
	private String name;
	private Class<?> type;
	private Type genericType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isNamed() {
		return name != null;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public Type getGenericType() {
		return genericType;
	}

	public void setGenericType(Type genericType) {
		this.genericType = genericType;
	}
}
