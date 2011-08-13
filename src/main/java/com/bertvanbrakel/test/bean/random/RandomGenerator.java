package com.bertvanbrakel.test.bean.random;

import java.lang.reflect.Type;

public interface RandomGenerator<T> {
    public T generateRandom(Class beanClass, String propertyName, Class<?> propertyType, Type genericType);
}