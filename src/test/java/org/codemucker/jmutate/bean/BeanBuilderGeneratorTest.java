package org.codemucker.jmutate.bean;

import java.util.Arrays;
import java.util.Date;

import org.codemucker.jmutate.bean.BeanBuilderGenerator;
import org.codemucker.jmutate.bean.GeneratorOptions;
import org.junit.Test;

import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.PropertyDefinition;
import com.bertvanbrakel.test.util.ClassNameUtil;

@Deprecated
public class BeanBuilderGeneratorTest {
	
	@Test
	public void test_generate() {
		BeanDefinition def = new BeanDefinition(Void.class);
		for (Class<?> type : Arrays.asList(Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Character.TYPE,
		        Character.class, Short.TYPE, Short.class, Integer.TYPE, Integer.class, Long.TYPE, Long.class,
		        Float.TYPE, Float.class, Double.TYPE, Double.class, String.class, Date.class)) {
			addProperty(def, type);
		}

		new BeanBuilderGenerator().generate("com.bertvanbrakel.codegen.bean.AutoBean", def, new GeneratorOptions());
	}

	private void addProperty(BeanDefinition def, Class<?> propertyType) {
		String name;
		if (propertyType.isPrimitive()) {
			name = "primitive" + ClassNameUtil.upperFirstChar(propertyType.getSimpleName());
		} else {
			name = ClassNameUtil.lowerFirstChar( propertyType.getSimpleName() );
		}
		addProperty(def, name, propertyType);
	}

	private void addProperty(BeanDefinition def, String propertyName, Class<?> propertyType) {
		def.addProperty(property(propertyName, propertyType));
	}

	private PropertyDefinition property(String name, Class<?> type) {
		PropertyDefinition p = new PropertyDefinition();
		p.setIgnore(false);
		p.setName(name);
		p.setType(type);
		return p;
	}
}
