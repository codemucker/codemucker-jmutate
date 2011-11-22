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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PrimitiveGenerator implements RandomGenerator<Object>{
	
	private static Map<Class<?>, RandomGenerator<?>> builtInProviders = new HashMap<Class<?>, RandomGenerator<?>>();

	private static final ExtendedRandom RANDOM = new ExtendedRandom();

	static {
		internalRegisterPrimitiveProvider(Boolean.class, Boolean.TYPE, new RandomGenerator<Boolean>() {
			public Boolean generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextBoolean();
			}
		});
		internalRegisterPrimitiveProvider(Byte.class, Byte.TYPE, new RandomGenerator<Byte>() {
			public Byte generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextByte();
			}
		});
		internalRegisterPrimitiveProvider(Character.class, Character.TYPE, new RandomGenerator<Character>() {
			public Character generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextChar();
			}
		});
		internalRegisterPrimitiveProvider(Short.class, Short.TYPE, new RandomGenerator<Short>() {
			public Short generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextShort();
			}
		});
		internalRegisterPrimitiveProvider(Integer.class, Integer.TYPE, new RandomGenerator<Integer>() {
			public Integer generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextInt();
			}
		});
		internalRegisterPrimitiveProvider(Long.class, Long.TYPE, new RandomGenerator<Long>() {
			public Long generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextLong();
			}
		});
		internalRegisterPrimitiveProvider(Float.class, Float.TYPE, new RandomGenerator<Float>() {
			public Float generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextFloat();
			}
		});
		internalRegisterPrimitiveProvider(Double.class, Double.TYPE, new RandomGenerator<Double>() {
			public Double generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextDouble();
			}
		});
		internalRegisterProvider(BigDecimal.class, new RandomGenerator<BigDecimal>() {
			public BigDecimal generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextBigDecimal();
			}
		});
		internalRegisterProvider(BigInteger.class, new RandomGenerator<BigInteger>() {
			public BigInteger generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextBigInteger();
			}
		});
		internalRegisterProvider(AtomicInteger.class, new RandomGenerator<AtomicInteger>() {
			public AtomicInteger generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return new AtomicInteger(RANDOM.nextInt());
			}
		});
		internalRegisterProvider(AtomicLong.class, new RandomGenerator<AtomicLong>() {
			public AtomicLong generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return new AtomicLong(RANDOM.nextLong());
			}
		});
		internalRegisterProvider(String.class, new RandomGenerator<String>() {
			public String generateRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return UUID.randomUUID().toString();
			}
		});
	}

	private static <T> void internalRegisterProvider(Class<T> type, RandomGenerator<T> provider) {
		builtInProviders.put(type, provider);
	}

	private static <T> void internalRegisterPrimitiveProvider(Class<T> type, Class<T> type2,
	        RandomGenerator<T> provider) {
		builtInProviders.put(type, provider);
		builtInProviders.put(type2, provider);
	}
	
	@Override
	public Object generateRandom(Class beanClass, String propertyName, Class propertyType, Type genericType) {
		RandomGenerator<?> provider = builtInProviders.get(propertyType);
		return provider.generateRandom(null, propertyName, propertyType, genericType);
	}

	public boolean supportsType(Class<?> type){
		return builtInProviders.containsKey(type);
	}
}
