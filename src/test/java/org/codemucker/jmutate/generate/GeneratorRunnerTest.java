package org.codemucker.jmutate.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jmatch.AString;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jtest.MavenProjectLayout;
import org.junit.Assert;
import org.junit.Test;

public class GeneratorRunnerTest {

    @Test
    public void generatorInvoked() throws Exception {
        String pkg = GeneratorRunnerTest.class.getPackage().getName();
        File generateTo =  new MavenProjectLayout().newTmpSubDir("GenRoot");
        
//        Logger logger = org.apache.log4j.LogManager.getLogger(GeneratorRunner.class.getPackage().getName());
//        logger.setLevel(Level.DEBUG);
//        
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
        
        Assert.assertEquals(1,MyCodeGeneratorOne.nodesInvoked.size());
        Assert.assertEquals(NameUtil.compiledNameToSourceName(BeanOne.class.getName()),MyCodeGeneratorOne.nodesInvoked.get(0).getFullName());
    }
    
    @GenerateOne(foo="myfoo",ensureWeImportTypesWhenCompilingAnnon=Access.PRIVATE)
    public static class BeanOne {
    }
    
    public static class MyCodeGeneratorOne extends AbstractCodeGenerator<GenerateOne> {
        
        public static final List<JType> nodesInvoked = new ArrayList<>();

        @Override
        protected void generate(JType applyToNode, GeneratorConfig options) {
            nodesInvoked.add(applyToNode);
        }
    }
    
    
    @Test
    public void generatorTemplateInvoked() throws Exception {
        String pkg = GeneratorRunnerTest.class.getPackage().getName();
        File generateTo =  new MavenProjectLayout().newTmpSubDir("GenRoot");
        
//        Logger logger = org.apache.log4j.LogManager.getLogger(GeneratorRunner.class.getPackage().getName());
//        logger.setLevel(Level.DEBUG);
//        
        MyCodeGeneratorTwo.nodesInvoked.clear();
        GeneratorRunner runner = GeneratorRunner.with()
                .defaults()
                .scanRoots(Roots.with().srcDirsOnly())
                .scanPackages(pkg)
                .failOnParseError(true)
                .matchGenerator(AString.contains("MyCodeGeneratorTwo"))
               // .matchAnnotations(AString.matchingExpression("(!*IsGeneratorTemplate)"))
                .defaultGenerateTo(new DirectoryRoot(generateTo,RootType.GENERATED,RootContentType.SRC))
                .build();
        
        runner.run();
        
        Assert.assertEquals(1,MyCodeGeneratorTwo.nodesInvoked.size());
        Assert.assertEquals(NameUtil.compiledNameToSourceName(BeanTwo.class.getName()),MyCodeGeneratorTwo.nodesInvoked.get(0).getFullName());
    }
    
    
    @GenerateMyTemplate
    public static class BeanTwo {
    	
    }
    
    public static class MyCodeGeneratorTwo extends AbstractCodeGenerator<GenerateTwo> {
        
        public static final List<JType> nodesInvoked = new ArrayList<>();

        @Override
        protected void generate(JType applyToNode, GeneratorConfig options) {
            nodesInvoked.add(applyToNode);
        }
    }
}
