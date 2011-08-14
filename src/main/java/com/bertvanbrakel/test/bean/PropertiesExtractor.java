package com.bertvanbrakel.test.bean;

import static com.bertvanbrakel.test.bean.ClassUtils.extractPropertyName;
import static com.bertvanbrakel.test.bean.ClassUtils.getLongestCtor;
import static com.bertvanbrakel.test.bean.ClassUtils.getNoArgCtor;
import static com.bertvanbrakel.test.bean.ClassUtils.isReaderMethod;
import static com.bertvanbrakel.test.bean.ClassUtils.isStatic;
import static com.bertvanbrakel.test.bean.ClassUtils.isWriterMethod;
import static com.bertvanbrakel.test.bean.ClassUtils.upperFirstChar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
			Constructor<?> ctor = findCtorFor(beanClass);
			def.setCtor(ctor);
		}
		return def;
	}
	
	private Constructor<?> findCtorFor(Class<?> beanClass) {
		Constructor<?> ctor = getNoArgCtor(beanClass, false);
		if (ctor == null) {
			ctor = getLongestCtor(beanClass);
		}
		if (ctor == null) {
			ctor = getNoArgCtor(beanClass, true);
		}
		return ctor;
	}

	public BeanDefinition extractBeanDef(Class<?> beanClass) {
		BeanDefinition def = beanDefsByType.get(beanClass.getName());
		if (def == null) {
			def = new BeanDefinition(beanClass);
			beanDefsByType.put(beanClass.getName(), def);
			extractProperties(def);
		}
		return def;
	}
	
	private void extractProperties(BeanDefinition def) {
		extractMethodGetters(def.getBeanType(), def);
		extractMethodSettersFromGetters(def.getBeanType(), def);
		extractMethodSetters(def.getBeanType(), def);
		if (getOptions().isExtractFields()) {
			extractFields(def.getBeanType(), def);
		}
	}

	private void extractMethodGetters(Class<?> beanClass, BeanDefinition def) {
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
					PropertyDefinition p = new PropertyDefinition();
					p.setName(propertyName);
					p.setRead(m);
					p.setType(m.getReturnType());
					p.setGenericType(m.getGenericReturnType());
					p.setIgnore(!isInclude);
					p.setMakeAccessible(getOptions().isMakeAccessible());
					
					def.addProperty(p);
				}
			}
		}
	}

	private void extractMethodSettersFromGetters(Class<?> beanClass, BeanDefinition def) {
		// find corresponding setters
		for (PropertyDefinition p : def.getProperties()) {
			if (p.getRead() != null) {
				String setterName = "set" + upperFirstChar(p.getName());
				Method setter = ClassUtils.getMethod(beanClass, setterName, p.getType());
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

	private void extractMethodSetters(Class<?> beanClass, BeanDefinition def) {
		Method[] methods = beanClass.getMethods();
		for (Method m : methods) {
			if (isWriterMethod(m)) {
				extractAdditionalSetterMethod(beanClass, def, m);
			}
		}
	}

	private void extractAdditionalSetterMethod(Class<?> beanClass, BeanDefinition def, Method m) {
		String propertyName = extractPropertyName(m);
		PropertyDefinition p = def.getProperty(propertyName);
		Class<?> propertyType = p != null ? p.getType() : m.getParameterTypes()[0];
		boolean isInclude = isIncludeProperty(beanClass, propertyName, propertyType);
		if (p == null) {
			if (isInclude && options.isFailOnAdditionalSetters()) {
				throw new BeanException(
				        "Found additional setter %s with no corresponding getter for property '%s' on class %s",
				        m.toGenericString(), propertyName, beanClass.getName());
			}
			p = new PropertyDefinition();
			p.setName(propertyName);
			p.setWrite(m);
			p.setType(propertyType);
			p.setGenericType(m.getGenericParameterTypes()[0]);
			p.setIgnore(!isInclude);
			p.setMakeAccessible(getOptions().isMakeAccessible());
			
			
			def.addProperty(p);
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
	
	private void extractFields(Class<?> beanClass, BeanDefinition def) {
		Field[] fields = beanClass.getDeclaredFields();
		for (Field f : fields) {
			if (isIncludeField(f)) {
				String propertyName = extractPropertyName(f);
				Class<?> propertyType = f.getType();
				PropertyDefinition p = def.getProperty(propertyName);
				if( p == null){
					boolean isInclude = isIncludeProperty(beanClass, propertyName, propertyType);
					
					p = new PropertyDefinition();
					p.setName(propertyName);
					p.setIgnore(!isInclude);
					p.setType(f.getType());
					p.setGenericType(f.getGenericType());
					p.setMakeAccessible(getOptions().isMakeAccessible());
					
					def.addProperty(p);
				} else {
					if (p.getType() != f.getType()) {
						if (getOptions().isFailOnMisMatchingFields()) {
							throw new BeanException(
							        "Error extracting field '%s', existing property '%s' exists but types do not match. Expected %s but was %s",
							        f.getName(), propertyName, p.getType().getName(), f.getType().getName());
						}
					}
					if (p.getGenericType() != f.getGenericType()) {
						if (getOptions().isFailOnMisMatchingFields()) {
							throw new BeanException(
							        "Error extracting field '%s', existing property '%s' exists but generic types do not match. Expected %s but was %s",
							        f.getName(), propertyName, p.getGenericType(), f.getGenericType());
						}
					}

				}
				p.setField(f);
			
			}
		}
	}

	private boolean isIncludeField(Field f){
		return !f.isSynthetic() && !isStatic(f);
	}
	
	private boolean isIncludeProperty(Class<?> beanClass, String propertyName, Class<?> propertyType) {
		if ("class".equals(propertyName)) {
			return false;
		}
		return options.isIncludeProperty(beanClass, propertyName, propertyType);
	}
}
