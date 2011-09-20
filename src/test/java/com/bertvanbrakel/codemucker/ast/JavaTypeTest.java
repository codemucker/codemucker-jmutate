package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.codemucker.util.SrcWriter;
import com.bertvanbrakel.test.util.ClassNameUtil;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaTypeTest {

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

		JavaType type = newJavaType(w, "foo.bar.Foo", "AnotherTopClass");
		assertTrue(type.isTopLevelClass());
		assertFalse(type.getTypeWithName("MyInnerClass").isTopLevelClass());
	}

	public JavaType newJavaType(SrcWriter w, String fqClassName) {
		String simpleClassName = ClassNameUtil.extractShortClassNamePart(fqClassName);
		return newJavaType(w, fqClassName, simpleClassName);
	}

	public JavaType newJavaType(SrcWriter w, String fqClassName, String typeToGet) {
		JavaSourceFile srcFile = newJavaSrc(w, fqClassName);
		JavaType type = new JavaType(srcFile, srcFile.getTopTypeWithName(typeToGet));

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
