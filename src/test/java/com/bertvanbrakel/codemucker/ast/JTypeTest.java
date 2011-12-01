package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatcher;
import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.codemucker.util.SrcWriter;
import com.bertvanbrakel.lang.matcher.IsCollectionOf;
import com.bertvanbrakel.test.util.ClassNameUtil;
import com.bertvanbrakel.test.util.TestHelper;
public class JTypeTest {

	TestHelper helper = new TestHelper();

	@Test
	public void testIsAbstract() {
		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");

		w.println("class MyClass{}");
		w.println("abstract class MyAbstractClass{}");
		w.println("interface MyInterface{}");
		w.println("enum MyEnum{}");

		assertTrue(newJavaType(w, "foo.bar.Foo", "MyAbstractClass").getJavaModifiers().isAbstract());
		assertFalse(newJavaType(w, "foo.bar.Foo", "MyClass").getJavaModifiers().isAbstract());
		assertFalse(newJavaType(w, "foo.bar.Foo", "MyInterface").getJavaModifiers().isAbstract());
		assertFalse(newJavaType(w, "foo.bar.Foo", "MyEnum").getJavaModifiers().isAbstract());	
	}

	@Test
	public void testIsInterface() {
		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");

		w.println("class MyClass{}");
		w.println("interface MyInterface{}");
		w.println("enum MyEnum{}");

		assertFalse(newJavaType(w, "foo.bar.Foo", "MyClass").isInterface());
		assertTrue(newJavaType(w, "foo.bar.Foo", "MyInterface").isInterface());
		assertFalse(newJavaType(w, "foo.bar.Foo", "MyEnum").isInterface());		
	}
	
	@Test
	public void testIsConcreteClass() {
		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");

		w.println("class MyClass{}");
		w.println("interface MyInterface{}");
		w.println("enum MyEnum{}");

		assertTrue(newJavaType(w, "foo.bar.Foo", "MyClass").isConcreteClass());
		assertFalse(newJavaType(w, "foo.bar.Foo", "MyInterface").isConcreteClass());
		assertFalse(newJavaType(w, "foo.bar.Foo", "MyEnum").isConcreteClass());		
	}

	@Test
	public void testIsTopLevelClass() {
		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");
		w.println("class MyTopClass {}");
		w.println("class AnotherTopClass { class MyInnerClass{} }");

		assertTrue(newJavaType(w, "foo.bar.Foo", "MyTopClass").isTopLevelClass());

		JType type = newJavaType(w, "foo.bar.Foo", "AnotherTopClass");
		assertTrue(type.isTopLevelClass());
		assertFalse(type.getTypeWithName("MyInnerClass").isTopLevelClass());
	
	}
	
	@Test
	public void testFindJavaMethods(){

		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");
		w.println("class MyTestClass  {");
		w.println( "public void methodA(){}" );
		w.println( "public void methodB(){}" );
		w.println("}");
	
		JType type = newJavaType(w, "foo.bar.Foo", "MyTestClass");

		Collection<JMethod> foundMethods = type.getAllJavaMethods();
	
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("methodA","methodB"));		
		
		MatcherAssert.assertThat(foundMethods, matcher);
	}

	@Test
	public void testFindJavaMethodsWithFilter(){

		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");
		w.println("class MyTestClass  {");
		w.println( "public void getA(){}" );
		w.println( "public void setB(){}" );
		w.println( "public void getB(){}" );
		w.println( "public void setA(){}" );
		w.println("}");
	
		JType type = newJavaType(w, "foo.bar.Foo", "MyTestClass");

		Collection<JMethod> foundMethods = type.findMethodsMatching(new JMethodMatcher() {
			@Override
			public boolean matches(JMethod found) {
				return found.getName().startsWith("get");
			}
		});
	
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("getA","getB"));		
		
		MatcherAssert.assertThat(foundMethods, matcher);
	}
	

	@Test
	public void testFindJavaConstructors(){

		SrcWriter w = new SrcWriter();
		w.println("package foo.bar");
		w.println("class MyTestClass ");
		w.println( "MyTestClass(){}" );
		w.println( "MyTestClass(String foo){}" );
		w.println( "public void someMethod(){}" );
		w.println("}");
	
		JType type = newJavaType(w, "foo.bar.Foo", "MyTestClass");

		Collection<JMethod> foundMethods = type.findMethodsMatching(new JMethodMatcher() {
			@Override
			public boolean matches(JMethod found) {
				return found.isConstructor();
			}
		});
	
		Matcher<Iterable<JMethod>> matcher = IsCollectionOf.containsOnlyItemsInOrder(equalsMethodNames("MyTestClass","MyTestClass"));		
		
		MatcherAssert.assertThat(foundMethods, matcher);
	}
	
	private List<Matcher<JMethod>> equalsMethodNames(String... methodNames){
		List<Matcher<JMethod>> matchers = new ArrayList<Matcher<JMethod>>();
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
				return method.getMethodNode().getName().getIdentifier().equals(methodName);
			}	
		};		
	}

	public JType newJavaType(SrcWriter w, String fqClassName) {
		String simpleClassName = ClassNameUtil.extractShortClassNamePart(fqClassName);
		return newJavaType(w, fqClassName, simpleClassName);
	}

	public JType newJavaType(SrcWriter w, String fqClassName, String typeToGet) {
		JavaSourceFile srcFile = newJavaSrc(w, fqClassName);
		JType type = new JType(srcFile, srcFile.getTopTypeWithName(typeToGet));

		return type;
	}

	private JavaSourceFile newJavaSrc(SrcWriter writer, String fqClassName) {
		File classRootDir = helper.createTempDir();
		try {
			return SourceUtil.writeJavaSrc(writer, classRootDir, fqClassName);
		} catch (IOException e) {
			throw new CodemuckerException("Couldn't writre tmp java source file", e);
		}
	}
}
