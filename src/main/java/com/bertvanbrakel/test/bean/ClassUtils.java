package com.bertvanbrakel.test.bean;

import static com.bertvanbrakel.test.bean.ClassUtils.lowerFirstChar;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ClassUtils {

	public static <T> Constructor<T> getLongestCtor(Class<T> beanClass) {
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

	public static <T> Constructor<T> getNoArgCtor(Class<T> beanClass, boolean changeAccessibility) {
		try {
			Constructor<T> ctor = beanClass.getDeclaredConstructor(null);
			if (isCtorPublic(ctor)) {
				return ctor;
			} else if (changeAccessibility) {
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

	public static boolean isCtorPublic(Constructor<?> ctor) {
		return Modifier.isPublic(ctor.getModifiers());
	}
	
	public static <T> T invokeCtorWith(Constructor<T> ctor, Object[] args) {
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
	
	public static boolean isReaderMethod(Method m) {
		return m.getParameterTypes().length == 0 && (m.getName().startsWith("get") || m.getName().startsWith("is"));
	}

	public static boolean isWriterMethod(Method m) {
		return m.getParameterTypes().length == 1 && (m.getName().startsWith("set"));
	}
	
	public static String extractPropertyName(Method m) {
		String name = m.getName();
		if (name.startsWith("get")) {
			return lowerFirstChar(name.substring(3));
		} else if (name.startsWith("is")) {
			return lowerFirstChar(name.substring(2));
		} else if (name.startsWith("set")) {
			return lowerFirstChar(name.substring(3));
		}
		throw new BeanException("Don't know how to extract the property name from method name " + name);
	}


	public static String lowerFirstChar(String name) {
		if (name.length() > 1) {
			return Character.toLowerCase(name.charAt(0)) + name.substring(1);
		} else {
			return Character.toLowerCase(name.charAt(0)) + "";
		}
	}

	public static String upperFirstChar(String name) {
		if (name.length() > 1) {
			return Character.toUpperCase(name.charAt(0)) + name.substring(1);
		} else {
			return Character.toUpperCase(name.charAt(0)) + "";
		}
	}
}
