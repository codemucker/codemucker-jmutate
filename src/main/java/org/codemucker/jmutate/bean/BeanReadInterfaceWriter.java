package org.codemucker.jmutate.bean;

import org.codemucker.jpattern.IsGenerated;
import org.codemucker.jtest.ClassNameUtil;
import org.codemucker.jtest.bean.BeanDefinition;
import org.codemucker.lang.annotation.NotThreadSafe;


/**
 * Write a bean as an interface
 */
@NotThreadSafe
@Deprecated
public class BeanReadInterfaceWriter extends AbstractBeanWriter {

	public BeanReadInterfaceWriter(String beanClassName) {
		this(new BeanBuilderOptions(), beanClassName);
	}
	
	public BeanReadInterfaceWriter(BeanBuilderOptions options, String beanClassName){
		super(options, ClassNameUtil.insertBeforeClassName(beanClassName, "I"));
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
			annotate(IsGenerated.class);
		}
		println("public interface ${this.shortClassName}{");
    }

}