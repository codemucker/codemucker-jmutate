package org.codemucker.jmutate.generate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.codemucker.jpattern.generate.Access;

@Retention(RetentionPolicy.RUNTIME)
public @interface MyTestAnnotation {

	String someStringField() default "";
	Class<?> someClassField();
	Access someEnumField() default Access.PUBLIC;
}
