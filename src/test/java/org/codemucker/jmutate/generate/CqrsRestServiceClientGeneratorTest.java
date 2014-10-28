package org.codemucker.jmutate.generate;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jpattern.cqrs.GenerateCqrsRestServiceClient;
import org.codemucker.jtest.MavenLayoutProjectResolver;
import org.codemucker.jtest.ProjectResolver;
import org.junit.Test;

public class CqrsRestServiceClientGeneratorTest {

    @Test
    public void test(){
        String pkg = CqrsRestServiceClientGeneratorTest.class.getPackage().getName();
        ProjectResolver project = new MavenLayoutProjectResolver();
        File generateTo = project.newTmpSubDir();
        
        GeneratorRunner runner = GeneratorRunner.with()
                .defaults()
                .sources(Roots.with().allDirs().build())
                .scanRootMatching(ARoot.with().javaPackage(pkg))
                .scanPackages(pkg)
                .failOnParseError(true)
                .defaultGenerateTo(new DirectoryRoot(generateTo,RootType.GENERATED,RootContentType.SRC))
                .build();
        
        runner.run();;
    }
    
    @GenerateCqrsRestServiceClient()
    public static class ICauseGeneratorToBeInvoked {
        
    }
    
    
}
