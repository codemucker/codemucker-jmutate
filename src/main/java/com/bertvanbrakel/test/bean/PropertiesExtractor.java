package com.bertvanbrakel.test.bean;

import static com.bertvanbrakel.test.bean.ClassUtils.extractPropertyName;
import static com.bertvanbrakel.test.bean.ClassUtils.getLongestCtor;
import static com.bertvanbrakel.test.bean.ClassUtils.getNoArgCtor;
import static com.bertvanbrakel.test.bean.ClassUtils.isReaderMethod;
import static com.bertvanbrakel.test.bean.ClassUtils.isWriterMethod;
import static com.bertvanbrakel.test.bean.ClassUtils.upperFirstChar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PropertiesExtractor {

	private final Map<String, BeanDefinition> beanDefsByType = new HashMap<String, BeanDefinition>();

	protected BeanOptions options = new BeanOptions();
	
	public BeanOptions getOptions() {
		return options;
	}

	public void setOptions(BeanOptions options) {
		this.options = options;
	}

	public BeanDefinition extractBeanDefWithCtor(Class<?> beanClass){
		BeanDefinition def = extractBeanDef(beanClass);
		if (def.getCtor() == null) {
			Constructor<?> ctor = getNoArgCtor(beanClass, false);
			if (ctor == null) {
				ctor = getLongestCtor(beanClass);
			}
			if (ctor == null) {
				ctor = getNoArgCtor(beanClass, true);
			}
			def.setCtor(ctor);
		}
		return def;
	}
	
	public BeanDefinition extractBeanDef(Class<?> beanClass) {
		BeanDefinition def = getOrCreateBeanDef(beanClass);
		if (def.getPropertyMap() == null) {
			def.setPropertyMap(extractProperties(beanClass));
		}
		return def;
	}

	private BeanDefinition getOrCreateBeanDef(Class<?> beanClass) {
		BeanDefinition cache = beanDefsByType.get(beanClass.getName());
		if (cache == null) {
			cache = new BeanDefinition(beanClass);
			beanDefsByType.put(beanClass.getName(), cache);
		}
		return cache;
	}

	private Map<String, Property> extractProperties(Class<?> beanClass) {
		Map<String, Property> properties = new HashMap<String, Property>();
		extractGetters(beanClass, properties);
		extractSettersFromGetters(beanClass, properties);
		extractAdditionalSetters(beanClass, properties);
		return properties;
	}

	private void extractGetters(Class<?> beanClass, Map<String, Property> properties) {
		Method[] methods = beanClass.getMethods();
		for (Method m : methods) {
			if (isReaderMethod(m)) {
				String propertyName = extractPropertyName(m);
				Class<?> propertyType = m.getReturnType();
				boolean isInclude = isIncludeProperty(beanClass, propertyName, propertyType);
				if (Void.class.equals(propertyType)) {
					if (isInclude && options.isFailOnInvalidGetters()) {
						throw new BeanException("Getter method %s returns void instead of a value for class %s",
						        m.toGenericString(), beanClass.getName());
					}
				} else {
					Property p = new Property();
					p.setName(propertyName);
					p.setRead(m);
					p.setType(m.getReturnType());
					p.setGenericType(m.getGenericReturnType());
					p.setIgnore(!isInclude);
					properties.put(p.getName(), p);
				}
			}
		}
	}

	private void extractSettersFromGetters(Class<?> beanClass, Map<String, Property> properties) {
		// find corresponding setters
		for (Property p : properties.values()) {
			if (p.getRead() != null) {
				String setterName = "set" + upperFirstChar(p.getName());
				Method setter = null;
				try {
					setter = beanClass.getMethod(setterName, p.getType());
				} catch (SecurityException e) {
					// ignore
				} catch (NoSuchMethodException e) {
					// ignore
				}
				if (setter != null) {
					p.setWrite(setter);
					// TODO:check generic type?
				} else if (options.isFailOnMissingSetters()) {
					throw new BeanException("No setter named %s for property '%s' on class %s", setterName,
					        p.getName(), beanClass.getName());
				}
			}
		}
	}

	private void extractAdditionalSetters(Class<?> beanClass, Map<String, Property> properties) {
		Method[] methods = beanClass.getMethods();
		for (Method m : methods) {
			if (isWriterMethod(m)) {
				extractAdditionalSetterMethod(beanClass, properties, m);
			}
		}
	}

	private void extractAdditionalSetterMethod(Class<?> beanClass, Map<String, Property> properties, Method m) {
		String propertyName = extractPropertyName(m);
		Property p = properties.get(propertyName);
		Class<?> propertyType = p != null ? p.getType() : m.getParameterTypes()[0];
		boolean isInclude = isIncludeProperty(beanClass, propertyName, propertyType);
		if (p == null) {
			if (isInclude && options.isFailOnAdditionalSetters()) {
				throw new BeanException(
				        "Found additional setter %s with no corresponding getter for property '%s' on class %s",
				        m.toGenericString(), propertyName, beanClass.getName());
			}
			p = new Property();
			p.setName(propertyName);
			p.setWrite(m);
			p.setType(propertyType);
			p.setGenericType(m.getGenericParameterTypes()[0]);
			p.setIgnore(!isInclude);
			properties.put(p.getName(), p);
		} else {
			if (p.getWrite() == null) {
				p.setWrite(m);
			} else if (p.getWrite() != m) {
				if (isInclude && options.isFailOnAdditionalSetters()) {
					throw new BeanException(
					        "Found additional setter %s for property '%s' on class %s, an existing setter %s already exsist",
					        m.toGenericString(), propertyName, beanClass.getName(), p.getWrite().toGenericString());
				}
			}
		}
	}

	private boolean isIncludeProperty(Class<?> beanClass, String propertyName, Class<?> propertyType) {
		if ("class".equals(propertyName)) {
			return false;
		}
		return options.isIncludeProperty(beanClass, propertyName, propertyType);
	}
}
