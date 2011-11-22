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
package com.bertvanbrakel.test.bean;

import static com.bertvanbrakel.test.bean.ClassUtils.getLongestCtor;
import static com.bertvanbrakel.test.bean.ClassUtils.getNoArgCtor;
import static com.bertvanbrakel.test.bean.ClassUtils.isPublic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import com.bertvanbrakel.codemucker.annotation.BeanProperty;

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
