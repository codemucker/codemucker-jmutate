package org.codemucker.jmutate.generate.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.generate.CodeGenerator;
import org.codemucker.jmutate.generate.builder.BuilderGenerator;
import org.codemucker.jmutate.generate.matcher.MatcherGenerator;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateBean;
import org.codemucker.jpattern.generate.GenerateBuilder;
import org.codemucker.jpattern.generate.GenerateCloneMethod;
import org.codemucker.jpattern.generate.GenerateHashCodeAndEqualsMethod;
import org.codemucker.jpattern.generate.GenerateMatchers;
import org.codemucker.jpattern.generate.GenerateProperties;
import org.codemucker.jpattern.generate.GenerateToStringMethod;
import org.codemucker.jpattern.generate.IsGeneratorConfig;
import org.junit.Test;

public class BeanGeneratorsTest {

	@Test
	public void ensureDefaultGeneratorSetOnAnnotations(){
		
		isCorrectGenerator(GenerateBean.class, BeanGenerator.class);
		isCorrectGenerator(GenerateToStringMethod.class, ToStringGenerator.class);
		isCorrectGenerator(GenerateHashCodeAndEqualsMethod.class,HashCodeEqualsGenerator.class);
		isCorrectGenerator(GenerateProperties.class, PropertiesGenerator.class);
		isCorrectGenerator(GenerateCloneMethod.class, CloneGenerator.class);
		isCorrectGenerator(GenerateBuilder.class, BuilderGenerator.class);
		isCorrectGenerator(GenerateMatchers.class, MatcherGenerator.class);
	}
	
	private <T extends Annotation> void isCorrectGenerator(Class<T> genOptions,Class<? extends CodeGenerator<T>> generatorClass){
		IsGeneratorConfig opts = genOptions.getAnnotation(IsGeneratorConfig.class);
		Expect.that(opts).isNotNull();
		Expect.that(opts.defaultGenerator()).isEqualTo(generatorClass.getName());
	}
	
	@Test
	public void ensureMethodNamesMatchThoseInBeanAnnotation(){
		matchBeanGen(GenerateBean.class);
		matchBeanGen(GenerateToStringMethod.class);
		matchBeanGen(GenerateHashCodeAndEqualsMethod.class);
		matchBeanGen(GenerateProperties.class);
		matchBeanGen(GenerateCloneMethod.class);
		matchBeanGen(GenerateBuilder.class);
		matchBeanGen(GenerateMatchers.class);
	}
	
	private <T extends Annotation> void matchBeanGen(Class<T> options){
		ensureHasMethodReturning(options,GenerateBeanOptions.PROP_ENABLED,boolean.class);
		ensureHasMethodReturning(options,GenerateBeanOptions.PROP_MARK_GENERATED,boolean.class);
		ensureHasMethodReturning(options,GenerateBeanOptions.PROP_FIELDNAMES,String.class);
		ensureHasMethodReturning(options,GenerateBeanOptions.PROP_CLASH_STRATEGY,ClashStrategy.class);
		ensureHasMethodReturning(options,GenerateBeanOptions.PROP_INHERIT_PROPERTIES,boolean.class);
	}
	
	private void ensureHasMethodReturning(Class<?> opts,String methodName, Class returnType){
		Method m = null;
		try {
			m = opts.getMethod(methodName, null);
		} catch (NoSuchMethodException | SecurityException e) {}
		
		Expect.with()
			.failureMessage("expected annotation value '" + methodName +"' on " + opts.getName())
			.that(m).isNotNull();
		
		Class actualReturnType = m.getReturnType();
		Expect.with()
			.failureMessage("expected annotation value '" + methodName +"' on " + opts.getName() + " to return " + returnType + " but was " + actualReturnType )
			.that(actualReturnType).isEqualTo(returnType);
	
	}
	
}
