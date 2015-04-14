package org.codemucker.jmutate.generate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.IsGeneratorConfig;

@Retention(RetentionPolicy.RUNTIME)
@IsGeneratorConfig(defaultGenerator="org.codemucker.jmutate.generate.GeneratorRunnerTest.MyCodeGeneratorTwo")
/**
 * Marking a class with this annotation will cause the associated generator to be invoked
 */
public @interface GenerateTwo {
    String foo();
    String bar() default "barDefault";
    int someAtt() default 0;
}