package com.bertvanbrakel.test.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BeanOptions {
    private boolean failOnInvalidGetters = false;
    private boolean failOnMissingSetters = false;
    private boolean failOnAdditionalSetters = false;
    private boolean failOnNonSupportedPropertyType = false;
    private boolean failOnRecursiveBeanCreation = true;
    
    private Collection<String> ignoreProperties = new HashSet<String>();
    private Map<String, Collection<String>> ignorePropertiesOnClass = new HashMap<String, Collection<String>>();

    public void setFailSilently() {
	failOnAdditionalSetters = false;
	failOnMissingSetters = false;
	failOnInvalidGetters = false;
	failOnNonSupportedPropertyType = false;
	failOnRecursiveBeanCreation = false;
    }

    public boolean isFailOnRecursiveBeanCreation() {
	return failOnRecursiveBeanCreation;
    }

    public BeanOptions failOnRecursiveBeanCreation(boolean failOnRecursiveBeanCreation) {
	this.failOnRecursiveBeanCreation = failOnRecursiveBeanCreation;
	return this;
    }

    public boolean isFailOnNonSupportedPropertyType() {
	return failOnNonSupportedPropertyType;
    }

    public BeanOptions failOnNonSupportedPropertyType(boolean failOnNonSupportedPropertyType) {
	this.failOnNonSupportedPropertyType = failOnNonSupportedPropertyType;
	return this;
    }

    public boolean isFailOnInvalidGetters() {
	return failOnInvalidGetters;
    }

    public BeanOptions failOnInvalidGetters(boolean failOnInvalidGetters) {
	this.failOnInvalidGetters = failOnInvalidGetters;
	return this;
    }

    public boolean isFailOnMissingSetters() {
	return failOnMissingSetters;
    }

    public BeanOptions failOnMissingSetters(boolean failOnMissingSetters) {
	this.failOnMissingSetters = failOnMissingSetters;
	return this;
    }

    public boolean isFailOnAdditionalSetters() {
	return failOnAdditionalSetters;
    }

    public BeanOptions failOnAdditionalSetters(boolean failOnAdditionalSetters) {
	this.failOnAdditionalSetters = failOnAdditionalSetters;
	return this;
    }

    public BeanOptions ignoreProperty(String propertyPath) {
	ignoreProperties.add(propertyPath);
	return this;
    }

    public Collection<String> getIgnoreProperties() {
	return ignoreProperties;
    }

    public BeanOptions ignoreProperty(Class<?> beanClass, String propertyName) {
	String key = beanClass.getName();
	Collection<String> propertiesToIgnore = ignorePropertiesOnClass.get(key);
	if (propertiesToIgnore == null) {
	    propertiesToIgnore = new HashSet<String>();
	    ignorePropertiesOnClass.put(key, propertiesToIgnore);
	}
	propertiesToIgnore.add(propertyName);
	return this;
    }

    public Map<String, Collection<String>> getIgnorePropertiesOnClass() {
	return ignorePropertiesOnClass;
    }

}