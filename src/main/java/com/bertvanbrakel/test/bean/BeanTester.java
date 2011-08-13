package com.bertvanbrakel.test.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.bertvanbrakel.test.bean.ClassUtils.*;

public class BeanTester extends PropertiesExtractor implements RandomDataProvider {

	private static Map<Class<?>, RandomDataProvider<?>> builtInProviders = new HashMap<Class<?>, RandomDataProvider<?>>();
	private final CollectionProvider collectionProvider = new CollectionProvider(this);
	private final Map<Class<?>, RandomDataProvider<?>> customProviders = new HashMap<Class<?>, RandomDataProvider<?>>();

	private static final Collection<String> defaultExcludePackages = Arrays.asList("java.", "javax.", "sun.",
	        "oracle.", "ibm.");
	private final Collection<String> customExcludePackages = new ArrayList<String>();

	private PrimitiveRandomProvider primitiveProvider = new PrimitiveRandomProvider();
	
	private EnumProvider enumProvider = new EnumProvider();

	private Stack<String> parentPropertes = new Stack<String>();

	// to prevent infinite recursion
	private Stack<Class<?>> parentBeansTypesCreated = new Stack<Class<?>>();

	private String parentPropertyPath;

	public <T> void registerProvider(Class<T> type, RandomDataProvider<T> provider) {
		customProviders.put(type, provider);
	}

	public <T> T populate(Class<T> beanClass) {
		BeanDefinition cache = getOrCreateCache(beanClass);
		if (cache.getCtor() == null) {
			Constructor<T> ctor = getNoArgCtor(beanClass, false);
			if (ctor == null) {
				ctor = getLongestCtor(beanClass);
			}
			if (ctor == null) {
				ctor = getNoArgCtor(beanClass, true);
			}
			if (ctor == null) {
				throw new BeanException(
				        "Could not find a valid ctor for bean class %s. Are you sure your bean ctor is public (or if you have no ctor that your bean is public) and the bean is not a non static inner class?",
				        beanClass.getName());
			}
			cache.setCtor(ctor);
		}
		T bean = invokeCtorWithRandomArgs((Constructor<T>) cache.getCtor());
		populatePropertiesWithRandomValues(bean);
		return bean;
	}

	private void populatePropertiesWithRandomValues(Object bean) {
		Map<String, Property> properties = extractAndCacheProperties(bean.getClass());
		for (Property p : properties.values()) {
			if (!p.isIgnore()) {
				if (isGenerateRandomPropertyValue(bean.getClass(), p.getName(), p.getType())) {
					populatePropertyWithRandomValue(p, bean);
				}
			}
		}
	}

	private void populatePropertyWithRandomValue(Property p, Object bean) {
		if (p.getWrite() != null) {
			Method setter = p.getWrite();
			Object propertyValue = getRandom(p.getName(), p.getType(), p.getGenericType());
			// TODO:option to ignore errors?
			try {
				setter.invoke(bean, new Object[] { propertyValue });
			} catch (IllegalArgumentException e) {
				throw new BeanException("Error invoking setter %s on property '%s' on class %s",
				        setter.toGenericString(), p.getName(), bean.getClass().getName());
			} catch (IllegalAccessException e) {
				throw new BeanException("Error invoking setter %s on property '%s' on class %s",
				        setter.toGenericString(), p.getName(), bean.getClass().getName());
			} catch (InvocationTargetException e) {
				throw new BeanException("Error invoking setter %s on property '%s' on class %s",
				        setter.toGenericString(), p.getName(), bean.getClass().getName());
			}
		}
	}

	private boolean isGenerateRandomPropertyValue(Class<?> beanClass, String propertyName, Class<?> propertyType) {
		boolean include = isIncludeProperty(beanClass, propertyName, propertyType);
		if (!include) {
			return false;
		}
		if (parentPropertyPath != null) {
			String fullPath = parentPropertyPath + propertyName;
			if (options.getIgnoreProperties().contains(fullPath)) {
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

	@Override
	public Object getRandom(String propertyName, Class propertyType, Type genericType) {
		RandomDataProvider<?> provider = customProviders.get(propertyType);
		if (provider == null) {
			provider = builtInProviders.get(propertyType);
			if (provider == null) {
				if (propertyType.isArray() || Collection.class.isAssignableFrom(propertyType)) {
					provider = collectionProvider;
				} else if (propertyType.isEnum()) {
					provider = enumProvider;
				} else if (primitiveProvider.supportsType(propertyType)){
					provider = primitiveProvider;
				}
			}
		}
		if (provider == null) {
			// lets create the bean
			if (isGenerateBeanPropertyOfType(propertyType)) {
				if (parentBeansTypesCreated.contains(propertyType)) {
					if (options.isFailOnRecursiveBeanCreation()) {
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
				if (options.isFailOnNonSupportedPropertyType()) {
					throw new BeanException("no provider for type %s for property '%s'", propertyType, propertyName);
				}
				return null;
			}
		}
		return provider.getRandom(propertyName, propertyType, genericType);
	}

	private boolean isGenerateBeanPropertyOfType(Class<?> type) {
		String name = type.getName();
		if (isTypeInPackages(name, defaultExcludePackages)) {
			return false;
		}
		if (isTypeInPackages(name, customExcludePackages)) {
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

	protected <T> T invokeCtorWithRandomArgs(Constructor<T> ctor) {
		int len = ctor.getParameterTypes().length;
		Object[] args = new Object[len];
		for (int i = 0; i < len; i++) {
			args[i] = getRandom(null, ctor.getParameterTypes()[i], ctor.getGenericParameterTypes()[i]);
		}
		T bean = invokeCtorWith(ctor, args);
		return bean;
	}

}
