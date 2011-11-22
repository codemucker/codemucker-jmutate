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

import static com.bertvanbrakel.test.bean.ClassUtils.getFieldValue;
import static com.bertvanbrakel.test.bean.ClassUtils.invokeMethod;
import static com.bertvanbrakel.test.bean.ClassUtils.setFieldValue;

public class Property {

	private final PropertyDefinition def;

	public Property(PropertyDefinition def) {
		super();
		this.def = def;
	}

	public PropertyDefinition getPropertyDefinition() {
    	return def;
    }

	public Object getValue(Object bean) {
		if (def.getRead() != null) {
			return invokeMethod(bean, def.getRead(), null, def.isMakeAccessible());
		}
		if (def.getField() != null) {
			return getFieldValue(bean, def.getField(), def.isMakeAccessible());
		}
		throw new BeanException("No accessor for proeprty '%s'", def.getName());
	}

	public void setValue(Object bean, Object val) {
		if (def.getWrite() != null) {
			invokeMethod(bean, def.getWrite(), new Object[] {val}, def.isMakeAccessible());
			return;
		}
		if (def.getField() != null) {
			setFieldValue(bean, def.getField(), val, def.isMakeAccessible());
			return;
		}
		throw new BeanException("No mutator for property '%s'", def.getName());
	}
}
