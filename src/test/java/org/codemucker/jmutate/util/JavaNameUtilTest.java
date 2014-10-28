package org.codemucker.jmutate.util;

import static org.junit.Assert.assertEquals;

import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JType;
import org.junit.Test;

public class JavaNameUtilTest {

	DefaultMutateContext ctxt = DefaultMutateContext.with().defaults().build();
	
	/**
	 * bug where interfaces declared on types are not being resolved correctly
	 */
	@Test
	public void resolveQualifiedName_onInterfacesDeclaredInSamePackageMembers(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.var("pkg", JavaNameUtilTest.class.getPackage().getName());
		t.var("declaringClass", JavaNameUtilTest.class.getName());
		t.pl("package ${pkg};");
		t.pl("import ${declaringClass}.MyInterface;");
		
		t.pl("class MyClass {");
		t.pl("public MyInterface myField;");
		t.pl("}");
		
		JField field = t.asResolvedSourceFileNamed("${pkg}.MyClass").getMainType().findFields().getFirst();
		String expect = JavaNameUtil.compiledNameToSourceName(MyInterface.class);
		assertEquals(expect, JavaNameUtil.resolveQualifiedName(field.getType()));
	}
	
	@Test
	public void resolveQualifiedName_handleStarImports(){
		//TODO:what about when it's not a compiled class but a generated source file?
		SourceTemplate t = ctxt.newSourceTemplate();
		t.var("pkg", JavaNameUtilTest.class.getPackage().getName());
		t.var("declaringClass", JavaNameUtilTest.class.getName());
		t.pl("package ${pkg};");
		t.pl("import java.util.*;");
		t.pl("import ${declaringClass}.*;");
		
		t.pl("class MyClass {");
		t.pl("public MyInterface myField;");
		t.pl("}");
		
		JField field = t.asResolvedSourceFileNamed("${pkg}.MyClass").getMainType().findFields().getFirst();
		String expect = JavaNameUtil.compiledNameToSourceName(MyInterface.class);
		assertEquals(expect, JavaNameUtil.resolveQualifiedName(field.getType()));
	}
	
	/**
	 * Bug resolving the correct fqdn for a type declared in a compilation unit where there were both a class in the same
	 * package which was also referenced in imports, and the compilation unit contained a class with the same name. This caused
	 * the external class to be resolved instead of the compilation unit version as would be expected
	 */
	@Test
	public void resolveQualifiedName_forNameReferencingTypeInSameCompilationUnitWhereSameSimpleNameClassExistsInPackage(){
		SourceTemplate t = ctxt.newSourceTemplate();
		t.var("pkg", JavaNameUtilTest.class.getPackage().getName());
		t.var("clashingClassName", JavaNameUtilTest.class.getSimpleName());
		
		t.pl("package ${pkg};");
		t.pl("class MyClass {");
		t.pl("public class ${clashingClassName} {} ");
		t.pl("public class SubClass extends ${clashingClassName} {}");//should resolve to internal class rather than this test class
		t.pl("}");
		
		JType subClass = t.asResolvedSourceFileNamed("${pkg}.MyClass").getTypeWithName("SubClass");
		
		String expectName = JavaNameUtilTest.class.getPackage().getName() + ".MyClass." + JavaNameUtilTest.class.getSimpleName();
		//SimpleType parent = (SimpleType)subClass.asTypeDecl().getSuperclassType();
		String actualName = JavaNameUtil.resolveQualifiedName(subClass.asTypeDecl().getSuperclassType());
		
		assertEquals(expectName, actualName);
	}

	public static interface MyInterface {
	};
	
}
