package com.bertvanbrakel.test.bean.builder;

import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.test.bean.BeanDefinition;

@NotThreadSafe
public class WriterBean extends AbstractBeanWriter {

	public WriterBean(String beanClassName) {
		this(new BeanBuilderOptions(), beanClassName);
	}
	
	public WriterBean(BeanBuilderOptions options, String beanClassName){
		super(options,beanClassName);
	}

	public void generate(BeanDefinition def){
		generatePkgDecl();
		generateImports();
		generateClassOpen();
		generatePropertyFields(def);
		generateGetters(def);
		generateSetters(def);
		generateFieldEquals(def);
		generateFieldClone(def);
		generateAccessorCopy(def);
		generateClassClose();	
	}

}