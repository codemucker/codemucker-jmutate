package org.codemucker.jmutate.generate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.GeneratorOptions;

@Retention(RetentionPolicy.RUNTIME)
@GeneratorOptions(defaultGenerator="org.codemucker.jmutate.generate.GeneratorRunnerTest.MyCodeGeneratorTwo")
public @interface GenerateTwo {
    String foo();
}