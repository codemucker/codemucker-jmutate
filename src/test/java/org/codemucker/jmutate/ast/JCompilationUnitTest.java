package org.codemucker.jmutate.ast;

import org.codemucker.jmutate.transform.MutateContext;
import org.codemucker.jmutate.transform.SourceTemplate;
import org.junit.Assert;
import org.junit.Test;

public class JCompilationUnitTest {

	MutateContext ctxt = new SimpleMutateContext();
	
	@Test
	public void getPackage(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("package my.pkg;");
		t.pl("class MyClass {}");
		
		JSourceFile f = t.asResolvedSourceFileNamed("my.pkg.MyClass");
		Assert.assertEquals("my.pkg",f.getCompilationUnit().getFullPackageName());
		
		
		
	}
}
