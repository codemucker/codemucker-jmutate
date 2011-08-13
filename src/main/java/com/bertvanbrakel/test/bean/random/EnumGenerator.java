package com.bertvanbrakel.test.bean.random;

import java.lang.reflect.Type;
import java.util.Random;

import com.bertvanbrakel.test.bean.BeanException;

public class EnumGenerator implements RandomGenerator {

	private final Random RANDOM = new Random();

	@Override
	public Object generateRandom(Class bean, String propertyName, Class propertyType, Type genericType) {
		if (propertyType.isEnum()) {
			Object[] enums = propertyType.getEnumConstants();
			int idx = RANDOM.nextInt(enums.length);
			return enums[idx];
		}

		throw new BeanException("Property '%s' of type %s is not an enum", propertyName, propertyType.getName());
	}
}