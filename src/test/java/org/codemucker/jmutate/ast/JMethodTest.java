package org.codemucker.jmutate.ast;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.SimpleCodeMuckContext;
import org.codemucker.jmutate.transform.SourceTemplate;
import org.junit.Assert;
import org.junit.Test;


public class JMethodTest {

	SimpleCodeMuckContext ctxt = new SimpleCodeMuckContext();
	
	@Test
	public void test_clash_signature_generations_resolved(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("package mypackage.path;");
		t.pl("import java.util.Collection;");
		t.pl("class MyType {");
		t.pl("public void myMethod(String bar,Collection<String> col){}");
		t.pl("public void myMethod(String[][] bar,Collection<String> col, int foo){}");
		t.pl("}");
		
		List<JMethod> methods = t.asResolvedSourceFileNamed("mypackage.path.MyType").getMainType().findAllJMethods().toList();
		
		JMethod method1 = methods.get(0);
		JMethod method2 = methods.get(1);
		
		assertEquals("myMethod(java.lang.String,java.util.Collection)", method1.getClashDetectionSignature());
		assertEquals("myMethod(java.lang.String[][],java.util.Collection,int)", method2.getClashDetectionSignature());
	}
	
	@Test
	public void test_clash_signature_generation_unresolved(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("package mypackage.path;");
		t.pl("import java.util.Collection;");
		t.pl("class MyType {");
		t.pl("public void myMethod(String bar,Collection<String> col){}");
		t.pl("public void myMethod(String[][] bar,Collection<String> col, int foo){}");
		t.pl("}");
		
		List<JMethod> methods = t.asSourceFileSnippet().getMainType().findAllJMethods().toList();
		
		JMethod method1 = methods.get(0);
		JMethod method2 = methods.get(1);
		
		assertEquals("myMethod(java.lang.String,java.util.Collection)", method1.getClashDetectionSignature());
		assertEquals("myMethod(java.lang.String[][],java.util.Collection,int)", method2.getClashDetectionSignature());
	}
	
	@Test
	public void test_clash_signature_generation_methodSnippet(){
		JMethod method1 = ctxt.newSourceTemplate().pl("public void myMethod(String bar,Collection<String> col){}").asJMethodSnippet();
		JMethod method2 = ctxt.newSourceTemplate().pl("public void myMethod(String bar,java.util.Collection<String> col){}").asJMethodSnippet();
		JMethod method3 = ctxt.newSourceTemplate().pl("public void myMethod(String[][] bar,Collection<String> col, int foo){}").asJMethodSnippet();
		
		assertEquals("myMethod(java.lang.String,Collection)", method1.getClashDetectionSignature());
		assertEquals("myMethod(java.lang.String,java.util.Collection)", method2.getClashDetectionSignature());
		assertEquals("myMethod(java.lang.String[][],Collection,int)", method3.getClashDetectionSignature());
	}

	@Test
	//bug where interfaces declared on types are not being resolved correctly
	public void test_clash_signature_generations_bug(){
		SourceTemplate t = ctxt.newSourceTemplate();
		String pkg =  JMethod.class.getPackage().getName();
		
		t.v("pkg", pkg);
		t.pl("package ${pkg};");
		t.pl("class MyType {");
		t.pl("public interface MyInterface {}");
		t.pl("public void myMethod(MyInterface myArg){}");
		t.pl("}");
		
		JMethod method = t.asResolvedSourceFileNamed("${pkg}.MyType").getMainType().findAllJMethods().getFirst();
		
		assertEquals("myMethod(" + pkg + ".MyType.MyInterface)", method.getClashDetectionSignature());
	}
	
	public static interface MyInterface{};
	
	@Test
	public void testIsVoid(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("public void myMethod(){}");
		JMethod method = t.asResolvedJMethod();
		Assert.assertTrue(method.isVoid());		
	}
	
	@Test
	public void testIsNotVoid(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("public String myMethod(){ return \"foo\";}");
		JMethod method = t.asResolvedJMethod();
		Assert.assertFalse(method.isVoid());
	}
	
	@Test
	public void testIsConstructor(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MyType { MyType(){}}");
		JMethod method = t.asResolvedJTypeNamed("MyType").findAllJMethods().getFirst();
		Assert.assertTrue(method.isConstructor());
	}
	
	@Test
	public void testIsNotConstructor(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MyType {");
		t.pl("void MyMethod(){}");
		t.pl("}");
		
		JMethod method = t.asResolvedJTypeNamed("MyType").findAllJMethods().getFirst();
		Assert.assertFalse(method.isConstructor());
	}
	
}
