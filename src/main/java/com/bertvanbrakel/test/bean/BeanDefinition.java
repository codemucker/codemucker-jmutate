package com.bertvanbrakel.test.bean;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.generator.qdox.model.BeanProperty;

public class BeanDefinition {
	
	private final Class<?> beanType;
	private Constructor ctor;
	private Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();

	public BeanDefinition(Class<?> type) {
		this.beanType = type;
	}

	public void setCtor(Constructor ctor) {
		this.ctor = ctor;
	}

	public Constructor getCtor() {
		return ctor;
	}

	public Class<?> getBeanType() {
    	return beanType;
    }

	public Collection<PropertyDefinition> getProperties() {
		return properties.values();
	}

	public PropertyDefinition getProperty(String name){
		return properties.get(name);
	}
	
	public Collection<String> getPropertyNames(){
		return properties.keySet();
	}
	
	public void setPropertyMap(Map<String, PropertyDefinition> properties) {
		this.properties = properties;
	}
	
	public void addProperty(PropertyDefinition p) {
		this.properties.put(p.getName(), p);
	}
}