package com.bertvanbrakel.test.bean;

import org.junit.Test;

import com.bertvanbrakel.test.bean.builder.BeanBuilderGenerator;
import com.bertvanbrakel.test.bean.builder.BuilderOptions;
import com.bertvanbrakel.test.bean.random.TstBeanSetters;

public class BeanBuilderGeneratorTest {

	@Test
	public void test_generate() {

		new BeanBuilderGenerator().generateBuilder(TstBeanSetters.class, new BuilderOptions());

	}
}
