package org.codemucker.jmutate.generate;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.IsGeneratorConfig;
import org.codemucker.jpattern.generate.IsGeneratorTemplate;
import org.codemucker.jtest.MavenProjectLayout;
import org.junit.Test;

import com.google.inject.Inject;

public class GeneratorRunnerTest {

    @Test
    public void generatorInvokedOnGenerationAnnotation() throws Exception {
        String pkg = GeneratorRunnerTest.class.getPackage().getName();
        File generateTo =  new MavenProjectLayout().newTmpSubDir("GenRoot");

        MyCodeGeneratorOne.reset();
        GeneratorRunner runner = GeneratorRunner.with()
                .defaults()
                .scanRoots(Roots.with().srcDirsOnly())
                .scanPackages(pkg)
                .failOnParseError(true)
                .matchGenerator(MyCodeGeneratorOne.class)
                .defaultGenerateTo(new DirectoryRoot(generateTo,RootType.GENERATED,RootContentType.SRC))
                .build();
        
        runner.run();
        
        Expect
        	.that(MyCodeGeneratorOne.nodesInvoked)
        	.is(AList.withOnly(AJType.with().fullName(BeanOne.class)));
    }
    
    @GenerateOne(foo="myfoo",ensureWeImportTypesWhenCompilingAnnon=Access.PRIVATE)
    public static class BeanOne {
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @IsGeneratorConfig(defaultGenerator="org.codemucker.jmutate.generate.GeneratorRunnerTest.MyCodeGeneratorOne")    
    public static @interface GenerateOne {
        String foo();
        String bar() default "someDefault";
        Access ensureWeImportTypesWhenCompilingAnnon() default Access.PUBLIC;
    }
    
    public static class MyCodeGeneratorOne extends AbstractGenerator<GenerateOne> {
        
    	@Inject
    	public MyCodeGeneratorOne(JMutateContext ctxt) {
			super(ctxt);
		}

		public static final List<JType> nodesInvoked = new ArrayList<>();
        public static final List<SmartConfig> configs = new ArrayList<>();

        public static void reset(){
        	nodesInvoked.clear();
        	configs.clear();
        }

        @Override
        protected void generate(JType applyToNode, SmartConfig config) {
            nodesInvoked.add(applyToNode);
        }
    }

    @Test
    public void generatorInvokedOnTemplate() throws Exception {
        String pkg = GeneratorRunnerTest.class.getPackage().getName();
        File generateTo =  new MavenProjectLayout().newTmpSubDir("GenRoot");

        MyCodeGeneratorTwo.reset();
        GeneratorRunner runner = GeneratorRunner.with()
                .defaults()
                .scanRoots(Roots.with().srcDirsOnly())
                .scanPackages(pkg)
                .failOnParseError(true)
                .matchGenerator(MyCodeGeneratorTwo.class)
                .defaultGenerateTo(new DirectoryRoot(generateTo,RootType.GENERATED,RootContentType.SRC))
                .build();
        
        runner.run();
        
        Expect
    		.that(MyCodeGeneratorTwo.nodesInvoked)
        	.is(AList.withOnly(AJType.with().fullName(BeanTwo.class)));
        
        List<SmartConfig> cfgs = MyCodeGeneratorTwo.configs;
        Expect.that(cfgs).isNotNull();
        Expect
			.that(MyCodeGeneratorTwo.configs.get(0).getConfigFor(GenerateTwo.class))
			.is(AConfig.with()
					.entry("foo", string("my template"))
					.entry("someAtt", AnInt.equalTo(5))
					.numEntries(2));
        
        GenerateTwoOptions opts = MyCodeGeneratorTwo.configs.get(0).mapFromTo(GenerateTwo.class,GenerateTwoOptions.class);
        
        Expect.that(opts.foo).isEqualTo("my template");
        Expect.that(opts.bar).isEqualTo("barDefault");
        Expect.that(opts.someAtt).isEqualTo(5);
        
    }
    
    private static Matcher<String> string(String s){
    	return AString.equalTo(s);
    }
 
    private static class GenerateTwoOptions extends GenerateOptions<GenerateTwo>{
    	public String foo;
    	public String bar;
    	public int someAtt;
    	
		public GenerateTwoOptions() {
			super();
		}
    }
   
    @GenerateMyTemplate(att3 = "att3val", att4 = true)
    public static class BeanTwo { }
    
    @IsGeneratorTemplate
    @GenerateTwo(foo="my template", someAtt=5)
    public static @interface GenerateMyTemplate {
    	String att1() default "att1default";
    	boolean att2() default false;
    	String att3();
    	boolean att4();
    	
    }

    @Retention(RetentionPolicy.RUNTIME)
    @IsGeneratorConfig(defaultGenerator="org.codemucker.jmutate.generate.GeneratorRunnerTest.MyCodeGeneratorTwo")
    public static @interface GenerateTwo {
        String foo();
        String bar() default "barDefault";
        int someAtt() default 0;
    }
    
    public static class MyCodeGeneratorTwo extends AbstractGenerator<GenerateTwo> {
        
    	@Inject
        public MyCodeGeneratorTwo(JMutateContext ctxt) {
			super(ctxt);
		}

		public static final List<JType> nodesInvoked = new ArrayList<>();
        public static final List<SmartConfig> configs = new ArrayList<>();

        public static void reset(){
        	nodesInvoked.clear();
        	configs.clear();
        }
        
        @Override
        protected void generate(JType applyToNode, SmartConfig config) {
            nodesInvoked.add(applyToNode);
            configs.add(config);
        }
    }
}
