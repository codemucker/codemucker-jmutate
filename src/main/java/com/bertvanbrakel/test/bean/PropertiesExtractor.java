package com.bertvanbrakel.test.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static com.bertvanbrakel.test.bean.ClassUtils.*;

public class PropertiesExtractor {

	protected Options options = new Options();
	private final Map<String, BeanDefinition> beanCache = new HashMap<String, BeanDefinition>();

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public Map<String, Property> extractAndCacheProperties(Class<?> beanClass) {
		BeanDefinition cache = getOrCreateCache(beanClass);
		if (cache.getPropertyMap() == null) {
			cache.setPropertyMap(extractProperties(beanClass));
		}
		return cache.getPropertyMap();
	}

	protected BeanDefinition getOrCreateCache(Class<?> beanClass) {
		BeanDefinition cache = beanCache.get(beanClass.getName());
		if (cache == null) {
			cache = new BeanDefinition(beanClass);
			beanCache.put(beanClass.getName(), cache);
		}
		return cache;
	}

	public Map<String, Property> extractProperties(Class<?> beanClass) {
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

	protected boolean isIncludeProperty(Class<?> beanClass, String propertyName, Class<?> propertyType) {
		if ("class".equals(propertyName)) {
			return false;
		}
		if (options.getIgnoreProperties().contains(propertyName)) {
			return false;
		}
		Collection<String> properties = options.getIgnorePropertiesOnClass().get(beanClass.getName());
		if (properties != null) {
			if (properties.contains(propertyName)) {
				return false;
			}
		}
		return true;
	}
}
