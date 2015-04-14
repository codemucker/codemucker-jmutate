package org.codemucker.jmutate.generate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.IsGeneratorTemplate;

//@Retention(RetentionPolicy.RUNTIME)
@IsGeneratorTemplate
@GenerateTwo(foo="my template", someAtt=5)
public @interface GenerateMyTemplate {
	String att1() default "att1default";
	boolean att2() default false;
	String att3();
	boolean att4();
	
}