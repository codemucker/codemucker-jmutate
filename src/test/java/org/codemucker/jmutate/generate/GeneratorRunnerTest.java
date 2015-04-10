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
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.DontGenerate;
import org.codemucker.jtest.MavenProjectLayout;
import org.junit.Test;

public class GeneratorRunnerTest {

    @Test
    public void generatorInvoked() throws Exception {
        String pkg = GeneratorRunnerTest.class.getPackage().getName();
        File generateTo =  new MavenProjectLayout().newTmpSubDir("GenRoot");

        MyCodeGeneratorOne.nodesInvoked.clear();
        GeneratorRunner runner = GeneratorRunner.with()
                .defaults()
                .scanRoots(Roots.with().srcDirsOnly())
                .scanPackages(pkg)
                .failOnParseError(true)
                .matchGenerator(AString.contains(MyCodeGeneratorOne.class.getSimpleName()))
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

        @Override
        protected void generate(JType applyToNode, Configuration config) {
            nodesInvoked.add(applyToNode);
        }
        
        @Override
		protected GenerateOne getAnnotation() {
			return Defaults.class.getAnnotation(GenerateOne.class);
		}
		
        @DontGenerate
		@GenerateOne(foo="__default")
		private static class Defaults {}
    }
    
    
    @Test
    public void generatorTemplateInvoked() throws Exception {
        String pkg = GeneratorRunnerTest.class.getPackage().getName();
        File generateTo =  new MavenProjectLayout().newTmpSubDir("GenRoot");

        MyCodeGeneratorTwo.nodesInvoked.clear();
        GeneratorRunner runner = GeneratorRunner.with()
                .defaults()
                .scanRoots(Roots.with().srcDirsOnly())
                .scanPackages(pkg)
                .failOnParseError(true)
                .matchGenerator(AString.contains("MyCodeGeneratorTwo"))
                .defaultGenerateTo(new DirectoryRoot(generateTo,RootType.GENERATED,RootContentType.SRC))
                .build();
        
        runner.run();
        
        Expect
    		.with().debugEnabled(true)
        	.that(MyCodeGeneratorTwo.nodesInvoked)
        	.is(AList.withOnly(AJType.with().fullName(BeanTwo.class)));
    }
   
    @GenerateMyTemplate
    public static class BeanTwo {
    	
    }
    
    public static class MyCodeGeneratorTwo extends AbstractCodeGenerator<GenerateTwo> {
        
        public static final List<JType> nodesInvoked = new ArrayList<>();

        @Override
        protected void generate(JType applyToNode, Configuration config) {
            nodesInvoked.add(applyToNode);
        }
        
        @Override
		protected GenerateTwo getAnnotation() {
			return Defaults.class.getAnnotation(GenerateTwo.class);
		}
		
        @DontGenerate
        @GenerateTwo(foo="__default")
		private static class Defaults {}
    }
}
