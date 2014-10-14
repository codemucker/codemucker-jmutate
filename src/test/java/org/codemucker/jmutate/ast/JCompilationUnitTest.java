package org.codemucker.jmutate.ast;

import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.junit.Assert;
import org.junit.Test;

public class JCompilationUnitTest {

	JMutateContext ctxt = DefaultMutateContext.with().defaults().build();
	
	@Test
	public void getPackage(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("package my.pkg;");
		t.pl("class MyClass {}");
		
		JSourceFile f = t.asResolvedSourceFileNamed("my.pkg.MyClass");
		Assert.assertEquals("my.pkg",f.getCompilationUnit().getFullPackageName());
		
		
		
	}
}
