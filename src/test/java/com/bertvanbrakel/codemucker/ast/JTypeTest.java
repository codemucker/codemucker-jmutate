package com.bertvanbrakel.codemucker.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;

import com.bertvanbrakel.codemucker.SourceHelper;
import com.bertvanbrakel.codemucker.ast.JTypeTest.MyClass.MyChildClass1;
import com.bertvanbrakel.codemucker.ast.JTypeTest.MyClass.MyChildClass2;
import com.bertvanbrakel.codemucker.ast.JTypeTest.MyClass.MyChildClass3;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.matcher.AField;
import com.bertvanbrakel.codemucker.ast.matcher.AMethod;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.lang.matcher.AList;

public class JTypeTest {

	MutationContext ctxt = new SimpleMutationContext();
	
	@Test
	public void testIsAbstract() {
		assertEquals(false, newJType("class MyClass{}").getModifiers().isAbstract());
		assertEquals(false, newJType("interface MyInterface{}").getModifiers().isAbstract());
		assertEquals(false, newJType("enum MyEnum{}").getModifiers().isAbstract());

		assertEquals(true, newJType("abstract class MyAbstractClass{}").getModifiers().isAbstract());
	}
	
	@Test
	public void testIsInterface() {
		assertEquals(false, newJType("class MyClass{}").isInterface());
		assertEquals(false, newJType("enum MyEnum{}").isInterface());	
	
		assertEquals(true, newJType("interface MyInterface{}").isInterface());
	}
	
	@Test
	public void testIsConcreteClass() {
		assertEquals(true, newJType("class MyClass{}").isConcreteClass());
		
		assertEquals(false, newJType("interface MyInterface{}").isConcreteClass());
		assertEquals(false, newJType("enum MyEnum{}").isConcreteClass());
	}
	
	private JType newJType(String src){
		return ctxt.newSourceTemplate().println(src).asJType();
	}

	@Test
	public void testIsTopLevelClass() {
		assertEquals(true, newJType("class MyTopClass {}").isTopLevelClass());
		assertEquals(true, newJType("class AnotherTopClass { class MyInnerClass{} }").isTopLevelClass());
		
		assertEquals(false, newJType("class AnotherTopClass { class MyInnerClass{} }").getChildTypeWithName("MyInnerClass").isTopLevelClass());
	}
	
	@Test
	public void testFindJavaMethods(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.pl("class MyTestClass  {");
		t.pl( "public void methodA(){}" );
		t.pl( "public void methodB(){}" );
		t.pl("}");
	
		FindResult<JMethod> foundMethods = t.asJType().findAllJMethods();

		MatcherAssert.assertThat(
				foundMethods.toList(), 
				AList.of(JMethod.class)
					.inOrder()
					.containingOnly()
					.item(methodWithName("methodA"))
					.item(methodWithName("methodB"))
		);
	}

	@Test
	public void testFindJavaMethodsWithFilter(){
		SourceTemplate t = ctxt.newSourceTemplate();

		t.pl("class MyTestClass  {");
		t.pl( "public void getA(){}" );
		t.pl( "public void setB(){}" );
		t.pl( "public void getB(){}" );
		t.pl( "public void setA(){}" );
		t.pl("}");

		FindResult<JMethod> foundMethods = t.asJType().findMethodsMatching(AMethod.withMethodNamed("get*"));
	
		MatcherAssert.assertThat(
				foundMethods.toList(), 
				AList.of(JMethod.class)
					.inOrder()
					.containingOnly()
					.item(methodWithName("getA"))
					.item(methodWithName("getB"))
		);
	}

	@Test
	public void testFindJavaMethodsExcludingChildTypeMethods(){
		SourceTemplate t = ctxt.newSourceTemplate();

		t.pl("class MyTestClass{");
		t.pl("	public void getA(){ return null; }" );
		t.pl("	private class Foo {" );
		t.pl("		public void getB(){ return null;}//should be ignored" );
		t.pl("	}");
		t.pl("}");
		
		FindResult<JMethod> foundMethods = t.asJType().findMethodsMatching(AMethod.withMethodNamed("get*"));

		MatcherAssert.assertThat(
				foundMethods.toList(), 
				AList.ofOnly(methodWithName("getA"))
		);
	}

	@Test
	//For a bug where performing a method search on top level children also return nested methods
	public void testFindJavaMethodsExcludingAnonymousTypeMethodsEmbeddedInMethods(){
		JType type = SourceHelper.findSourceForTestClass(getClass()).getTypeWithName(MyTestClass.class);
		FindResult<JMethod> foundMethods = type.findMethodsMatching(AMethod.withMethodNamed("get*"));

		MatcherAssert.assertThat(
			foundMethods.toList(), 
			AList.of(JMethod.class)
				.inOrder()
				.containingOnly()
				.item(methodWithName("getA"))
				.item(methodWithName("getB"))
		);
	}
	
