package com.bertvanbrakel.codemucker.ast;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Assert;
import org.junit.Test;

import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;

public class NodeCollectorTest {

	MutationContext ctxt = new SimpleMutationContext();
	
	@Test
	public void testCollectMethod() {
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MyClass {");
		t.pl("void myMethod(){}");
		t.pl("}");
		
		JType type = t.asJType();
	
		NodeCollector col = NodeCollector.builder()
			.collectType(MethodDeclaration.class)
			.ignoreChildTypes()
			.build();
		
		type.getAstNode().accept(col);
		List<MethodDeclaration> methods = col.getCollectedAs();
		
		Assert.assertEquals(1, methods.size());
		Assert.assertEquals("myMethod", methods.get(0).getName().toString());
	}
	
	@Test
	public void testCollectMethodIgnoreChildType() {
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MyClass {");
		t.pl("void myMethod1(){}");
		t.pl("class MyChildClass {");
		t.pl("void myMethod2(){}");
		t.pl("}");
		t.pl("}");
		
		JType type = t.asJType();
	
		NodeCollector col = NodeCollector.builder()
			.collectType(MethodDeclaration.class)
			.ignoreChildTypes()
			.build();
		
		type.getAstNode().accept(col);
		
		List<MethodDeclaration> methods = col.getCollectedAs();
		
		Assert.assertEquals(1, methods.size());
		Assert.assertEquals("myMethod1", methods.get(0).getName().toString());
	}
}
