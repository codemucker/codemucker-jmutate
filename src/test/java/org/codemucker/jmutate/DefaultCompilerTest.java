package org.codemucker.jmutate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codemucker.jfind.matcher.AClass;
import org.codemucker.jfind.matcher.AField;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.ast.JSourceFile;
import org.junit.Test;

public class DefaultCompilerTest {

    @Test
    public void testCanCompileSourceUsingEclipseAndSystemCompiler() {
        JMutateContext ctxt = DefaultMutateContext.with().defaults().build();

        //use a changing package name to avoid other test classes (like import cleaner) finding this class on the class path
        String packageName = "com.mycompany.x" + System.currentTimeMillis() + "x";
        
        SourceTemplate t = ctxt.newSourceTemplate();
        t.var("pkg",packageName);
        t.pl("package ${pkg};");
        t.p("@").p(MyAnnotation.class).pl("(foo=\"myvalue\")" );
        t.pl("public class FooBar {");
        t.pl("  private String myField;");
        t.pl("  public String myField2;");
        t.pl("  public String getMyField(){ return myField; }");
        t.pl("  public void setMyField(String v){  this.myField = v; }");
        t.pl("}");

        JSourceFile source = t.asSourceFileSnippet().asMutator(ctxt).writeModificationsToDisk();

        ResourceLoader resourceLoader = ctxt.getParser().getResourceLoader();
        
        SystemCompiler compiler = new SystemCompiler(resourceLoader, new DefaultProjectOptions());
        Class<?> sourceClassSystem = compiler.toCompiledClass(source.getResource());
        assertCorrectCompile(sourceClassSystem, packageName);   
        
        SystemCompiler compilerEclipse = new SystemCompiler(resourceLoader, new DefaultProjectOptions());
        Class<?> sourceClassEclipe = compilerEclipse.toCompiledClass(source.getResource());
        assertCorrectCompile(sourceClassEclipe,packageName);   
        
    }



    private void assertCorrectCompile(Class<?> sourceClass,String pkgName) {
        Expect
            .that(sourceClass)
            .is(AClass.with()
                .fullNameAntPattern( pkgName + ".FooBar")
                .isPublic()
                .annotation(AnAnnotation.with().fullName(MyAnnotation.class))
                .field(AField.with().name("myField").isPrivate())
                .field(AField.with().name("myField2").isPublic()));
        
        MyAnnotation a = sourceClass.getAnnotation(MyAnnotation.class);
        Expect.that(a).isNotNull();
        Expect.that(a.foo()).isEqualTo("myvalue");
    }
    
    
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface MyAnnotation {
        String foo();
    }
}
