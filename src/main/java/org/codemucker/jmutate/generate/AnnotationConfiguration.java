package org.codemucker.jmutate.generate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import org.codemucker.jmutate.ast.JAnnotation;
import org.eclipse.jdt.core.dom.Annotation;

/**
 * Extracts the config from either a source or compiled annotation
 */
public class AnnotationConfiguration extends MapConfiguration {
	private static final Object[] NO_ARGS = new Object[] {};
	private static Collection<String> IGNORE_METHODS = new ArrayList<>();

	static {
		for (Method m : Object.class.getMethods()) {
			IGNORE_METHODS.add(m.getName());
		}
	}

	public AnnotationConfiguration(Annotation a) {
		this(JAnnotation.from(a));
	}

	public AnnotationConfiguration(JAnnotation a) {
		//TODO:need to extract defaults!!
		super(a.getAttributeMap());
	}

	public AnnotationConfiguration(java.lang.annotation.Annotation a) {
		super();
		extractConfig(a);
	}

	private void extractConfig(java.lang.annotation.Annotation a) {
		Method[] methods = a.getClass().getDeclaredMethods();
		for (Method m : methods) {
			if (!includeAnnotationMethod(m)) {
				continue;
			}
			String name = m.getName();
			try {
				Object val = m.invoke(a, NO_ARGS);
				if (val instanceof Enum<?>) { // from source code expands to
												// full name
					Enum<?> en = (Enum<?>) val;
					val = en.getClass().getName() + "." + en.name();
				} else if (val instanceof Class<?>) {// in source code expanded
														// to fully qualified
														// name
					val = ((Class<?>) val).getName();
				}
				addProperty(name, val);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// should never be thrown
				throw new RuntimeException(
						"Error extracting value from annotation method '"
								+ name + "'", e);
			}
		}
	}

	public static boolean includeAnnotationMethod(Method m) {
		if (m.getReturnType() == Void.class
				|| !Modifier.isPublic(m.getModifiers()) || m.isSynthetic()
				|| m.getParameterCount() != 0
				|| IGNORE_METHODS.contains(m.getName())) {
			return false;
		}
		return true;
	}
}