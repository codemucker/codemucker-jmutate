package org.codemucker.jmutate.bean;

import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.test.bean.BeanDefinition;

/**
 * Write a bean with no setters, only getters
 */
@NotThreadSafe
@Deprecated
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