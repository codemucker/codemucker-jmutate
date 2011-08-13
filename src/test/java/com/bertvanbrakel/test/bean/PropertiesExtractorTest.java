package com.bertvanbrakel.test.bean;

import static junit.framework.Assert.assertNotNull;
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
    	
    	Property p = def.getProperty("fieldC");
    	assertNotNull(p);
    	assertTrue(p.isIgnore());
    }

}
