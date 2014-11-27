package org.codemucker.jmutate;

import org.codemucker.jfind.matcher.AClass;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.ast.JSourceFile;
import org.junit.Test;

public class DefaultMutationContextTest {

    @Test
    public void testCanCompileSource() {
        DefaultMutateContext ctxt = DefaultMutateContext.with().defaults().build();

        SourceTemplate t = ctxt.newSourceTemplate();
        t.pl("package com.mycompany;");
        t.pl("public class FooBar {");
        t.pl("  private String myField;");
        t.pl("  public String getMyField(){ return myField; }");
        t.pl("  public void setMyField(String v){  this.myField = v; }");
        t.pl("}");

        JSourceFile source = t.asSourceFileSnippet().asMutator(ctxt).writeModificationsToDisk();

        Class<?> sourceClass = ctxt.getCompiler().toCompiledClass(source);
        
        Expect.that(sourceClass).is(AClass.that().isPublic().name("com.mycompany.FooBar"));
        
    }
}
