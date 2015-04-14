package org.codemucker.jmutate.generate.bean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.IsGeneratorConfig;

@Retention(RetentionPolicy.RUNTIME)
@IsGeneratorConfig(defaultGenerator="org.codemucker.jmutate.generate.bean.BeanGeneratorsTest.MyTestBeanGenerator")
public @interface GenerateMyTestBean {
	boolean att1() default true;
	String att2() default "att2default";
	String att3();
	boolean att4() default false;
	
	boolean enabled() default true;
}