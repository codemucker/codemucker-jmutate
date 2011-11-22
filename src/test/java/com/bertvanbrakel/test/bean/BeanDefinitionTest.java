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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.PropertyDefinition;

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
