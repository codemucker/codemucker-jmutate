package org.codemucker.jmutate.bean;

import org.codemucker.jtest.bean.BeanDefinition;
import org.codemucker.lang.annotation.NotThreadSafe;


@NotThreadSafe
@Deprecated
public class BeanWriteWriter extends AbstractBeanWriter {

	public BeanWriteWriter(String beanClassName) {
		this(new BeanBuilderOptions(), beanClassName);
	}
	
	public BeanWriteWriter(BeanBuilderOptions options, String beanClassName){
		super(options, beanClassName + "Write");
	}

	@Override
	public void generate(BeanDefinition def){
		generatePkgDecl();
		generateImports();
		generateInterfaceOpen();
		generateInterfaceSetters(def);
	//	generateFieldEquals(def);
		//generateFieldClone(def);
		generateClassClose();	
	}

}