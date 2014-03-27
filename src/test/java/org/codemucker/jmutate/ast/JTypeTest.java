package org.codemucker.jmutate.ast;

import static org.codemucker.jmatch.Assert.assertThat;
import static org.codemucker.jmatch.Assert.is;
import static org.codemucker.jmatch.Assert.isEqualTo;
import static org.codemucker.jmatch.Assert.isFalse;
import static org.codemucker.jmatch.Assert.isTrue;
import static org.codemucker.jmatch.Assert.not;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.codemucker.jfind.FindResult;
import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.SourceHelper;
import org.codemucker.jmutate.ast.JTypeTest.MyClass.MyChildClass1;
import org.codemucker.jmutate.ast.JTypeTest.MyClass.MyChildClass2;
import org.codemucker.jmutate.ast.JTypeTest.MyClass.MyChildClass3;
import org.codemucker.jmutate.ast.JTypeTest.MyClass.MyNonExtendingClass;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.transform.MutateContext;
import org.codemucker.jmutate.transform.SourceTemplate;
import org.junit.Test;

public class JTypeTest {

	MutateContext ctxt = new SimpleMutateContext();
	
	@Test
	public void testIsAbstract() {
		assertThat(newJType("class MyClass{}").getModifiers().isAbstract(),isFalse());
		assertThat(newJType("interface MyInterface{}").getModifiers().isAbstract(),isFalse());
		assertThat(newJType("enum MyEnum{}").getModifiers().isAbstract(),isFalse());

		assertThat(newJType("abstract class MyAbstractClass{}").getModifiers().isAbstract(),isTrue());
	}
	
	@Test
	public void testIsInterface() {
		assertThat(newJType("class MyClass{}").isInterface(),isFalse());
		assertThat(newJType("enum MyEnum{}").isInterface(),isFalse());	
	
		assertThat(newJType("interface MyInterface{}").isInterface(),isTrue());
	}
	
	@Test
	public void testIsConcreteClass() {
		assertThat(newJType("class MyClass{}").isConcreteClass(),isTrue());
		
		assertThat(newJType("interface MyInterface{}").isConcreteClass(),isFalse());
		assertThat(newJType("enum MyEnum{}").isConcreteClass(),isFalse());
	}
	
	private JType newJType(String src){
		return ctxt.newSourceTemplate().println(src).asResolvedJTypeNamed(null);
	}

	@Test
	public void testIsTopLevelClass() {
		assertThat(newJType("class MyTopClass {}").isTopLevelClass(),isTrue());
		assertThat(newJType("class AnotherTopClass { class MyInnerClass{} }").isTopLevelClass(),isTrue());
		
		assertThat(newJType("class AnotherTopClass { class MyInnerClass{} }").getDirectChildTypeWithName("MyInnerClass").isTopLevelClass(),isFalse());
	}
	
	
	@Test
	public void testIsSubclassOfConcreteClass(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MySubClass extends " + MyParentClass.class.getName() + "{}");
		JType type = t.asJTypeSnippet();
		
		Expect.that(type.isSubClassOf(MyParentClass.class)).is(true);
	}
	
	@Test
	public void testIsSubclassOfConcreteClassTwoLevelsUp(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("superClass", MyParentClass.class);
		//t.pl("package ${pkg};");
		
		t.pl("class MyClass {");
		t.pl("	class SubClass extends ${superClass} {}");
		t.pl("	class SubSubClass extends SubClass {}");
		t.pl("}");
		
		JType type = t.asJTypeSnippet().getDirectChildTypeWithName("SubSubClass");
		
		Expect.that(type.isSubClassOf(MyParentClass.class)).is(true);
	}
	
	@Test
	public void isSubClass_deepEmbeddedSubclassOfConcreteClassTwoLevelsUp(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("superClass", MyParentClass.class);
		//t.pl("package ${pkg};");
		
		t.pl("class MyClass {");
		t.pl("	class SubClass extends ${superClass} {}");
		t.pl("	class SomeClass1 {");
		t.pl("		class SomeClass2 {");
		t.pl("			class SubSubClass extends SubClass {}");
		t.pl("		}");
		t.pl("	}");
		t.pl("}");
		
		JType type = t.asJTypeSnippet().findChildTypesMatching(AJType.with().simpleName("SubSubClass")).getFirst();
		
		Expect.that(type.isSubClassOf(MyParentClass.class)).is(true);
	}
	
	@Test
	public void isSubClass_deepEmbeddedSubclassOfConcreteClassManyLevelsUp(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("superClass", MyParentClass.class);
		//t.pl("package ${pkg};");
		
		t.pl("class MyClass {");
		t.pl("	class SubClass extends ${superClass} {}");
		t.pl("	class SomeClass1 extends SubClass {");
		t.pl("		class SomeClass2 extends SomeClass1 {");
		t.pl("			class SubSubClass extends SomeClass2 {}");
		t.pl("		}");
		t.pl("	}");
		t.pl("}");
		
		JType type = t.asJTypeSnippet().findChildTypesMatching(AJType.with().simpleName("SubSubClass")).getFirst();
		
		Expect.that(type.isSubClassOf(MyParentClass.class)).is(true);
	}
	
