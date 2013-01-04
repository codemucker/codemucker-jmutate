package com.bertvanbrakel.codemucker.ast;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bertvanbrakel.codemucker.transform.SourceTemplate;

public class JMethodTest {

	SimpleMutationContext ctxt = new SimpleMutationContext();
	
	@Test
	public void test_clash_signature_generations(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("package mypackage.path;");
		t.pl("import java.util.Collection;");
		t.pl("class MyType {");
		t.pl("public void myMethod(String bar,Collection<String> col){}");
		t.pl("public void myMethod(String[][] bar,Collection<String> col, int foo){}");
		t.pl("}");
		
		List<JMethod> methods = t.asSourceFile().getMainType().findAllJMethods().toList();
		
		JMethod method1 = methods.get(0);
		JMethod method2 = methods.get(1);
		
		assertEquals("myMethod(java.lang.String,java.util.Collection)", method1.toClashDetectionSignature());
		assertEquals("myMethod(java.lang.String[][],java.util.Collection,int)", method2.toClashDetectionSignature());
	}

	@Test
	//bug where interfaces declared on types are not being resolved correctly
	public void test_clash_signature_generations_bug(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("pkg", JMethod.class.getPackage().getName());
		t.pl("package ${pkg};");
		t.pl("class MyType {");
		//t.pl("public interface MyInterface {}");
		t.pl("public void myMethod(MyInterface myArg){}");
		t.pl("}");
		
		JMethod method = t.asSourceFile().getMainType().findAllJMethods().getFirst();
		
		assertEquals("myMethod(MyInterface)", method.toClashDetectionSignature());
	}
	
	public static interface MyInterface{};
	
	@Test
	public void testIsVoid(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("public void myMethod(){}");
		JMethod method = t.asJMethod();
		Assert.assertTrue(method.isVoid());		
	}
	
	@Test
	public void testIsNotVoid(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("public String myMethod(){ return \"foo\";}");
		JMethod method = t.asJMethod();
		Assert.assertFalse(method.isVoid());
	}
	
	@Test
	public void testIsConstructor(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MyType { MyType(){}}");
		JMethod method = t.asJType().findAllJMethods().getFirst();
		Assert.assertTrue(method.isConstructor());
	}
	
	@Test
	public void testIsNotConstructor(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MyType {");
		t.pl("void MyMethod(){}");
		t.pl("}");
		
		JMethod method = t.asJType().findAllJMethods().getFirst();
		Assert.assertFalse(method.isConstructor());
	}
	
}
