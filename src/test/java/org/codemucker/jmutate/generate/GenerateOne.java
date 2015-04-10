package org.codemucker.jmutate.generate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.IsGeneratorConfig;

@Retention(RetentionPolicy.RUNTIME)
@IsGeneratorConfig(defaultGenerator="org.codemucker.jmutate.generate.GeneratorRunnerTest.MyCodeGeneratorOne")
/**
 * Marking a class with this annotation will cause the associated generator to be invoked
 */
public @interface GenerateOne {
    String foo();
    String bar() default "someDefault";
    Access ensureWeImportTypesWhenCompilingAnnon() default Access.PUBLIC;
}