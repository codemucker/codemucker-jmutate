package com.bertvanbrakel.test.bean;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class CollectionProvider implements RandomDataProvider {

    private final Random RANDOM = new Random();

    private final RandomDataProvider provider;

    public CollectionProvider(RandomDataProvider<?> provider) {
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
		throw new BeanException("Don't know how to create collection of type " + propertyType.getName()
		        + ", for property '" + propertyName + "'", e);
	    } catch (IllegalAccessException e) {
		throw new BeanException("Don't know how to create collection of type " + propertyType.getName()
		        + ", for property '" + propertyName + "'", e);
	    }
	} else {
	    throw new BeanException("Don't know how to create collection of type " + propertyType.getName()
		    + ", for property '" + propertyName + "'");
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