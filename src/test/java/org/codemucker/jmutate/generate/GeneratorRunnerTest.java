package org.codemucker.jmutate.generate;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.DefaultGenerator;
import org.codemucker.jtest.MavenProjectLayout;
import org.junit.Assert;
import org.junit.Test;


public class GeneratorRunnerTest {

    @Test
    //@Ignore("need to fix to extract compiled annotation and auto detect generation annotations")
    public void smokeTest() throws Exception {
        String pkg = GeneratorRunnerTest.class.getPackage().getName();
        File generateTo =  new MavenProjectLayout().newTmpSubDir("GenRoot");
        
        Logger logger = org.apache.log4j.LogManager.getLogger(GeneratorRunner.class.getPackage().getName());
        logger.setLevel(Level.DEBUG);
        
        GeneratorRunner runner = GeneratorRunner.with()
                .defaults()
                .scanRoots(Roots.with().srcDirsOnly())
                .scanPackages(pkg)
                .failOnParseError(true)
                .defaultGenerateTo(new DirectoryRoot(generateTo,RootType.GENERATED,RootContentType.SRC))
                .build();
        
        runner.run();
        
        Assert.assertEquals(1,MyCodeGenerator.nodesInvoked.size());
        
    }
    
    @GenerateMyStuff(foo="myfoo")
    public static class ICauseGeneratorToBeInvoked {
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @DefaultGenerator("org.codemucker.jmutate.generate.GeneratorRunnerTest.MyCodeGenerator")
    public static @interface GenerateMyStuff {
        String foo();
        String bar() default "someDefault";
    }
    
    public static class MyCodeGenerator extends AbstractGenerator<GenerateMyStuff> {
        
        public static final List<JType> nodesInvoked = new ArrayList<>();

        @Override
        protected void generate(JType applyToNode, GenerateMyStuff options) {
            nodesInvoked.add(applyToNode);
        }
    }
}
