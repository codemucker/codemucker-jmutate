package com.bertvanbrakel.test.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BeanTester implements RandomDataProvider {
	
    private static Map<Class<?>, RandomDataProvider<?>> builtInProviders = new HashMap<Class<?>, RandomDataProvider<?>>();
    private final CollectionProvider collectionProvider = new CollectionProvider(this);
    private final Map<Class<?>, RandomDataProvider<?>> customProviders = new HashMap<Class<?>, RandomDataProvider<?>>();

    private static final Collection<String> defaultExcludePackages = Arrays.asList("java.","javax.","sun.","oracle.","ibm.");
    private final Collection<String> customExcludePackages = new ArrayList<String>();
    
    private EnumProvider enumProvider = new EnumProvider();
    
    private Stack<String> parentPropertes = new Stack<String>();
    
    //to prevent infinite recursion
    private Stack<Class<?>> parentBeansTypesCreated = new Stack<Class<?>>();

    private Options options = new Options();
    private String parentPropertyPath;
 
    private static final ExtendedRandom RANDOM = new ExtendedRandom();
    
    private final Map<String, BeanCache> beanCache = new HashMap<String, BeanCache>();
    
    private static class BeanCache {
	private final Class<?> beanType;
	private Constructor ctor;
	
	BeanCache(Class<?> type) {
	    this.beanType = type;
	}

	public void setCtor(Constructor ctor) {
            this.ctor = ctor;
        }

	public Constructor getCtor() {
            return ctor;
        }
	
	private Map<String, Property> properties;

	public Map<String, Property> getPropertyMap() {
	    return properties;
	}

	public Collection<Property> getProperties() {
	    return properties==null?Collections.EMPTY_LIST:properties.values();
	}

	public void setPropertyMap(Map<String, Property> properties) {
	    this.properties = properties;
	}
    }

    static {
	internalRegisterPrimitiveProvider(Boolean.class, Boolean.TYPE, new RandomDataProvider<Boolean>() {
	    public Boolean getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextBoolean();
	    }
	});
	internalRegisterPrimitiveProvider(Byte.class, Byte.TYPE, new RandomDataProvider<Byte>() {
	    public Byte getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextByte();
	    }
	});
	internalRegisterPrimitiveProvider(Character.class, Character.TYPE, new RandomDataProvider<Character>() {
	    public Character getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextChar();
	    }
	});
	internalRegisterPrimitiveProvider(Short.class, Short.TYPE, new RandomDataProvider<Short>() {
	    public Short getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextShort();
	    }
	});
	internalRegisterPrimitiveProvider(Integer.class, Integer.TYPE, new RandomDataProvider<Integer>() {
	    public Integer getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextInt();
	    }
	});
	internalRegisterPrimitiveProvider(Long.class, Long.TYPE, new RandomDataProvider<Long>() {
	    public Long getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextLong();
	    }
	});
	internalRegisterPrimitiveProvider(Float.class, Float.TYPE, new RandomDataProvider<Float>() {
	    public Float getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextFloat();
	    }
	});
	internalRegisterPrimitiveProvider(Double.class, Double.TYPE, new RandomDataProvider<Double>() {
	    public Double getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextDouble();
	    }
	});
	internalRegisterProvider(BigDecimal.class, new RandomDataProvider<BigDecimal>() {
	    public BigDecimal getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextBigDecimal();
	    }
	});
	internalRegisterProvider(BigInteger.class, new RandomDataProvider<BigInteger>() {
	    public BigInteger getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextBigInteger();
	    }
	});
	internalRegisterProvider(AtomicInteger.class, new RandomDataProvider<AtomicInteger>() {
	    public AtomicInteger getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return new AtomicInteger(RANDOM.nextInt());
	    }
	});
	internalRegisterProvider(AtomicLong.class, new RandomDataProvider<AtomicLong>() {
	    public AtomicLong getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return new AtomicLong(RANDOM.nextLong());
	    }
	});
	internalRegisterProvider(String.class, new RandomDataProvider<String>() {
	    public String getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return UUID.randomUUID().toString();
	    }
	});
    }

    private static <T> void internalRegisterProvider(Class<T> type, RandomDataProvider<T> provider) {
	builtInProviders.put(type, provider);
    }

    private static <T> void internalRegisterPrimitiveProvider(Class<T> type, Class<T> type2, RandomDataProvider<T> provider) {
	builtInProviders.put(type, provider);
	builtInProviders.put(type2, provider);
    }

    public <T> void registerProvider(Class<T> type, RandomDataProvider<T> provider) {
	customProviders.put(type, provider);
    }
    
    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public <T> T populate(Class<T> beanClass) {
	BeanCache cache = beanCache.get(beanClass.getName());
	if(cache == null){
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
	    cache = new BeanCache(beanClass);
	    cache.setCtor(ctor);
	    beanCache.put(beanClass.getName(), cache);
	}
	T bean = invokeCtor((Constructor<T>)cache.getCtor());
	populatePropertiesWithRandomData(bean);
	return bean;
    }

    public Map<String, Property> extractAndCacheProperties(Class<?> beanClass) {
	BeanCache cache = beanCache.get(beanClass.getName());
	if (cache == null) {
	    cache = new BeanCache(beanClass);
	    beanCache.put(beanClass.getName(), cache);
	}
	if (cache.getPropertyMap() == null) {
	    cache.setPropertyMap(extractProperties(beanClass));
	}
	return cache.getPropertyMap();
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
		    throw new BeanException("No setter named %s for property '%s' on class %s", 
			    setterName, p.getName(), beanClass.getName());
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

    private void populatePropertiesWithRandomData(Object bean) {
	Map<String, Property> properties = extractAndCacheProperties(bean.getClass());
	for (Property p : properties.values()) {
	    if (!p.isIgnore()) {
		if (isGenerateRandom(bean.getClass(), p.getName(), p.getType())) {
		    populatePropertyWithRandomData(p, bean);
		}
	    }
	}
    }
    
    private void populatePropertyWithRandomData(Property p, Object bean) {
	if (p.getWrite() != null) {
	    Method setter = p.getWrite();
	    Object propertyValue = getRandom(p.getName(), p.getType(), p.genericType);
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
    
    private boolean isGenerateRandom(Class<?> beanClass,String propertyName, Class<?> propertyType){
	return isIncludeProperty(beanClass, propertyName, propertyType);
    }
    
    private boolean isIncludeProperty(Class<?> beanClass, String propertyName, Class<?> propertyType) {
	if( "class".equals(propertyName)){
	    return false;
	}
	if (options.getIgnoreProperties().contains(propertyName)) {
	    return false;
	}
	if( parentPropertyPath != null ){
	    String fullPath = parentPropertyPath + propertyName;
	    if (options.getIgnoreProperties().contains(fullPath)) {
		return false;
	    }
	}
	Collection<String> properties = options.getIgnorePropertiesOnClass().get(beanClass.getName());
	if (properties != null ) {
	    if(properties.contains(propertyName)) {
		return false;
	    }
	}
	return true;
    }

    private boolean isReaderMethod(Method m) {
	return m.getParameterTypes().length == 0 && (m.getName().startsWith("get") || m.getName().startsWith("is"));
    }

    private boolean isWriterMethod(Method m) {
	return m.getParameterTypes().length == 1 && (m.getName().startsWith("set"));
    }

    private String extractPropertyName(Method m) {
	String name = m.getName();
	if (name.startsWith("get")) {
	    return lowerFirstChar(name.substring(3));
	} else if (name.startsWith("is")) {
	    return lowerFirstChar(name.substring(2));
	} else if (name.startsWith("set")) {
	    return lowerFirstChar(name.substring(3));
	}
	throw new BeanException("Don't know how to extract the proeprty name from method name " + name);
    }

    private String lowerFirstChar(String name) {
	if (name.length() > 1) {
	    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	} else {
	    return Character.toLowerCase(name.charAt(0)) + "";
	}
    }

    private String upperFirstChar(String name) {
	if (name.length() > 1) {
	    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	} else {
	    return Character.toUpperCase(name.charAt(0)) + "";
	}
    }
    
    private void pushBeanProperty(String propertyName,Class<?> propertyType){
	parentBeansTypesCreated.add(propertyType);
	parentPropertes.push(propertyName);
	parentPropertyPath = joinParentProperties();
    }

    
    private void popBeanProperty(){
	parentBeansTypesCreated.pop();
	parentPropertes.pop();
	parentPropertyPath = joinParentProperties();
    }
    
    private String joinParentProperties(){
	if( parentPropertes.size() > 0){
	    StringBuilder sb = new StringBuilder();
	    for(String name:parentPropertes){
		sb.append(name);
		sb.append('.');
	    }
	    return sb.toString();
	} else {
	    return "";
	}
    }

    private <T> T invokeCtor(Constructor<T> ctor) {
	int len = ctor.getParameterTypes().length;
	Object[] args = new Object[len];
	for (int i = 0; i < len; i++) {
	    args[i] = getRandom(null, ctor.getParameterTypes()[i], ctor.getGenericParameterTypes()[i]);
	}
	try {
	    T bean = ctor.newInstance(args);
	    return bean;
	} catch (IllegalArgumentException e) {
	    throw new BeanException("Error invoking ctor for type %s", ctor.getDeclaringClass().getName(), e);
	} catch (InstantiationException e) {
	    throw new BeanException("Error invoking ctor for type %s", ctor.getDeclaringClass().getName(), e);
	} catch (IllegalAccessException e) {
	    throw new BeanException("Error invoking ctor for type %s", ctor.getDeclaringClass().getName(), e);
	} catch (InvocationTargetException e) {
	    throw new BeanException("Error invoking ctor for type %s", ctor.getDeclaringClass().getName(), e);
	}
    }	

    private <T> Constructor<T> getLongestCtor(Class<T> beanClass) {
	Constructor<T> longest = null;
	Constructor<T>[] ctors = (Constructor<T>[]) beanClass.getDeclaredConstructors();
	for (Constructor<T> ctor : ctors) {
	    if (isCtorPublic(ctor)) {
		int len = ctor.getParameterTypes().length;
		if (len > 0 && (longest == null || longest.getParameterTypes().length < len)) {
		    longest = ctor;
		}
	    }
	}
	return longest;
    }

    private <T> Constructor<T> getNoArgCtor(Class<T> beanClass, boolean changeAccessibility) {
	try {
	    Constructor<T> ctor = beanClass.getDeclaredConstructor(null);
	    if (isCtorPublic(ctor)) {
		return ctor;
	    } else if( changeAccessibility ){
		ctor.setAccessible(true);
		return ctor;
	    }
	} catch (SecurityException e) {
	    // do nothing
	} catch (NoSuchMethodException e) {
	    // do nothing
	}
	
	return null;
    }

    private boolean isCtorPublic(Constructor<?> ctor) {
	return Modifier.isPublic(ctor.getModifiers());
    }

    @Override
    public Object getRandom(String propertyName, Class propertyType, Type genericType) {
	RandomDataProvider<?> provider = customProviders.get(propertyType);
	if (provider == null) {
	    provider = builtInProviders.get(propertyType);
	    if (provider == null) {
		if (propertyType.isArray() || Collection.class.isAssignableFrom(propertyType)) {
		    provider = collectionProvider;
		} else if( propertyType.isEnum()){
		    provider = enumProvider;
		}
	    }
	}
	if (provider == null) {
	    //lets create the bean
	    if( isGenerateBeanProperty(propertyType)){
		if( parentBeansTypesCreated.contains(propertyType)){
		    if( options.isFailOnRecursiveBeanCreation() ){
			throw new BeanException("Recursive bean creation for type %s for property %s", propertyType.getName(),propertyName);
		    } else {
			return null;
		    }
		}
		try{
		    pushBeanProperty(propertyName,propertyType);
		    return populate(propertyType);
		} finally {
		    popBeanProperty();
		}
	    } else {
		if( options.isFailOnNonSupportedPropertyType()){
		    throw new BeanException("no provider for type %s for property '%s'",propertyType,propertyName);	    
		} 
		return null;
	    }
	}
	return provider.getRandom(propertyName, propertyType, genericType);
    }
    
    private boolean isGenerateBeanProperty(Class<?> type){
	String name = type.getName();
	if (containsPackageMatching(name, defaultExcludePackages)) {
	    return false;
	}
	if (containsPackageMatching(name, customExcludePackages)) {
	    return false;
	}

	return true;
    }
    
    private boolean containsPackageMatching(String fullyQualifiedTypeName,Iterable<String> packages){
	for(String pkg:packages){
	    if( fullyQualifiedTypeName.startsWith(pkg)){
		return true;
	    }
	}
	return false;
    }
    
    static class ExtendedRandom extends Random {
	char nextChar() {
	    return (char) next(16);
	}

	byte nextByte() {
	    return (byte) next(8);
	}

	short nextShort() {
	    return (short) next(16);
	}

	BigDecimal nextBigDecimal() {
	    int scale = nextInt();
	    return new BigDecimal(nextBigInteger(), scale);
	}

	BigInteger nextBigInteger() {
	    int randomLen = 1 + nextInt(15);
	    byte[] bytes = new byte[randomLen];
	    nextBytes(bytes);
	    return new BigInteger(bytes);
	}
    }

}
