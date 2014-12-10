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
    public void testCanCompileSource() {
        JMutateContext ctxt = DefaultMutateContext.with().defaults().build();

        SourceTemplate t = ctxt.newSourceTemplate();
        t.pl("package com.mycompany;");
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
        Class<?> sourceClass = compiler.toCompiledClass(source.getResource());
        assertCorrectCompile(sourceClass);   
        
        SystemCompiler compilerEclipse = new SystemCompiler(resourceLoader, new DefaultProjectOptions());
        Class<?> sourceClassEclipe = compilerEclipse.toCompiledClass(source.getResource());
        assertCorrectCompile(sourceClassEclipe);   
        
        
    }



    private void assertCorrectCompile(Class<?> sourceClass) {
        Expect
            .that(sourceClass)
            .is(AClass.with()
                .fullName("com.mycompany.FooBar")
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
