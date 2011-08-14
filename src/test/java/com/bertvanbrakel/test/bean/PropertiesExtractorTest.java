package com.bertvanbrakel.test.bean;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

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
}
