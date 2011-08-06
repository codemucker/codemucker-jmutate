package com.bertvanbrakel.test.bean;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
    
    //to prevent infinite recursion
    private Stack<Class<?>> parentBeansTypesCreated = new Stack<Class<?>>();
    
    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }


    public static class Options {
	private boolean failOnInvalidGetters = false;
	private boolean failOnMissingSetters = false;
	private boolean failOnAdditionalSetters = false;
	private boolean failOnNonSupportedPropertyType = false;
	private boolean failOnRecursiveBeanCreation = true;
	
	public boolean isFailOnRecursiveBeanCreation() {
            return failOnRecursiveBeanCreation;
        }
	public void setFailOnRecursiveBeanCreation(boolean failOnRecursiveBeanCreation) {
            this.failOnRecursiveBeanCreation = failOnRecursiveBeanCreation;
        }
	public boolean isFailOnNonSupportedPropertyType() {
            return failOnNonSupportedPropertyType;
        }
	public void setFailOnNonSupportedPropertyType(boolean failOnNonSupportedPropertyType) {
            this.failOnNonSupportedPropertyType = failOnNonSupportedPropertyType;
        }
	public boolean isFailOnInvalidGetters() {
            return failOnInvalidGetters;
        }
	public void setFailOnInvalidGetters(boolean failOnInvalidGetters) {
            this.failOnInvalidGetters = failOnInvalidGetters;
        }
	public boolean isFailOnMissingSetters() {
            return failOnMissingSetters;
        }
	public void setFailOnMissingSetters(boolean failOnMissingSetters) {
            this.failOnMissingSetters = failOnMissingSetters;
        }
	public boolean isFailOnAdditionalSetters() {
            return failOnAdditionalSetters;
        }
	public void setFailOnAdditionalSetters(boolean failOnAdditionalSetters) {
            this.failOnAdditionalSetters = failOnAdditionalSetters;
        }

    }
    
    private Options options = new Options();
    
    private static ExtendedRandom RANDOM = new ExtendedRandom();

    static {
	registerPrimitiveProvider(Boolean.class, Boolean.TYPE, new RandomDataProvider<Boolean>() {
	    public Boolean getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextBoolean();
	    }
	});
	registerPrimitiveProvider(Byte.class, Byte.TYPE, new RandomDataProvider<Byte>() {
	    public Byte getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextByte();
	    }
	});
	registerPrimitiveProvider(Character.class, Character.TYPE, new RandomDataProvider<Character>() {
	    public Character getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextChar();
	    }
	});
	registerPrimitiveProvider(Short.class, Short.TYPE, new RandomDataProvider<Short>() {
	    public Short getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextShort();
	    }
	});
	registerPrimitiveProvider(Integer.class, Integer.TYPE, new RandomDataProvider<Integer>() {
	    public Integer getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextInt();
	    }
	});
	registerPrimitiveProvider(Long.class, Long.TYPE, new RandomDataProvider<Long>() {
	    public Long getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextLong();
	    }
	});
	registerPrimitiveProvider(Float.class, Float.TYPE, new RandomDataProvider<Float>() {
	    public Float getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextFloat();
	    }
	});
	registerPrimitiveProvider(Double.class, Double.TYPE, new RandomDataProvider<Double>() {
	    public Double getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextDouble();
	    }
	});
	registerProvider(BigDecimal.class, new RandomDataProvider<BigDecimal>() {
	    public BigDecimal getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextBigDecimal();
	    }
	});
	registerProvider(BigInteger.class, new RandomDataProvider<BigInteger>() {
	    public BigInteger getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return RANDOM.nextBigInteger();
	    }
	});
	registerProvider(AtomicInteger.class, new RandomDataProvider<AtomicInteger>() {
	    public AtomicInteger getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return new AtomicInteger(RANDOM.nextInt());
	    }
	});
	registerProvider(AtomicLong.class, new RandomDataProvider<AtomicLong>() {
	    public AtomicLong getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return new AtomicLong(RANDOM.nextLong());
	    }
	});
	registerProvider(String.class, new RandomDataProvider<String>() {
	    public String getRandom(String propertyName, Class<?> propertyType, Type genericType) {
		return UUID.randomUUID().toString();
	    }
	});
    }

    private static class EnumProvider implements RandomDataProvider {

	@Override
        public Object getRandom(String propertyName, Class propertyType, Type genericType) {
	    if( propertyType.isEnum()){
		Object[] enums = propertyType.getEnumConstants();
		int idx = RANDOM.nextInt(enums.length);
		return enums[idx];
	    } 
	    
	    throw new BeanException("Property '%s' of type %s is not an enum", propertyName,propertyType.getName());
        }
	
    }
    private static class CollectionProvider implements RandomDataProvider {

	private final RandomDataProvider provider;

	CollectionProvider(RandomDataProvider<?> provider) {
	    this.provider = provider;
	}

	@Override
	public Object getRandom(String propertyName, Class propertyType, Type genericType) {
	    if (propertyType.isArray()) {
		int randomLen = randomLen();
		Object[] arr = (Object[]) Array.newInstance(propertyType.getComponentType(), randomLen);
		return fillArray(propertyName, arr, propertyType.getComponentType());
	    } else if (Collection.class.equals(propertyType)) {
		return fillCollection(propertyName, new ArrayList(), genericType);
	    } else if (List.class.equals(propertyType)) {
		return fillCollection(propertyName, new ArrayList(), genericType);
	    } else if (Set.class.equals(propertyType)) {
		return fillCollection(propertyName, new HashSet(), genericType);
	    } else if (Collection.class.isAssignableFrom(propertyType)) {
		try {
		    Collection col = (Collection) propertyType.newInstance();
		    return fillCollection(propertyName, col, genericType);
		} catch (InstantiationException e) {
		    throw new BeanException("Don't know how to create collection of type " + propertyType.getName() + ", for property '" + propertyName + "'", e);
		} catch (IllegalAccessException e) {
		    throw new BeanException("Don't know how to create collection of type " + propertyType.getName() + ", for property '" + propertyName + "'", e);
		}
	    } else {
		throw new BeanException("Don't know how to create collection of type " + propertyType.getName() + ", for property '" + propertyName + "'");
	    }
	}

	public <T extends Collection> T fillCollection(String propertyName, T col, Type genericType) {
	    Class<?> elementType = extractConcreteType(genericType);
	    if (elementType == null) {
		throw new BeanException("Can't create collection elements using non concrete type:" + genericType);
	    }
	    int randomLen = randomLen();
	    for (int i = 0; i < randomLen; i++) {
		Object eleVal = provider.getRandom(propertyName, elementType, null);
		col.add(eleVal);
	    }
	    return col;
	}

	public Object[] fillArray(String propertyName, Object[] arr, Class<?> elementType) {
	    for (int i = 0; i < arr.length; i++) {
		Object eleVal = provider.getRandom(propertyName, elementType, null);
		arr[i] = eleVal;
	    }
	    return arr;
	}

	Class<?> extractConcreteType(Type type) {
	    if (type instanceof ParameterizedType) {
		ParameterizedType pType = (ParameterizedType) type;
		if (pType.getActualTypeArguments().length == 0) {
		    Type subType = pType.getActualTypeArguments()[0];
		    if (subType instanceof Class) {
			return (Class) subType;
		    }

		}
	    }
	    return null;
	}

	private int randomLen() {
	    return 1 + RANDOM.nextInt(10);
	}
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

    private static <T> void registerProvider(Class<T> type, RandomDataProvider<T> provider) {
	builtInProviders.put(type, provider);
    }

    private static <T> void registerPrimitiveProvider(Class<T> type, Class<T> type2, RandomDataProvider<T> provider) {
	builtInProviders.put(type, provider);
	builtInProviders.put(type2, provider);

    }

    public <T> T populate(Class<T> beanClass) {
	Constructor<T> ctor = getNoArgCtor(beanClass, false);

	if (ctor == null) {
	    ctor = getLongestCtor(beanClass);
	}
	if( ctor == null ){
	    ctor = getNoArgCtor(beanClass, true);
	}
	if (ctor == null) {
	    
	    throw new BeanException(
		    "Could not find a valid ctor for bean class %s. Are you sure your bean ctor is public (or if you have no ctor that your bean is public) and the bean is not a non static inner class?",
		    beanClass.getName());
	}
	T bean = invokeCtor(ctor);
	// TODO:populate
	populatePropertiesWithRandomData(bean);
	return bean;
    }

    void populatePropertiesWithRandomData(Object bean){
	Map<String, Property> properties = extractProperties(bean.getClass());
	for( Property p:properties.values()){
	    populateProeprtyWithRandomData(p,bean);
	}
    }
    
    private void populateProeprtyWithRandomData(Property p, Object bean){
	if( p.getWrite()!=null){
	    Method setter = p.getWrite();
	    Object propertyValue = getRandom(p.getName(), p.getType(), p.genericType);
	    //TODO:option to ignore errors?
	    try {
		setter.invoke(bean, new Object[]{ propertyValue });
            } catch (IllegalArgumentException e) {
                throw new BeanException("Error invoking setter %s on property '%s' on class %s",setter.toGenericString(), p.getName(), bean.getClass().getName());
            } catch (IllegalAccessException e) {
                throw new BeanException("Error invoking setter %s on property '%s' on class %s",setter.toGenericString(), p.getName(), bean.getClass().getName());
            } catch (InvocationTargetException e) {
                throw new BeanException("Error invoking setter %s on property '%s' on class %s",setter.toGenericString(), p.getName(), bean.getClass().getName());
            }
	}
    }
    
    Map<String,Property> extractProperties(Class<?> beanClass){
	Method[] methods = beanClass.getMethods();
    
	Map<String,Property> properties = new HashMap<String, Property>();
	
	// find getters
	for (Method m : methods) {
	    if (isReaderMethod(m)) {
		if (Void.class.equals(m.getReturnType())) {
		    if (options.isFailOnInvalidGetters()) {
			throw new BeanException("Getter method %s returns void instead of a value for class %s", m.toGenericString(),beanClass.getName());
		    }
		} else {
		    String propertyName = extractPropertyName(m);
		    Property p = new Property();
		    p.setName(propertyName);
		    p.setRead(m);
		    p.setType(m.getReturnType());
		    p.setGenericType(m.getGenericReturnType());
		    
		    properties.put(p.getName(), p);
		}
	    }
	}
	//find corresponding setters
	for(Property p:properties.values()){
	    String setterName = "set" + upperFirstChar(p.getName());
	    Method setter = null;
	    try {
	        setter = beanClass.getMethod(setterName, p.getType());
            } catch (SecurityException e) {
	        //ignore
            } catch (NoSuchMethodException e) {
	        //ignore
            }
	    if( setter !=null){
		p.setWrite(setter);
	        //TODO:check generic type?
	    } else if( options.isFailOnMissingSetters() ){
		throw new BeanException("No setter named %s for property '%s' on class %s", setterName, p.getName(), beanClass.getName());
	    }
	}
	//find additional setters
	for( Method m:methods){
	    if( isWriterMethod(m)){
		String propertyName = extractPropertyName(m);
		Property p = properties.get(propertyName);
		if( p == null ){
		    if( options.isFailOnAdditionalSetters() ){
			throw new BeanException("Found additional setter %s with no corresponding getter for property '%s' on class %s",m.toGenericString(),propertyName,beanClass.getName() );
		    } else {
			p = new Property();
			p.setName(propertyName);
			p.setWrite(m);
			p.setType(m.getParameterTypes()[0]);
			p.setGenericType(m.getGenericParameterTypes()[0]); 
			
			properties.put(p.getName(), p);
		    }
		} else {
		    if( p.getWrite()==null){
			p.setWrite(m);
		    } else if( p.getWrite() != m){
			if (options.isFailOnAdditionalSetters()) {
			    throw new BeanException(
        			    "Found additional setter %s for property '%s' on class %s, an existin setter %s already exsist",
        			    m.toGenericString(), 
        			    propertyName, 
        			    beanClass.getName(), 
        			    p.getWrite().toGenericString()
        		   );
			}
		    }
		}
	    }
	}	
	return properties;
    }
    
    private boolean isReaderMethod(Method m){
	return m.getParameterTypes().length==0 && ( m.getName().startsWith("get") || m.getName().startsWith("is"));
    }
    private boolean isWriterMethod(Method m){
	return m.getParameterTypes().length==1 && ( m.getName().startsWith("set"));
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
    
    
    private static class Property {
	private Method read;
	private Method write;
	private String name;
	private Class<?> type;
	private Type genericType;
	public Method getRead() {
            return read;
        }
	public void setRead(Method read) {
            this.read = read;
        }
	public Method getWrite() {
            return write;
        }
	public void setWrite(Method write) {
            this.write = write;
        }
	public String getName() {
            return name;
        }
	public void setName(String name) {
            this.name = name;
        }
	public Class<?> getType() {
            return type;
        }
	public void setType(Class<?> type) {
            this.type = type;
        }
	public Type getGenericType() {
            return genericType;
        }
	public void setGenericType(Type genericType) {
            this.genericType = genericType;
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

    public static class BeanException extends RuntimeException {

	private static final long serialVersionUID = -6472991937598744481L;

	public BeanException(String message, Throwable cause) {
	    super(message, cause);
	}

	public BeanException(String message) {
	    super(message);
	}

	public BeanException(String message, Object... args) {
	    super(String.format(message, args));
	}

	public BeanException(String message, Throwable cause, Object... args) {
	    super(String.format(message, args), cause);
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
		} else if( propertyType.isEnum()){
		    provider = enumProvider;
		}
	    }
	}
	if (provider == null) {
	    //lets create the bean
	    if( generateBeanOfType(propertyType)){
		if( parentBeansTypesCreated.contains(propertyType)){
		    if( options.isFailOnRecursiveBeanCreation() ){
			throw new BeanException("Recursive bean creation for type %s for property %s", propertyType.getName(),propertyName);
		    } else {
			return null;
		    }
		}
		parentBeansTypesCreated.push(propertyType);
		try{
		    return populate(propertyType);
		} finally {
		    parentBeansTypesCreated.pop();
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
    
    private boolean generateBeanOfType(Class<?> type){
	String name = type.getName();
	if( containsPackageMatching(name,defaultExcludePackages)){
	    return false;
	}
	if( containsPackageMatching(name,customExcludePackages)){
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
}
