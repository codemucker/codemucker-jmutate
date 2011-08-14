package com.bertvanbrakel.test.bean.random;

import static com.bertvanbrakel.test.bean.ClassUtils.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.BeanException;
import com.bertvanbrakel.test.bean.PropertiesExtractor;
import com.bertvanbrakel.test.bean.PropertyDefinition;

public class BeanRandom implements RandomGenerator {

	private final PropertiesExtractor extractor = new PropertiesExtractor();

	private static Map<Class<?>, RandomGenerator<?>> builtInProviders = new HashMap<Class<?>, RandomGenerator<?>>();

	private final PrimitiveGenerator primitiveProvider = new PrimitiveGenerator();
	private final CollectionGenerator collectionProvider = new CollectionGenerator(this);
	private final EnumGenerator enumProvider = new EnumGenerator();

	private Stack<String> parentPropertes = new Stack<String>();

	// to prevent infinite recursion
	private Stack<Class<?>> parentBeansTypesCreated = new Stack<Class<?>>();

	private String parentPropertyPath;

	public BeanRandom() {
		extractor.setOptions(new RandomOptions());
	}

	public <T> T populate(Class<T> beanClass) {
		BeanDefinition def = extractor.extractBeanDefWithCtor(beanClass);
		if (def.getCtor() == null) {
			throw new BeanException(
			        "Could not find a valid ctor for bean class %s. Are you sure your bean ctor is public (or if you have no ctor that your bean is public) and the bean is not a non static inner class?",
			        beanClass.getName());

		}
		T bean = invokeCtorWithRandomArgs((Constructor<T>) def.getCtor());
		populatePropertiesWithRandomValues(bean);
		return bean;
	}

	private void populatePropertiesWithRandomValues(Object bean) {
		BeanDefinition def = extractor.extractBeanDef(bean.getClass());
		for (PropertyDefinition p : def.getProperties()) {
			if (!p.isIgnore()) {
				if (isGenerateRandomPropertyValue(bean.getClass(), p.getName(), p.getType())) {
					populatePropertyWithRandomValue(p, bean);
				}
			}
		}
	}

	private void populatePropertyWithRandomValue(PropertyDefinition p, Object bean) {
		if (p.getWrite() != null) {
			Object propertyValue = generateRandom(null, p.getName(), p.getType(), p.getGenericType());
			// TODO:option to ignore errors?
			try {
				invokeMethod(bean, p.getWrite(), new Object[] { propertyValue }, p.isMakeAccessible());
			} catch (BeanException e) {
				throw new BeanException("Error setting property '%s' on bean %s", e, p.getName(), bean.getClass()
				        .getName());
			}
		}
	}

	private boolean isGenerateRandomPropertyValue(Class<?> beanClass, String propertyName, Class<?> propertyType) {
		if (parentPropertyPath != null) {
			String fullPath = parentPropertyPath + propertyName;
			if (getOptions().getIgnoreProperties().contains(fullPath)) {
				return false;
			}
		}

		return true;
	}

	private void pushBeanProperty(String propertyName, Class<?> propertyType) {
		parentBeansTypesCreated.add(propertyType);
		parentPropertes.push(propertyName);
		parentPropertyPath = joinParentProperties();
	}

	private void popBeanProperty() {
		parentBeansTypesCreated.pop();
		parentPropertes.pop();
		parentPropertyPath = joinParentProperties();
	}

	private String joinParentProperties() {
		if (parentPropertes.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String name : parentPropertes) {
				sb.append(name);
				sb.append('.');
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	protected <T> T invokeCtorWithRandomArgs(Constructor<T> ctor) {
		int len = ctor.getParameterTypes().length;
		Object[] args = new Object[len];
		for (int i = 0; i < len; i++) {
			args[i] = generateRandom(ctor.getDeclaringClass(), null, ctor.getParameterTypes()[i], ctor.getGenericParameterTypes()[i]);
		}
		T bean = invokeCtorWith(ctor, args);
		return bean;
	}
	
	@Override
	public Object generateRandom(Class beanClass, String propertyName, Class propertyType, Type genericType) {
		RandomGenerator<?> provider = getOptions().getProvider(propertyType);
		if (provider == null) {
			provider = builtInProviders.get(propertyType);
			if (provider == null) {
				if (propertyType.isArray() || Collection.class.isAssignableFrom(propertyType)) {
					provider = collectionProvider;
				} else if (propertyType.isEnum()) {
					provider = enumProvider;
				} else if (primitiveProvider.supportsType(propertyType)) {
					provider = primitiveProvider;
				}
			}
		}
		if (provider == null) {
			// lets create the bean
			if (isGenerateBeanPropertyOfType(beanClass, propertyName, propertyType, genericType)) {
				if (parentBeansTypesCreated.contains(propertyType)) {
					if (getOptions().isFailOnRecursiveBeanCreation()) {
						throw new BeanException("Recursive bean creation for type %s for property %s",
						        propertyType.getName(), propertyName);
					} else {
						return null;
					}
				}
				try {
					pushBeanProperty(propertyName, propertyType);
					return populate(propertyType);
				} finally {
					popBeanProperty();
				}
			} else {
				if (getOptions().isFailOnNonSupportedPropertyType()) {
					throw new BeanException("no provider for type %s for property '%s'", propertyType, propertyName);
				}
				return null;
			}
		}
		return provider.generateRandom(beanClass, propertyName, propertyType, genericType);
	}

	private boolean isGenerateBeanPropertyOfType(Object bean, String propertyName, Class<?> type, Type genericType) {
		return getOptions().isGeneratePropertyType(bean, propertyName, type, genericType);
	}

	public RandomOptions getOptions() {
		return (RandomOptions) extractor.getOptions();
	}

	public void setOptions(RandomOptions options) {
		extractor.setOptions(options);
	}
}