	@Test
	public void isSubClass_isSubClassOfExternalClassNotDirectlyImported(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("superClass", MyParentClass.class);
		//t.pl("package ${pkg};");
		
		t.pl("class MyClass {");
		t.pl("	class SubClass extends java.util.ArrayList {}");
		t.pl("}");
		
		JType type = t.asJTypeSnippet().findChildTypesMatching(AJType.with().simpleName("SubClass")).getFirst();
		
		Expect.that(type.isSubClassOf(List.class)).is(true);
	}
	
	@Test
	public void isSubclassOfConcreteClassChildIsEmbeddedClass(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MyClass {");
		t.pl("	private static class MySubClass extends " + MyParentClass.class.getName() + "{}");
		t.pl("}");
		JType type = t.asJTypeSnippet();
		
		Expect
			.that(type.findDirectChildTypes().getFirst().isSubClassOf(MyParentClass.class))
			.is(true);
	}
	
	/**
	 * Bug resolving the correct fqdn for a type declared in a compilation unit where there were both a class in the same
	 * package which was also referenced in imports, and the compilation unit contained a class with the same name. This caused
	 * the external class to be resolved instead of the compilation unit version as would be expected
	 */
	@Test
	public void isSubclassOfConcreteClassChildIsEmbeddedAndClassWithSameSimpleNameAlsoExistsInPackage(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("pkg", JTypeTest.class.getPackage().getName());
		t.v("clashingClassName", JTypeTest.class.getSimpleName());
		
		t.pl("package ${pkg};");
		t.pl("class MyClass {");
		t.pl("	public class ${clashingClassName} /* same as other class in package */ {} ");//this name should be the one subclass extends resolves to, not the package one
		t.pl("	public class SubClass extends ${clashingClassName} {}");
		t.pl("}");
		
		JType subClass = t.asResolvedSourceFileNamed("${pkg}.MyClass").getTypeWithName("SubClass");
		
		String embeddedQualfiedName = JTypeTest.class.getPackage().getName() + ".MyClass." + JTypeTest.class.getSimpleName();
		
		assertEquals(true, subClass.isSubClassOf(embeddedQualfiedName));
	}
	
	@Test
	public void isSubclassOfGenericClass(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MySubClass extends " + MyParentGenericClass.class.getName() + "<String>{}");
		JType type = t.asJTypeSnippet();
		
		Expect.that(type.isSubClassOf(MyParentGenericClass.class)).is(true);
	}
	
	@Test
	public void isGenericSubclassOfConcreteClass(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MySubClass<T> extends " + MyParentClass.class.getName() + "{}");
		JType type = t.asJTypeSnippet();
		
		Expect.that(type.isSubClassOf(MyParentClass.class)).is(true);
	}
	
	@Test
	public void isGenericSubclassOfGenericClass(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MySubClass<T> extends " + MyParentGenericClass.class.getName() + "<T>{}");
		JType type = t.asJTypeSnippet();
		
		Expect.that(type.isSubClassOf(MyParentGenericClass.class)).is(true);
	}
	
	private static class MyParentClass {}
	private static class MyParentGenericClass<String> extends MyParentClass {}

	@Test
	public void testIsSubclassOfInterface(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.pl("class MySubClass implements " + MyInterface.class.getName() + "{}");
		JType type = t.asJTypeSnippet();
		
		Expect.that(type.isSubClassOf(MyInterface.class)).is(true);
	}
	
	public static class MyInterface {}
	
	@Test
	public void testFindJavaMethods(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.pl("class MyTestClass  {");
		t.pl( "public void methodA(){}" );
		t.pl( "public void methodB(){}" );
		t.pl("}");
	
		FindResult<JMethod> foundMethods = t.asResolvedJTypeNamed("MyTestClass").findAllJMethods();

		assertThat(
				foundMethods.toList(), 
				AList.of(JMethod.class)
					.inOrder()
					.withOnly()
					.item(AJMethod.with().name("methodA"))
					.item(AJMethod.with().name("methodB"))
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

		FindResult<JMethod> foundMethods = t.asResolvedJTypeNamed("MyTestClass").findMethodsMatching(AJMethod.with().nameMatchingAntPattern("get*"));
	
		assertThat(
				foundMethods.toList(), 
				AList.of(JMethod.class)
					.inOrder()
					.withOnly()
					.item(AJMethod.with().name("getA"))
					.item(AJMethod.with().name("getB"))
		);
	}

	@Test
	public void testFindJavaMethodsExcludingChildTypeMethods(){
		SourceTemplate t = ctxt.newSourceTemplate();

		t.pl("class MyTestClass{");
		t.pl("	public void getA(){ return; }" );
		t.pl("	private class Foo {" );
		t.pl("		public Object getB(){ return null;}//should be ignored" );
		t.pl("	}");
		t.pl("}");
		
		FindResult<JMethod> foundMethods = t.asResolvedJTypeNamed("MyTestClass").findMethodsMatching(AJMethod.with().nameMatchingAntPattern("get*"));

		assertThat(
				foundMethods.toList(), 
				AList.withOnly(AJMethod.with().name("getA"))
		);
	}

	@Test
	//For a bug where performing a method search on top level children also return nested methods
	public void testFindJavaMethodsExcludingAnonymousTypeMethodsEmbeddedInMethods(){
		JType type = SourceHelper.findSourceForTestClass(getClass()).getTypeWithName(MyTestClass.class);
		FindResult<JMethod> foundMethods = type.findMethodsMatching(AJMethod.with().nameMatchingAntPattern("get*"));

		assertThat(
			foundMethods.toList(), 
			AList.inOrder()
				.withOnly()
				.item(AJMethod.with().name("getA"))
				.item(AJMethod.with().name("getB"))
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
		t.pl( "public void getA(){ return; }" );
		t.pl( "private Foo foo = new Foo(){};//should resolve to Foo" );
		t.pl( "private class Foo {" );
		t.pl( "		public void getB(){ return;}" );
		t.pl("	}");
		t.pl("}");
		
		JField field = t.asResolvedJTypeNamed("MyTestClass").findFieldsMatching(AJField.with().name("foo")).getFirst();
		
		assertThat(field.getTypeSignature(),isEqualTo("Foo"));
	}
	
	@Test
	public void testIsAnonymousClass(){
		SourceTemplate t = ctxt.newSourceTemplate();

		t.pl("class MyTestClass  {");
		t.pl( "private Foo foo = new Foo(){};//anonymous" );
		t.pl( "private class Foo {" );
		t.pl("	}");
		t.pl("}");
		
		List<JType> types = t.asResolvedJTypeNamed("MyTestClass").findAllChildTypes().toList();

		assertThat(
			types,
			is(AList.of(JType.class)
				.inOrder()
				.withOnly()
				.item(AJType.with().isAnonymous())
				.item(not(AJType.with().isAnonymous()))
			)
		);
	}
	
	@Test
	public void testGetFullName(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.pl("package foo.bar;");
		
		t.pl("import java.util.Collection;");
		t.pl("import java.io.File;");

		t.pl("class MyTestClass  {");
		t.pl( "public Collection getA(){return null;}" );
		t.pl( "public Object getB(){return null;}" );
		t.pl("}");
	
		JType type = t.asResolvedSourceFileNamed("foo.bar.MyTestClass").getMainType();

		assertThat(type.getFullName(), isEqualTo("foo.bar.MyTestClass"));
	}

	@Test
	public void testHasNotAnnotationOfType(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("a1", NotMyAnnotation.class);
		t.pl("@${a1}");
		t.pl("class MyClass  {}");
	
		assertThat(t.asResolvedJTypeNamed("MyClass").hasAnnotationOfType(MyAnnotation.class),isFalse());
	}
	
	@Test
	public void testHasAnnotationOfType(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.v("a1", MyAnnotation.class);
		t.pl("@${a1}");
		t.pl("class MyClass {}");
		
		assertThat(t.asResolvedJTypeNamed("MyClass").hasAnnotationOfType(MyAnnotation.class),isTrue());
	}
	
	public @interface MyAnnotation {}
	
	public @interface NotMyAnnotation {}
	
	@Test
	public void testImplementsClass(){
		JSourceFile source = SourceHelper.findSourceForTestClass(getClass());
		assertThat(source.getTypeWithName(MyChildClass1.class).isSubClassOf(MyExtendedClass.class),isFalse());
		assertThat(source.getTypeWithName(MyChildClass2.class).isSubClassOf(MyExtendedClass.class),isTrue());
		assertThat(source.getTypeWithName(MyChildClass3.class).isSubClassOf(MyExtendedClass.class),isTrue());
		assertThat(source.getTypeWithName(MyNonExtendingClass.class).isSubClassOf(MyExtendedClass.class),isFalse());
	}
	
	public static class MyExtendedClass {
	}
	
	class MyClass {
		class MyChildClass1 {}
		class MyChildClass2 extends MyExtendedClass {}
		class MyChildClass3 extends MyChildClass2 {}
		class MyNonExtendingClass {}
	}

	@Test
	public void testFindJavaConstructors(){
		SourceTemplate t = ctxt.newSourceTemplate();
		
		t.pl("class MyTestClass { ");
		t.pl( "MyTestClass(){}" );
		t.pl( "MyTestClass(String foo){}" );
		t.pl( "public void someMethod(){}" );
		t.pl("}");

		FindResult<JMethod> foundMethods = t.asResolvedJTypeNamed("MyTestClass").findMethodsMatching(AJMethod.that().isConstructor());

		assertThat(
				foundMethods.toList(), 
				is(AList.of(JMethod.class)
					.inOrder()
					.withOnly()
					.item(AJMethod.with().name("MyTestClass"))
					.item(AJMethod.with().name("MyTestClass")))
		);
	}
}
