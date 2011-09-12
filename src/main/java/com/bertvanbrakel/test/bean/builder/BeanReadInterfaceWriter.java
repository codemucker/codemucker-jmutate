package com.bertvanbrakel.test.bean.builder;

import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.ClassUtils;

@NotThreadSafe
public class BeanReadInterfaceWriter extends AbstractBeanWriter {

	public BeanReadInterfaceWriter(String beanClassName) {
		this(new BeanBuilderOptions(), beanClassName);
	}
	
	public BeanReadInterfaceWriter(BeanBuilderOptions options, String beanClassName){
		super(options, ClassUtils.insertBeforeClassName(beanClassName, "I"));
	}

	@Override
	public void generate(BeanDefinition def){
		generatePkgDecl();
		generateImports();
		generateInterfaceOpen();
		generateInterfaceGetters(def);
		//generateInterfaceSetters(def);
		generateClassClose();	
	}
	
	protected void generateInterfaceOpen() {
        if (options.isMarkPatternOnClass()) {
			annotate(Generated.class);
		}
		println("public interface ${this.shortClassName}{");
    }

}