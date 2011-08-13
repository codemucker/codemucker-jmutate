package com.bertvanbrakel.test.bean;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class BeanDefinition {
	
	private final Class<?> beanType;
	private Constructor ctor;
	private Map<String, Property> properties;

	public BeanDefinition(Class<?> type) {
		this.beanType = type;
	}

	public void setCtor(Constructor ctor) {
		this.ctor = ctor;
	}

	public Constructor getCtor() {
		return ctor;
	}

	public Map<String, Property> getPropertyMap() {
		return properties;
	}

	public Collection<Property> getProperties() {
		return properties == null ? Collections.EMPTY_LIST : properties.values();
	}

	public void setPropertyMap(Map<String, Property> properties) {
		this.properties = properties;
	}
}