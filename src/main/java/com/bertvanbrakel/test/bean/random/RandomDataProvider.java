package com.bertvanbrakel.test.bean.random;

import java.lang.reflect.Type;

public interface RandomDataProvider<T> {
    public T getRandom(String propertyName, Class<?> propertyType, Type genericType);
}