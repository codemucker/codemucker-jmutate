package org.codemucker.jmutate.generate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.GeneratorOptions;
import org.codemucker.jpattern.generate.IsGeneratorTemplate;

@Retention(RetentionPolicy.RUNTIME)
@IsGeneratorTemplate
@GenerateTwo(foo="my template")
public @interface GenerateMyTemplate {
	
}