package com.bertvanbrakel.test.bean;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.bertvanbrakel.test.bean.PropertiesExtractor;
import com.bertvanbrakel.test.bean.Property;

public class PropertiesExtractorTest {

	@Test
    public void test_get_properties_ignore() {
    	PropertiesExtractor tester = new PropertiesExtractor();
    	tester.getOptions().ignoreProperty(TstBeanIgnoreProperty.class, "fieldA")
    	        .ignoreProperty(TstBeanIgnoreProperty.class, "fieldC");
    
    	Map<String, Property> properties = tester.extractProperties(TstBeanIgnoreProperty.class);
    
    	assertNotNull(properties);
    	Property p = properties.get("fieldC");
    	assertNotNull(p);
    	assertTrue(p.isIgnore());
    }

}
