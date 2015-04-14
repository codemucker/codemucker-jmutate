package org.codemucker.jmutate.generate;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.codemucker.jmatch.Expect;
import org.codemucker.jpattern.bean.Property;
import org.junit.Test;

public class SmartConfigTest {

	@Test
	public void mapsExistingPropertiesToCtor() {
		Configuration c = new BaseConfiguration();
		c.setProperty("att1", "value1");
		c.setProperty("att2", "value2");
		SmartConfig cfg = new SmartConfig();
		cfg.addNodeConfigFor(MyAnnotation.class, c);
		
		MyBeanCtorAll bean = cfg.mapFromTo(MyAnnotation.class,MyBeanCtorAll.class);

		Expect.that(bean.att1).isEqualTo("value1");
		Expect.that(bean.att2).isEqualTo("value2");
	}

	@Test
	public void mapsExistingPropertiesToSetters() {
		Configuration c = new BaseConfiguration();
		c.setProperty("att1", "value1");
		c.setProperty("att2", "value2");
		SmartConfig cfg = new SmartConfig();
		cfg.addNodeConfigFor(MyAnnotation.class, c);
		
		MyBeanSettersOnly bean = cfg.mapFromTo(MyAnnotation.class,MyBeanSettersOnly.class);

		Expect.that(bean.att1).isEqualTo("value1");
		Expect.that(bean.att2).isEqualTo("value2");
	}
	
	@Test
	public void mapsDefaultPropertiesToCtor(){ 
		Configuration c = new BaseConfiguration();
		c.setProperty("att1", "value1");
		
		SmartConfig cfg = new SmartConfig();
		cfg.addNodeConfigFor(MyAnnotationWithDefaults.class, c);
		MyBeanCtorAll bean = cfg.mapFromTo(MyAnnotationWithDefaults.class,MyBeanCtorAll.class);

		Expect.that(bean.att1).isEqualTo("value1");
		Expect.that(bean.att2).isEqualTo("defaultValue2");
	}

	@Test
	public void mapsDefaultPropertiesToSetters(){ 
		Configuration c = new BaseConfiguration();
		c.setProperty("att1", "value1");
		
		SmartConfig cfg = new SmartConfig();
		cfg.addNodeConfigFor(MyAnnotationWithDefaults.class, c);
		MyBeanSettersOnly bean = cfg.mapFromTo(MyAnnotationWithDefaults.class,MyBeanSettersOnly.class);

		Expect.that(bean.att1).isEqualTo("value1");
		Expect.that(bean.att2).isEqualTo("defaultValue2");
	}
	
	private static class MyBeanCtorAll {
		public String att1;
		public String att2;

		public MyBeanCtorAll(@Property(name = "att1") String att1,
				@Property(name = "att2") String att2) {
			super();
			this.att1 = att1;
			this.att2 = att2;
		}
	}

	public static class MyBeanSettersOnly {
		public String att1;
		public String att2;
		public String att3NotInConfig;
		
		public void setAtt1(String att1) {
			this.att1 = att1;
		}
		
		public void setAtt2(String att2) {
			this.att2 = att2;
		}
		
		public void setAtt3NotInConfig(String val) {
			this.att3NotInConfig = val;
		}
	}

	private static @interface MyAnnotation {
		String att1();
		String att2();
		String att3NotOnBean();
		
	}

	private static @interface MyAnnotationWithDefaults {
		String att1() default "defaultValue1";
		String att2() default "defaultValue2";
	}
}
