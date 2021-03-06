package org.codemucker.jmutate.generate.bean;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.generate.CodeGenerator;
import org.codemucker.jmutate.generate.GeneratorRunner;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.bean.AllArgConstructorGenerator.AllArgOptions;
import org.codemucker.jmutate.generate.bean.CloneGenerator.CloneOptions;
import org.codemucker.jmutate.generate.bean.HashCodeEqualsGenerator.HashCodeEqualsOptions;
import org.codemucker.jmutate.generate.bean.PropertiesGenerator.PropertiesOptions;
import org.codemucker.jmutate.generate.bean.ToStringGenerator.ToStringOptions;
import org.codemucker.jmutate.generate.builder.BuilderGenerator;
import org.codemucker.jmutate.generate.matcher.ManyMatchersGenerator;
import org.codemucker.jmutate.generate.matcher.MatcherGenerator;
import org.codemucker.jmutate.generate.matcher.MatcherGenerator.GenerateMatcherOptions;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jpattern.generate.GenerateAllArgsCtor;
import org.codemucker.jpattern.generate.GenerateBuilder;
import org.codemucker.jpattern.generate.GenerateCloneMethod;
import org.codemucker.jpattern.generate.GenerateHashCodeAndEquals;
import org.codemucker.jpattern.generate.GenerateProperties;
import org.codemucker.jpattern.generate.GenerateToString;
import org.codemucker.jpattern.generate.IsGeneratorConfig;
import org.codemucker.jpattern.generate.matcher.GenerateManyMatchers;
import org.codemucker.jpattern.generate.matcher.GenerateMatcher;
import org.codemucker.jtest.MavenProjectLayout;
import org.codemucker.lang.BeanNameUtil;
import org.junit.Test;

import com.google.inject.Inject;

import org.codemucker.jmutate.generate.bean.AbstractBeanGenerator;
import org.codemucker.jmutate.generate.bean.AbstractBeanOptions;
import org.codemucker.jmutate.generate.bean.CloneGenerator;
import org.codemucker.jmutate.generate.bean.GenerateMyTestBean;
import org.codemucker.jmutate.generate.bean.HashCodeEqualsGenerator;
import org.codemucker.jmutate.generate.bean.PropertiesGenerator;
import org.codemucker.jmutate.generate.bean.ToStringGenerator;

public class BeanGeneratorsTest {

    String pkg = BeanGeneratorsTest.class.getPackage().getName();
    File generateTo =  new MavenProjectLayout().newTmpSubDir("GenRoot");
    
	@Test
	public void ensureDefaultGeneratorSetOnAnnotations(){
		assertCorrectGenerator(GenerateToString.class, ToStringGenerator.class);
		assertCorrectGenerator(GenerateHashCodeAndEquals.class,HashCodeEqualsGenerator.class);
		assertCorrectGenerator(GenerateProperties.class, PropertiesGenerator.class);
		assertCorrectGenerator(GenerateCloneMethod.class, CloneGenerator.class);
		assertCorrectGenerator(GenerateBuilder.class, BuilderGenerator.class);
		assertCorrectGenerator(GenerateManyMatchers.class, ManyMatchersGenerator.class);
		assertCorrectGenerator(GenerateMatcher.class, MatcherGenerator.class);
		
	}
	
	//TODO:random bean fill!
	
	
	
	private <T extends Annotation> void assertCorrectGenerator(Class<T> genOptions,Class<? extends CodeGenerator<T>> generatorClass){
		IsGeneratorConfig opts = genOptions.getAnnotation(IsGeneratorConfig.class);
		Expect.that(opts).isNotNull();
		Expect.that(opts.defaultGenerator()).isEqualTo(generatorClass.getName());
	}
	
	@Test
	public void ensureMethodSettersOrFieldsMatchThoseInGeneratorAnnotation(){
		assertPropertiesMatch(AllArgOptions.class,GenerateAllArgsCtor.class);
		assertPropertiesMatch(CloneOptions.class,GenerateCloneMethod.class);
		assertPropertiesMatch(HashCodeEqualsOptions.class,GenerateHashCodeAndEquals.class);
		assertPropertiesMatch(ToStringOptions.class,GenerateToString.class);
		assertPropertiesMatch(PropertiesOptions.class, GenerateProperties.class);
		//ensurePropertiesMatch(BuilderGenerator.BuilderOptions.class,GenerateBuilder.class);
		assertPropertiesMatch(GenerateMatcherOptions.class,GenerateMatcher.class);
	}
	
