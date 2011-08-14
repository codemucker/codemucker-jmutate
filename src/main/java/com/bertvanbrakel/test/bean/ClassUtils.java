package com.bertvanbrakel.test.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

public class ClassUtils {

	public static <T> Constructor<T> getLongestCtor(Class<T> beanClass) {
		Constructor<T> longest = null;
		Constructor<T>[] ctors = (Constructor<T>[]) beanClass.getDeclaredConstructors();
		for (Constructor<T> ctor : ctors) {
			if (isPublic(ctor)) {
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
			if (isPublic(ctor)) {
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

	public static boolean isPublic(Class<?> type) {
		return Modifier.isPublic(type.getModifiers());
	}
	
	public static boolean isPublic(Member member) {
		return Modifier.isPublic(member.getModifiers());
	}

	public static boolean isStatic(Member member) {
		return Modifier.isStatic(member.getModifiers());
	}

	public static boolean isTransient(Member member) {
		return Modifier.isTransient(member.getModifiers());
	}

	public static boolean isVolatile(Member member) {
		return Modifier.isVolatile(member.getModifiers());
	}

	public static <T> T invokeCtorWith(Constructor<T> ctor, Object[] args) {
		try {
			T bean = ctor.newInstance(args);
			return bean;
		} catch (IllegalArgumentException e) {
			throw new BeanException("Error invoking ctor for type %s with args %s", e, ctor.getDeclaringClass().getName(), toString(args));
		} catch (InstantiationException e) {
			throw new BeanException("Error invoking ctor for type %s with args %s", e, ctor.getDeclaringClass().getName(), toString(args));
		} catch (IllegalAccessException e) {
			throw new BeanException("Error invoking ctor for type %s with args %s", e, ctor.getDeclaringClass().getName(), toString(args));
		} catch (InvocationTargetException e) {
			throw new BeanException("Error invoking ctor for type %s with args %s", e, ctor.getDeclaringClass().getName(), toString(args));
		}
	}

	public static Object invokeMethod(Object bean, Method m, Object[] args) {
		return invokeMethod(bean, m, args, false);
	}
	
	public static Object invokeMethod(Object bean, Method m, Object[] args, boolean makeAccessible) {
		try {
			boolean setAccessible = (!isPublic(m) || !isPublic(m.getDeclaringClass()) ) && makeAccessible;
			if (setAccessible) {
				m.setAccessible(true);
			}
			try {
				return m.invoke(bean, args);
			} finally {
				if (setAccessible) {
					m.setAccessible(false);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new BeanException("Error invoking method '%s' on class %s with args %s", e, m.toGenericString(), bean.getClass().getName(),toString(args));
		} catch (IllegalAccessException e) {
			throw new BeanException("Error invoking method '%s' on class %s with args %s", e, m.toGenericString(), bean.getClass().getName(),toString(args));
		} catch (InvocationTargetException e) {
			throw new BeanException("Error invoking method '%s' on class %s with args %s", e, m.toGenericString(),bean.getClass().getName(),toString(args));
		}
	}
	

	public static Object getFieldValue(Object bean, Field f){
		return getFieldValue(bean, f, false);
	}
	
	public static Object getFieldValue(Object bean, Field f, boolean makeAccessible){
		try {
			boolean setAccessible = !isPublic(f) && makeAccessible;
			if (setAccessible) {
				f.setAccessible(true);
			}
			try {
				return f.get(bean);
			} finally {
				if (setAccessible) {
					f.setAccessible(false);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new BeanException("Error getting value from field '%s' on class %s", e, f.toGenericString(), bean.getClass().getName());
		} catch (IllegalAccessException e) {
			throw new BeanException("Error getting value from field '%s' on class %s", e, f.toGenericString(), bean.getClass().getName());
		}
	}
	
	public static void setFieldValue(Object bean, Field f, Object val) {
		setFieldValue(bean, f, val, false);
	}
	
	public static void setFieldValue(Object bean, Field f, Object val, boolean makeAccessible){
		try {
			boolean setAccessible = !isPublic(f) && makeAccessible;
			if (setAccessible) {
				f.setAccessible(true);
			}
			try {
				f.set(bean,val);
			} finally {
				if (setAccessible) {
					f.setAccessible(false);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new BeanException("Error setting value on field '%s' on class %s using val %s", e, f.toGenericString(), bean.getClass().getName(), toString(val));
		} catch (IllegalAccessException e) {
			throw new BeanException("Error setting value on field '%s' on class %s using val %s", e, f.toGenericString(), bean.getClass().getName(), toString(val));
		}
	}
	
	private static String toString(Object val) {
		if (val == null) {
			return null;
		}
		if (val.getClass().isArray()) {
			return Arrays.deepToString((Object[]) val);
		}
		if (Collection.class.isAssignableFrom(val.getClass())) {
			return val.toString();
		}
		return val.toString();
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

	public static String extractPropertyName(Field f) {
		String name = f.getName();
		//todo:handle hungarian annotation
		if( name.startsWith("_")){
			return name.substring(1);
		}
		if( name.startsWith("m_")){
			return name.substring(2);
		}
		//todo:handle json field name annotations...
		//todo:allow custom annotations for field names...
		
		return name;
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

	public static Method getMethod(Class<?> beanClass, String methodName, Class<?> parameterTypes) {
		try {
			return beanClass.getMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		return null;
	}
}
