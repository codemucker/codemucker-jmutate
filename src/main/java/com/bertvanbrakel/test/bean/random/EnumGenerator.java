/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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