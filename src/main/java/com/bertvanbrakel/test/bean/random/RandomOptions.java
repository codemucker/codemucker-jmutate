package com.bertvanbrakel.test.bean.random;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bertvanbrakel.test.bean.BeanOptions;

public class RandomOptions extends BeanOptions {

	private static final Collection<String> defaultExcludePackages = Arrays.asList("java.", "javax.", "sun.","oracle.", "ibm.");
	private final Collection<String> excludePackages = new ArrayList<String>();

	private final Map<Class<?>, RandomGenerator<?>> randomProviders = new HashMap<Class<?>, RandomGenerator<?>>();

	private boolean failOnNonSupportedPropertyType = false;
	private boolean failOnRecursiveBeanCreation = true;
	
	public RandomOptions excludePackage(String pkg) {
		excludePackages.add(pkg);
		return this;
	}
	
	public <T> RandomOptions addProvider(Class<T> type, RandomGenerator<T> provider) {
		randomProviders.put(type, provider);
		return this;
	}
	
	public <T> RandomGenerator<T> getProvider(Class<T> type){
		return (RandomGenerator<T>) randomProviders.get(type);
	}
	
	public boolean isGeneratePropertyType(Object bean, String propertyName, Class<?> type, Type genericType){
		String name = type.getName();
		if (isTypeInPackages(name, defaultExcludePackages)) {
			return false;
		}
		if (isTypeInPackages(name, excludePackages)) {
			return false;
		}

		return true;
	}

	private boolean isTypeInPackages(String fullyQualifiedTypeName, Iterable<String> packages) {
		for (String pkg : packages) {
			if (fullyQualifiedTypeName.startsWith(pkg)) {
				return true;
			}
		}
		return false;
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

	@Override
	public void setFailSilently() {
		super.setFailSilently();
		failOnNonSupportedPropertyType = false;
		failOnRecursiveBeanCreation = false;
	}

}
