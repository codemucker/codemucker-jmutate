package org.codemucker.jmutate.generate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.GeneratorOptions;

@Retention(RetentionPolicy.RUNTIME)
@GeneratorOptions(defaultGenerator="org.codemucker.jmutate.generate.GeneratorRunnerTest.MyCodeGeneratorTwo")
/**
 * Marking a class with this annotation will cause the associated generator to be invoked
 */
public @interface GenerateTwo {
    String foo();
}