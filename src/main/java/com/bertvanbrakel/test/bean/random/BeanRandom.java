package com.bertvanbrakel.test.bean.random;

import static com.bertvanbrakel.test.bean.ClassUtils.invokeCtorWith;
import static com.bertvanbrakel.test.bean.ClassUtils.invokeMethod;
import static com.bertvanbrakel.test.bean.ClassUtils.setFieldValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.BeanException;
import com.bertvanbrakel.test.bean.CtorArgDefinition;
import com.bertvanbrakel.test.bean.PropertiesExtractor;
import com.bertvanbrakel.test.bean.PropertyDefinition;

public class BeanRandom implements RandomGenerator {

	private final PropertiesExtractor extractor;
	
	private static Map<Class<?>, RandomGenerator<?>> builtInProviders = new HashMap<Class<?>, RandomGenerator<?>>();

	private final PrimitiveGenerator primitiveProvider = new PrimitiveGenerator();
	private final CollectionGenerator collectionProvider = new CollectionGenerator(this);
	private final EnumGenerator enumProvider = new EnumGenerator();

	private Stack<String> parentPropertes = new Stack<String>();

	// to prevent infinite recursion
	private Stack<Class<?>> parentBeansTypesCreated = new Stack<Class<?>>();

	private String parentPropertyPath;
	
	private final RandomOptions options;

	public BeanRandom() {
		this(new RandomOptions());
	}

	public BeanRandom(RandomOptions options) {
		extractor = new PropertiesExtractor(options);
		this.options = options;
	}
	
	public <T> T populate(Class<T> beanClass) {
		BeanDefinition def = extractor.extractBeanDefWithCtor(beanClass);
		if (def.getCtor() == null) {
			throw new BeanException(
			        "Could not find a valid ctor for bean class %s. Are you sure your bean ctor is public (or if you have no ctor that your bean is public) and the bean is not a non static inner class?",
			        beanClass.getName());

		}
		T bean = populateCtor((Constructor<T>) def.getCtor());
		populateProperties(bean);
		return bean;
	}

	private void populateProperties(Object bean) {
		BeanDefinition def = extractor.extractBeanDef(bean.getClass());
		populateProperties(def, bean);
	}

	public void populateProperties(BeanDefinition def, Object bean) {
		for (PropertyDefinition p : def.getProperties()) {
			if (!p.isIgnore() && p.hasMutator() && isGenerateRandomPropertyValue(bean.getClass(), p.getName(), p.getType())) {
				populateProperty(bean, p);
			}
		}
	}
	
	public void populateProperty(Object bean, PropertyDefinition p) {
		Class<?> beanClass = bean.getClass();
		Object propertyValue = generateRandom(beanClass, p);
		setPropertyWithValue(bean, p, propertyValue);
	}

	public Object generateRandom(Class<?> beanClass, PropertyDefinition p) {
		Object propertyValue = generateRandom(beanClass, p.getName(), p.getType(), p.getGenericType());
		return propertyValue;
	}
	
	public void setPropertyWithValue(Object bean, PropertyDefinition p, Object propertyValue) {
		if (!p.hasMutator()) {
			throw new BeanException("Property '%s' has no mutators (no setters or field access)", p.getName());
		}
		// TODO:option to ignore errors?
		try {
			if (p.getWrite() != null) {
				invokeMethod(bean, p.getWrite(), new Object[] { propertyValue }, p.isMakeAccessible());
			} else if (p.getField() != null) {
				setFieldValue(bean, p.getField(), propertyValue);
			}
		} catch (BeanException e) {
			throw new BeanException("Error setting property '%s' on bean %s", e, p.getName(), bean.getClass().getName());
		}
	}

	private boolean isGenerateRandomPropertyValue(Class<?> beanClass, String propertyName, Class<?> propertyType) {
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

	public <T> T populateCtor(Constructor<T> ctor) {
		Object[] args = generateRandomArgsForCtor(ctor);
		T bean = invokeCtorWith(ctor, args);
		return bean;
	}
	
	public Object[] generateRandomArgsForCtor(Constructor<?> ctor) {
		Class<?>[] types = ctor.getParameterTypes();
		Type[] genericTypes = ctor.getGenericParameterTypes();

		int numArgs = types.length;
		Object[] args = new Object[numArgs];
		for (int i = 0; i < numArgs; i++) {
			args[i] = generateRandom(ctor.getDeclaringClass(), null, types[i], genericTypes[i]);
		}
		return args;
	}
	
	public Object generateRandomNotEqualsTo(Object orgVal, Class<?> beanClass, PropertyDefinition p) {
		return generateRandomNotEqualsTo(orgVal, beanClass, p.getName(), p.getType(), p.getGenericType());
	}
	
	public Object generateRandomNotEqualsTo(Object orgVal, Class<?> beanClass, CtorArgDefinition ctorArg) {
		return generateRandomNotEqualsTo(orgVal, beanClass, ctorArg.getName(), ctorArg.getType(), ctorArg.getGenericType());
	}
	
	private Object generateRandomNotEqualsTo(Object orgVal, Class<?> beanClass, String propertyName, Class<?> type, Type genericType) {
		final int maxNumAttempts = 10;
		for (int i = 0; i < maxNumAttempts; i++) {
			Object newVal = generateRandom(beanClass, propertyName, type, genericType);
			if (!newVal.equals(orgVal)) {
				return newVal;
			}
		}
		throw new BeanException(
		        "Exceeded max number of attempts (%d) to generate different random value of type '%s', for value '%s'",
		        maxNumAttempts, type.getName(), genericType, orgVal);
	}
	@Override
	public Object generateRandom(Class beanClass, String propertyName, Class propertyType, Type genericType) {
		RandomGenerator<?> provider = options.getProvider(propertyType);
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
		return provider.generateRandom(beanClass, propertyName, propertyType, genericType);
	}

	private boolean isGenerateBeanPropertyOfType(Object bean, String propertyName, Class<?> type, Type genericType) {
		return options.isGeneratePropertyType(bean, propertyName, type, genericType);
	}

	public RandomOptions getOptions() {
		return options;
	}
}
