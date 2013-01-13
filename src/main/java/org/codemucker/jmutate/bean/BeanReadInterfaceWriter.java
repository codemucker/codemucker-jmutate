package org.codemucker.jmutate.bean;

import org.codemucker.lang.annotation.NotThreadSafe;

import com.bertvanbrakel.codemucker.annotation.Generated;
import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.util.ClassNameUtil;

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
			annotate(Generated.class);
		}
		println("public interface ${this.shortClassName}{");
    }

}