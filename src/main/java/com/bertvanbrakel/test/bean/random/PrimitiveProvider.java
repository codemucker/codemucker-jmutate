package com.bertvanbrakel.test.bean.random;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PrimitiveProvider implements RandomDataProvider<Object>{
	
	private static Map<Class<?>, RandomDataProvider<?>> builtInProviders = new HashMap<Class<?>, RandomDataProvider<?>>();

	private static final ExtendedRandom RANDOM = new ExtendedRandom();

	static {
		internalRegisterPrimitiveProvider(Boolean.class, Boolean.TYPE, new RandomDataProvider<Boolean>() {
			public Boolean getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextBoolean();
			}
		});
		internalRegisterPrimitiveProvider(Byte.class, Byte.TYPE, new RandomDataProvider<Byte>() {
			public Byte getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextByte();
			}
		});
		internalRegisterPrimitiveProvider(Character.class, Character.TYPE, new RandomDataProvider<Character>() {
			public Character getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextChar();
			}
		});
		internalRegisterPrimitiveProvider(Short.class, Short.TYPE, new RandomDataProvider<Short>() {
			public Short getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextShort();
			}
		});
		internalRegisterPrimitiveProvider(Integer.class, Integer.TYPE, new RandomDataProvider<Integer>() {
			public Integer getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextInt();
			}
		});
		internalRegisterPrimitiveProvider(Long.class, Long.TYPE, new RandomDataProvider<Long>() {
			public Long getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextLong();
			}
		});
		internalRegisterPrimitiveProvider(Float.class, Float.TYPE, new RandomDataProvider<Float>() {
			public Float getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextFloat();
			}
		});
		internalRegisterPrimitiveProvider(Double.class, Double.TYPE, new RandomDataProvider<Double>() {
			public Double getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextDouble();
			}
		});
		internalRegisterProvider(BigDecimal.class, new RandomDataProvider<BigDecimal>() {
			public BigDecimal getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextBigDecimal();
			}
		});
		internalRegisterProvider(BigInteger.class, new RandomDataProvider<BigInteger>() {
			public BigInteger getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return RANDOM.nextBigInteger();
			}
		});
		internalRegisterProvider(AtomicInteger.class, new RandomDataProvider<AtomicInteger>() {
			public AtomicInteger getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return new AtomicInteger(RANDOM.nextInt());
			}
		});
		internalRegisterProvider(AtomicLong.class, new RandomDataProvider<AtomicLong>() {
			public AtomicLong getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
				return new AtomicLong(RANDOM.nextLong());
			}
		});
		internalRegisterProvider(String.class, new RandomDataProvider<String>() {
			public String getRandom(Class bean, String propertyName, Class<?> propertyType, Type genericType) {
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
	public Object getRandom(Class beanClass, String propertyName, Class propertyType, Type genericType) {
		RandomDataProvider<?> provider = builtInProviders.get(propertyType);
		return provider.getRandom(null, propertyName, propertyType, genericType);
	}

	public boolean supportsType(Class<?> type){
		return builtInProviders.containsKey(type);
	}
}