	//ensure the options and annotation class map to each other
	private void assertPropertiesMatch(Class<?> optionsClass, Class<? extends Annotation> annotationClass){
		for(Method m:annotationClass.getDeclaredMethods()){
			String propertyName = BeanNameUtil.methodToPropertyName(m.getName());
			ensureOptionHasSetterOrField(optionsClass,propertyName,m.getReturnType(),annotationClass);
		}
	}
	
	private void ensureOptionHasSetterOrField(Class<?> optionClass,String name, Class<?> annotationType,Class<? extends Annotation> annotationClass){
		Method setter = null;
		try {
			String setterName = BeanNameUtil.toSetterName(name);
			for(Method m:optionClass.getMethods()){
				//find setter/builder method for given property
				if(m.getParameterCount() == 1 && (m.getName().equals(setterName)||m.getName().equals(name)) && typesCompatible(annotationType, m.getParameterTypes()[0])){
					setter = m;
					break;
				}
			}
		} catch (SecurityException e) {}
		
		Field fieldSetter = null;
		try {
			fieldSetter = optionClass.getField(name);
		} catch (NoSuchFieldException | SecurityException e) {}
		

		Expect.with()
			.failureMessage("expected options class " + optionClass.getName() +" to have a setter or field for property '" + name + "' (for annotation " + annotationClass.getName()+ ") compatible with type " + annotationType)
			.that(setter != null || fieldSetter!= null).isTrue();
		
		Class<?> setterType = fieldSetter==null?setter.getParameterTypes()[0]:fieldSetter.getType();
		boolean compaitbleTypes = typesCompatible(annotationType, setterType);
		
		Expect.with()
			.failureMessage("expected options class " + optionClass.getName() +" property '" + name + "' to be of type " + annotationType + " but was " + setterType )
			.that(compaitbleTypes).isTrue();
	}
	
	private boolean typesCompatible(Class<?> annotationType,Class<?> optionType){
		if( annotationType == Class.class && optionType == String.class){
			return true;
		}
		return optionType.equals(annotationType);
	}
	
	@Test
	public void ensureOptionsArePopulated(){

        MyTestBeanGenerator.nodesInvoked.clear();
        GeneratorRunner runner = GeneratorRunner.with()
                .defaults()
                .scanRoots(Roots.with().srcDirsOnly())
                .scanPackages(pkg)
                .failOnParseError(true)
                .matchGenerator(MyTestBeanGenerator.class)
                .defaultGenerateTo(new DirectoryRoot(generateTo,RootType.GENERATED,RootContentType.SRC))
                .build();
        
        runner.run();
        
        Expect
    		.with().debugEnabled(true)
        	.that(MyTestBeanGenerator.nodesInvoked)
        	.is(AList.withOnly(AJType.with().fullName(MyBean.class)));
        
        BeanOptions opts = MyTestBeanGenerator.optionsPassed.get(0);
        
        Expect.that(opts.att1).isTrue();
        Expect.that(opts.att2).isEqualTo("att2default");
        Expect.that(opts.att3).isEqualTo("att3val");
        Expect.that(opts.att4).isTrue();
        Expect.that(opts.isEnabled()).isTrue();

    }
	
	@GenerateMyTestBean(att3="att3val", att4=true)
    public static class MyBean {
    	
    }

    public static class BeanOptions extends AbstractBeanOptions<GenerateMyTestBean>{

    	public boolean att1;
    	public String att2;
    	public String att3;
    	public boolean att4;
    	
		public BeanOptions(Configuration config,JType pojoType) {
			super(config,GenerateMyTestBean.class,pojoType);
		}
    	
    }
    
    public static class MyTestBeanGenerator extends AbstractBeanGenerator<GenerateMyTestBean,BeanOptions> {

		public static final List<JType> nodesInvoked = new ArrayList<>();
		public static final List<BeanOptions> optionsPassed = new ArrayList<>();

		@Inject
		public MyTestBeanGenerator(JMutateContext ctxt) {
			super(ctxt, GenerateMyTestBean.class);
		}

		@Override
		protected BeanOptions createOptionsFrom(Configuration config, JType type) {
			return new BeanOptions(config,type);
		}

		@Override
		protected void generate(JType bean, SmartConfig config,PojoModel model, BeanOptions opts) {
			 nodesInvoked.add(bean);
			 optionsPassed.add(opts);
		}
		
        

    }
}
