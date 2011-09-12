package com.bertvanbrakel.test.bean.builder;

import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.test.bean.BeanDefinition;

@NotThreadSafe
public class BeanReadWriter extends AbstractBeanWriter {

	public BeanReadWriter(String beanClassName) {
		this(new BeanBuilderOptions(), beanClassName);
	}
	
	public BeanReadWriter(BeanBuilderOptions options, String beanClassName){
		super(options, beanClassName + "Read");
	}

	@Override
	public void generate(BeanDefinition def){
		generatePkgDecl();
		generateImports();
		generateClassOpen();
		generatePropertyFields(def);
		generateGetters(def);
		generateFieldEquals(def);
		generateFieldClone(def);
		generateClassClose();	
	}

}