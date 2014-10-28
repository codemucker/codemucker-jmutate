package org.codemucker.jmutate.generate;

import java.io.File;

import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.generate.GeneratorRunner;
import org.codemucker.jpattern.cqrs.GenerateCqrsRestServiceClient;
import org.codemucker.jtest.MavenLayoutProjectResolver;
import org.codemucker.jtest.ProjectResolver;
import org.junit.Test;

public class GeneratorRunnerTest {

    @Test
    public void testCanCompileSource() {
        ProjectResolver resolver = new MavenLayoutProjectResolver();
        
        DefaultMutateContext ctxt = DefaultMutateContext.with().defaults().build();
        
        GeneratorRunner.with()
            .defaults()
            .sources(Roots.with().allSrcDirs().classpath(true).build())
            .defaultGenerateTo(new DirectoryRoot(new File(resolver.getBaseDir(), "src/generated/java"))).scanPackages(GeneratorRunnerTest.class.getPackage().getName())
            .build()
            .run();

    }

    @GenerateCqrsRestServiceClient(requestBeanPackages = { "org.codemucker.jmutate.generate" }, requestBeanNames = { "*Cmd", "*Query" }, generateInterface = true)
    public static class MyService {
        
    }

    public static class MyCmd {
        private String param1;
        public String param2;

        public String getParam1() {
            return param1;
        }
    }

    public static class MyQuery {
        private String param1;
    }

}
