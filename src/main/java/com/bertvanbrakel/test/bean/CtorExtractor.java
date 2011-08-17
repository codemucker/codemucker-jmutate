package com.bertvanbrakel.test.bean;

import static com.bertvanbrakel.test.bean.ClassUtils.getLongestCtor;
import static com.bertvanbrakel.test.bean.ClassUtils.getNoArgCtor;
import static com.bertvanbrakel.test.bean.ClassUtils.isPublic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import com.bertvanbrakel.test.bean.annotation.BeanProperty;

public class CtorExtractor {

	private final BeanOptions options;

	public CtorExtractor() {
		this(new BeanOptions());
	}

	public CtorExtractor(BeanOptions options) {
		this.options = options;
	}

	public BeanOptions getOptions() {
		return options;
	}

	public Collection<CtorDefinition> extractCtors(Class<?> beanClass) {
		Collection<CtorDefinition> defs = new ArrayList<CtorDefinition>();
		for (Constructor ctor : beanClass.getDeclaredConstructors()) {
			CtorDefinition ctorDef = extractCtorDef(ctor);
			defs.add(ctorDef);
		}
		return defs;
	}

	private CtorDefinition extractCtorDef(Constructor ctor) {
		CtorDefinition ctorDef = new CtorDefinition(ctor);

		if( !isPublic(ctor) && options.isMakeAccessible()){
			ctorDef.setMakeAccessible(true);
		}
		
		Class[] types = ctor.getParameterTypes();
		Type[] genericTypes = ctor.getGenericParameterTypes();
		Annotation[][] annotations = ctor.getParameterAnnotations();

		for (int i = 0; i < types.length; i++) {
			CtorArgDefinition argDef = extractArgDef(types[i], genericTypes[i], annotations[i]);
			ctorDef.addArg(argDef);
		}
		return ctorDef;
	}

	private CtorArgDefinition extractArgDef(Class<?> type, Type genericType, Annotation[] annotations) {
		CtorArgDefinition def = new CtorArgDefinition();
		def.setType(type);
		def.setGenericType(genericType);
		def.setName(extractArgName(annotations));

		return def;
	}

	private String extractArgName(Annotation[] annotations) {
		String name = null;
		for (Annotation a : annotations) {
			if (BeanProperty.class.isAssignableFrom(a.getClass())) {
				name = ((BeanProperty) a).name().trim();
				if (name.length() == 0) {
					name = null;
				}
			}
		}
		return name;
	}

	private Constructor<?> findCtorFor(Class<?> beanClass) {
		Constructor<?> ctor = getNoArgCtor(beanClass, false);
		if (ctor == null) {
			ctor = getLongestCtor(beanClass);
		}
		if (ctor == null) {
			ctor = getNoArgCtor(beanClass, true);
		}
		return ctor;
	}
}
