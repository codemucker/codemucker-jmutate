package com.bertvanbrakel.test.bean;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import com.bertvanbrakel.test.bean.builder.BeanBuilderGenerator;
import com.bertvanbrakel.test.bean.builder.GeneratorOptions;

public class BeanBuilderGeneratorTest {

	@Test
	public void test_generate() {

		BeanDefinition def = new BeanDefinition(Void.class);
		for (Class<?> type : Arrays.asList(Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Character.TYPE,
		        Character.class, Short.TYPE, Short.class, Integer.TYPE, Integer.class, Long.TYPE, Long.class,
		        Float.TYPE, Float.class, Double.TYPE, Double.class, String.class, Date.class)) {
			add(def, type);
		}

		new BeanBuilderGenerator().generate("com.bertvanbrakel.test.AutoBean", def, new GeneratorOptions());
	}

	private void add(BeanDefinition def, Class<?> propertyType) {
		String name;
		if (propertyType.isPrimitive()) {
			name = "primitive" + ClassUtils.upperFirstChar(propertyType.getSimpleName());
		} else {
			name = ClassUtils.lowerFirstChar( propertyType.getSimpleName() );
		}
		add(def, name, propertyType);
	}

	private void add(BeanDefinition def, String propertyName, Class<?> propertyType) {
		def.addProperty(prop(propertyName, propertyType));
	}

	private PropertyDefinition prop(String name, Class<?> type) {
		PropertyDefinition p = new PropertyDefinition();
		p.setIgnore(false);
		p.setName(name);
		p.setType(type);
		return p;
	}
}
