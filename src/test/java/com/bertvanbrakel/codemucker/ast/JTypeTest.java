package com.bertvanbrakel.codemucker.ast;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.matcher.AField;
import com.bertvanbrakel.codemucker.ast.matcher.AMethod;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.lang.matcher.IsCollectionOf;
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
	
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("methodA","methodB"));		
		
		MatcherAssert.assertThat(foundMethods.toList(), matcher);
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
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("getA","getB"));		
		
		MatcherAssert.assertThat(foundMethods.toList(), matcher);
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
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("getA"));		
		
		MatcherAssert.assertThat(foundMethods.toList(), matcher);
	}

	@Test
	//For a bug where performing a method search on top level children also return nested methods
	public void testFindJavaMethodsExcludingAnonymousTypeMethodsEmbeddedInMethods(){
		SourceTemplate t = ctxt.newSourceTemplate();

		t.pl("class MyTestClass{");
		t.pl("	public Foo foo = new Foo() { //should be ignored" );
		t.pl("		public void getC(){ return 1; }" );
		t.pl("	};" );
		t.pl("	public void getA(){ return null; }" );
		t.pl("	public Object getB(){" );
		t.pl("		return new Foo(){" );
		t.pl("			public int getC(){ //should be ignored" );
		t.pl("				return new Foo(){" );
		t.pl("					public int getC(){ return 2;}//should be ignored" );
		t.pl("				};" );
		t.pl("			};" );
		t.pl("		};" );
		t.pl("	}" );
		t.pl("	private class Foo {" );
		t.pl("		public int getC(){ return 0;}//should be ignored" );
		t.pl("	}");
		t.pl("}");
		
		FindResult<JMethod> foundMethods = t.asJType().findMethodsMatching(AMethod.withMethodNamed("get*"));
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("getA","getB"));		
		
		MatcherAssert.assertThat(foundMethods.toList(), matcher);
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
	public void testImplementsClass(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("extends", MyExtendedClass.class.getName());
		t.pl("class MyClass {");
		t.pl("	class MyChildClass1 {}");
		t.pl("	class MyChildClass2 extends ${extends} {}");
		t.pl("	class MyChildClass3 extends MyChildClass2 {}");
		t.pl("}");
		
		JType type = t.asJType();

		assertFalse(type.getChildTypeWithName("MyChildClass1").isImplementing(MyExtendedClass.class));
		assertTrue(type.getChildTypeWithName("MyChildClass2").isImplementing(MyExtendedClass.class));
		assertTrue(type.getChildTypeWithName("MyChildClass3").isImplementing(MyExtendedClass.class));
	}
	
	public static class MyExtendedClass {
		
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
		
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("MyTestClass","MyTestClass"));		
		
		MatcherAssert.assertThat(foundMethods.toList(), matcher);
	}
	
	private List<Matcher<JMethod>> equalsMethodNames(String... methodNames){
		List<Matcher<JMethod>> matchers = newArrayList();
		for( String methodName:methodNames){
			matchers.add(equalsMethodName(methodName));
		}
		return matchers;
	}
	
	private Matcher<JMethod> equalsMethodName(final String methodName){
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
