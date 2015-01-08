package org.codemucker.jmutate.generate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.GeneratorOptions;

@Retention(RetentionPolicy.RUNTIME)
@GeneratorOptions(defaultGenerator="org.codemucker.jmutate.generate.GeneratorRunnerTest.MyCodeGeneratorOne")
public @interface GenerateOne {
    String foo();
    String bar() default "someDefault";
    Access ensureWeImportTypesWhenCompilingAnnon() default Access.PUBLIC;
}