package com.bertvanbrakel.codemucker.ast;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatchers;
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
		
		assertEquals(false, newJType("class AnotherTopClass { class MyInnerClass{} }").getTypeWithName("MyInnerClass").isTopLevelClass());
	}
	
	@Test
	public void testFindJavaMethods(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.println("class MyTestClass  {");
		t.println( "public void methodA(){}" );
		t.println( "public void methodB(){}" );
		t.println("}");
	
		FindResult<JMethod> foundMethods = t.asJType().findAllJMethods();
	
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("methodA","methodB"));		
		
		MatcherAssert.assertThat(foundMethods.toList(), matcher);
	}

	@Test
	public void testFindJavaMethodsWithFilter(){
		SourceTemplate t = ctxt.newSourceTemplate();

		t.println("class MyTestClass  {");
		t.println( "public void getA(){}" );
		t.println( "public void setB(){}" );
		t.println( "public void getB(){}" );
		t.println( "public void setA(){}" );
		t.println("}");

		FindResult<JMethod> foundMethods = t.asJType().findMethodsMatching(JMethodMatchers.withMethodNamed("get*"));
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("getA","getB"));		
		
		MatcherAssert.assertThat(foundMethods.toList(), matcher);
	}
	

	@Test
	public void testGetFullName(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.println("package foo.bar;");
		
		t.println("import SamePackage;");
		t.println("import one.OnePackage;");
		t.println("import one.two.TwoPackages;");
		t.println("import one.two.WithInnerClass;");
		
		t.println("class MyTestClass  {");
		t.println( "public SamePackage getA(){return null;}" );
		t.println( "public TwoPackages getB(){return null;}" );
		t.println( "public WithInnerClass.Innerclass getC(){return null;}" );
		t.println("}");
	
		JType type = t.asSourceFileWithFQN("MyTestClass").getMainType();

		assertEquals("foo.bar.MyTestClass", type.getFullName());
	}
	
	
	@Test
	public void testFindJavaConstructors(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.println("class MyTestClass { ");
		t.println( "MyTestClass(){}" );
		t.println( "MyTestClass(String foo){}" );
		t.println( "public void someMethod(){}" );
		t.println("}");

		FindResult<JMethod> foundMethods = t.asJType().findMethodsMatching(JMethodMatchers.isConstructor());
		
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
