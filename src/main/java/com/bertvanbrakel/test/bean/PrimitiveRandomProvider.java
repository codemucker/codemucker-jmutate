package com.bertvanbrakel.test.bean;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PrimitiveRandomProvider implements RandomDataProvider<Object>{
	
	private static Map<Class<?>, RandomDataProvider<?>> builtInProviders = new HashMap<Class<?>, RandomDataProvider<?>>();

	private static final ExtendedRandom RANDOM = new ExtendedRandom();

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

	private static <T> void internalRegisterPrimitiveProvider(Class<T> type, Class<T> type2,
	        RandomDataProvider<T> provider) {
		builtInProviders.put(type, provider);
		builtInProviders.put(type2, provider);
	}
	
	@Override
	public Object getRandom(String propertyName, Class propertyType, Type genericType) {
		RandomDataProvider<?> provider = builtInProviders.get(propertyType);
		return provider.getRandom(propertyName, propertyType, genericType);
	}

	public boolean supportsType(Class<?> type){
		return builtInProviders.containsKey(type);
	}
}
