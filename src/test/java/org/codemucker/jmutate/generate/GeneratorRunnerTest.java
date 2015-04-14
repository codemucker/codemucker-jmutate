package org.codemucker.jmutate.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jtest.MavenProjectLayout;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.junit.Test;

public class GeneratorRunnerTest {

    @Test
    public void generatorInvoked() throws Exception {
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
    
    public static class MyCodeGeneratorOne extends AbstractCodeGenerator<GenerateOne> {
        
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
    public void generatorTemplateInvoked() throws Exception {
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
			.that(MyCodeGeneratorTwo.configs.get(0).getConfigFor(GenerateMyTemplate.class))
			.is(AConfig.with()
				//	.entry("att1", true)
				//	.entry("att2", "att2default")
					.entry("att3", "att3val")
					.entry("att4", true));
        Expect
			.that(MyCodeGeneratorTwo.configs.get(0).getConfigFor(GenerateTwo.class))
			.is(AConfig.with().entry("foo", "my template"));
        
        GenerateTwoOptions opts = MyCodeGeneratorTwo.configs.get(0).mapFromTo(GenerateTwo.class,GenerateTwoOptions.class);
        
        Expect.that(opts.foo).isEqualTo("my template");
        Expect.that(opts.bar).isEqualTo("barDefault");
        Expect.that(opts.someAtt).isEqualTo(5);
        
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
    public static class BeanTwo {
    	
    }
    
    public static class MyCodeGeneratorTwo extends AbstractCodeGenerator<GenerateTwo> {
        
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
