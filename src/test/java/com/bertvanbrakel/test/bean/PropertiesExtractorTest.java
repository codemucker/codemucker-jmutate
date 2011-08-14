package com.bertvanbrakel.test.bean;

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
	
	public static class TstBeanExtractFields {
		private final String myField;

		public TstBeanExtractFields(String myField) {
	        super();
	        this.myField = myField;
        }

		public String getMyField() {
        	return myField;
        }
		
		
	}

}
