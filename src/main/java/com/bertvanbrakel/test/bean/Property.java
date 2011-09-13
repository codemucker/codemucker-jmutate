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
