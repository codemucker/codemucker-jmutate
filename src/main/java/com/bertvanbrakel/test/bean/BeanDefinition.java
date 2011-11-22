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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BeanDefinition {

	private final Class<?> beanType;
	private Constructor ctor;
	private Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();

	public BeanDefinition(Class<?> type) {
		this.beanType = type;
	}

	public void setCtor(Constructor ctor) {
		this.ctor = ctor;
	}

	public Constructor getCtor() {
		return ctor;
	}

	public Class<?> getBeanType() {
		return beanType;
	}

	public boolean hasProperty(String name) {
		if (name == null) {
			return false;
		}
		return properties.containsKey(name);
	}

	public boolean hasNonIgnoredProperty(String name) {
		if (name == null) {
			return false;
		}
		PropertyDefinition p = properties.get(name);
		return p != null && !p.isIgnore();
	}

	public Collection<PropertyDefinition> getProperties() {
		return properties.values();
	}

	public PropertyDefinition getProperty(String name) {
		return properties.get(name);
	}

	public Collection<String> getPropertyNames() {
		return properties.keySet();
	}

	public void setPropertyMap(Map<String, PropertyDefinition> properties) {
		this.properties = properties;
	}

	public void addProperty(PropertyDefinition p) {
		this.properties.put(p.getName(), p);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}