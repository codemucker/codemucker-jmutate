package com.bertvanbrakel.test.bean;

import java.lang.reflect.Type;
import java.util.Random;

public class EnumProvider implements RandomDataProvider {

	private final Random RANDOM = new Random();

	@Override
	public Object getRandom(String propertyName, Class propertyType, Type genericType) {
		if (propertyType.isEnum()) {
			Object[] enums = propertyType.getEnumConstants();
			int idx = RANDOM.nextInt(enums.length);
			return enums[idx];
		}

		throw new BeanException("Property '%s' of type %s is not an enum", propertyName, propertyType.getName());
	}
}