	static class MyTestClass {
		public Foo foo = new Foo() { // should be ignored
			public int getC() {
				return 1;
			}
		};

		public Object getA() {
			return null;
		}

		public Object getB() {
			return new Foo() {
				public int getC() { // should be ignored
					return new Foo() {
						public int getC() {
							return 2;
						}// should be ignored
					}.getC();
				};
			};
		}

		private class Foo {
			public int getC() {
				return 0;
			}// should be ignored
		}
	}
	@Test
	public void testResolveFullNameAnonymousInnerClass(){
		SourceTemplate t = ctxt.newSourceTemplate();

		t.pl("class MyTestClass  {");
		t.pl( "public void getA(){ return null; }" );
		t.pl( "private Foo foo = new Foo(){};//should resolve to Foo" );
		t.pl( "private class Foo {" );
		t.pl( "		public void getB(){ return null;}" );
		t.pl("	}");
		t.pl("}");
		
		JField field = t.asJType().findFieldsMatching(AField.withName("foo")).getFirst();
		
		Assert.assertEquals("Foo",field.getTypeSignature());
	}
	
	@Test
	public void testIsAnonymousClass(){
		SourceTemplate t = ctxt.newSourceTemplate();

		t.pl("class MyTestClass  {");
		t.pl( "private Foo foo = new Foo(){};//anonymous" );
		t.pl( "private class Foo {" );
		t.pl("	}");
		t.pl("}");
		
		List<JType> found = t.asJType().findAllChildTypes().toList();

		Assert.assertEquals(2, found.size());
		Assert.assertTrue(found.get(0).isAnonymousClass());
		Assert.assertFalse(found.get(1).isAnonymousClass());
	}
	
	@Test
	public void testGetFullName(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.pl("package foo.bar;");
		
		t.pl("import SamePackage;");
		t.pl("import one.OnePackage;");
		t.pl("import one.two.TwoPackages;");
		t.pl("import one.two.WithInnerClass;");
		
		t.pl("class MyTestClass  {");
		t.pl( "public SamePackage getA(){return null;}" );
		t.pl( "public TwoPackages getB(){return null;}" );
		t.pl( "public WithInnerClass.Innerclass getC(){return null;}" );
		t.pl("}");
	
		JType type = t.asSourceFileWithFullName("MyTestClass").getMainType();

		assertEquals("foo.bar.MyTestClass", type.getFullName());
	}

	@Test
	public void testHasNotAnnotationOfType(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MyClass  {}");
	
		Assert.assertFalse(t.asJType().hasAnnotationOfType(MyAnnotation.class));
	}
	
	@Test
	public void testHasAnnotationOfType(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("@" + MyAnnotation.class.getName());
		t.pl("class MyClass {}");
		
		Assert.assertTrue(t.asJType().hasAnnotationOfType(MyAnnotation.class));
	}
	
	@interface MyAnnotation {
		
	}
	
	@Test
	public void testImplementsClass(){
		JSourceFile source = SourceHelper.findSourceForTestClass(getClass());
		assertFalse(source.getTypeWithName(MyChildClass1.class).isImplementing(MyExtendedClass.class));
		assertTrue(source.getTypeWithName(MyChildClass2.class).isImplementing(MyExtendedClass.class));
		assertTrue(source.getTypeWithName(MyChildClass3.class).isImplementing(MyExtendedClass.class));
	}
	
	public static class MyExtendedClass {
	}
	
	class MyClass {
		class MyChildClass1 {}
		class MyChildClass2 extends MyExtendedClass {}
		class MyChildClass3 extends MyChildClass2 {}
	}

	@Test
	public void testFindJavaConstructors(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.pl("class MyTestClass { ");
		t.pl( "MyTestClass(){}" );
		t.pl( "MyTestClass(String foo){}" );
		t.pl( "public void someMethod(){}" );
		t.pl("}");

		FindResult<JMethod> foundMethods = t.asJType().findMethodsMatching(AMethod.isConstructor());

		MatcherAssert.assertThat(
				foundMethods.toList(), 
				AList.of(JMethod.class)
					.inOrder()
					.containingOnly()
					.item(methodWithName("MyTestClass"))
					.item(methodWithName("MyTestClass"))
		);
	}
	
	private Matcher<JMethod> methodWithName(final String methodName){
		return new TypeSafeMatcher<JMethod>(JMethod.class) {
			@Override
            public void describeTo(Description desc) {
				desc.appendText("method name '" + methodName + "'");
            }

			@Override
            public boolean matchesSafely(JMethod method) {
				return method.getAstNode().getName().getIdentifier().equals(methodName);
			}	
		};		
	}
}
