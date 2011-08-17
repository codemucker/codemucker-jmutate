package com.bertvanbrakel.test.bean;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

public class BeanDefinitionTest {

	@Test
	public void test_add_properties() {
		BeanDefinition def = new BeanDefinition(TstBeanSuper.class);
		PropertyDefinition p1 = new PropertyDefinition();
		p1.setName("name1");
		p1.setIgnore(false);

		PropertyDefinition p2 = new PropertyDefinition();
		p2.setName("name2");
		p2.setIgnore(false);

		def.addProperty(p1);
		def.addProperty(p2);

		assertEquals(2, def.getProperties().size());
		assertTrue(def.hasProperty("name1"));
		assertTrue(def.hasProperty("name2"));
		assertTrue(def.hasNonIgnoredProperty("name1"));
		assertTrue(def.hasNonIgnoredProperty("name2"));

		assertEquals(p1, def.getProperty("name1"));
		assertEquals(p2, def.getProperty("name2"));
	}

	@Test
	public void test_ignore_properties() {
		BeanDefinition def = new BeanDefinition(TstBeanSuper.class);
		PropertyDefinition p1 = new PropertyDefinition();
		p1.setName("name1");
		p1.setIgnore(false);

		PropertyDefinition p2 = new PropertyDefinition();
		p2.setName("name2");
		p2.setIgnore(true);

		def.addProperty(p1);
		def.addProperty(p2);

		assertEquals(2, def.getProperties().size());

		assertTrue(def.hasProperty("name1"));
		assertTrue(def.hasProperty("name2"));
		assertFalse(def.hasProperty("name3"));

		assertTrue(def.hasNonIgnoredProperty("name1"));
		assertFalse(def.hasNonIgnoredProperty("name2"));
		assertFalse(def.hasNonIgnoredProperty("name3"));
	}
}
