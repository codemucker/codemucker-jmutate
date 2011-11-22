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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.PropertiesExtractor;
import com.bertvanbrakel.test.bean.PropertyDefinition;

public class PropertiesExtractorTest {

	@Test
    public void test_get_properties_ignore() {
    	PropertiesExtractor tester = new PropertiesExtractor();
    	tester.getOptions().ignoreProperty(TstBeanIgnoreProperty.class, "fieldA")
    	        .ignoreProperty(TstBeanIgnoreProperty.class, "fieldC");
    	
    	BeanDefinition def = tester.extractBeanDef(TstBeanIgnoreProperty.class);
         
    	assertNotNull(def);
    	assertNotNull(def.getProperties());
    	
    	PropertyDefinition p = def.getProperty("fieldC");
    	assertNotNull(p);
    	assertTrue(p.isIgnore());
    }
	
	@Test
	public void test_field_properties(){
	 	PropertiesExtractor tester = new PropertiesExtractor();
    	tester.getOptions().extractFields(true);
    	
    	BeanDefinition def = tester.extractBeanDef(TstBeanExtractFields.class);
         
    	assertNotNull(def);
    	assertNotNull(def.getProperties());
    	
    	PropertyDefinition p = def.getProperty("myField");
    	assertNotNull(p);
    	assertNotNull(p.getRead());
    	assertNull(p.getWrite());
    	
    	assertNotNull(p.getField());
	}
	
	@Test
    public void test_get_property_name_from_annotation() {
    	PropertiesExtractor tester = new PropertiesExtractor();
    	tester.getOptions().extractFields(true);

    	BeanDefinition def = tester.extractBeanDef(TstBeanAnnotations.class);
        
    	PropertyDefinition p = def.getProperty("customName");
    	assertNotNull(p);
    	assertNotNull(p.getRead());
    	assertNotNull(p.getWrite());
    	assertNull(p.getField());
    	
    	p = def.getProperty("customName");
    	assertNotNull(p);
    	assertNotNull(p.getRead());
    	assertNotNull(p.getWrite());
    	assertNull(p.getField());
    	
    	p = def.getProperty("noMethods");
    	assertNotNull(p);
    	assertNull(p.getRead());
    	assertNull(p.getWrite());
    	assertNotNull(p.getField());
    	
    	p = def.getProperty("myFieldEmptyAnnotationName");
    	assertNotNull(p);
    	assertNull(p.getRead());
    	assertNull(p.getWrite());
    	assertNotNull(p.getField());
    	
    	p = def.getProperty("myField");
    	assertNotNull(p);
    	assertNull(p.getRead());
    	assertNull(p.getWrite());
    	assertNotNull(p.getField());
    	
    	assertEquals(4,def.getProperties().size());
    }
	
	@Test
	public void test_parent_properties_detected() {
		BeanDefinition def = new PropertiesExtractor().extractBeanDef(TstBeanParentPropertiesIncluded.class);
		
		assertTrue(def.hasProperty("fieldA"));
		assertTrue(def.hasProperty("fieldB"));
		
		assertEquals(2, def.getProperties().size());
	}
}
