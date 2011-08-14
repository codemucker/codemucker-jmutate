package com.bertvanbrakel.test.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class PropertyDefinition {
	
	private String name;
	private Class<?> type;
	private Type genericType;
	private boolean ignore;;
	private boolean makeAccessible = false;

	private Field field;
	private Method read;
	private Method write;

	public boolean isMakeAccessible() {
    	return makeAccessible;
    }

	public void setMakeAccessible(boolean makeAccessible) {
    	this.makeAccessible = makeAccessible;
    }

	public Field getField() {
    	return field;
    }

	public void setField(Field field) {
    	this.field = field;
    }

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public Method getRead() {
		return read;
	}

	public void setRead(Method read) {
		this.read = read;
	}

	public Method getWrite() {
		return write;
	}

	public void setWrite(Method write) {
		this.write = write;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.MULTI_LINE_STYLE);
	}
}