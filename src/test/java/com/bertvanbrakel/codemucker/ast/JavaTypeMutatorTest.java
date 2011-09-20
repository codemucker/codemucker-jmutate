package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.a.TestBean;
import com.bertvanbrakel.codemucker.ast.finder.JavaSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SourceFinderOptions;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaTypeMutatorTest {

	@Test
	public void testAddSimpleField() throws Exception {		
		JavaTypeMutator type = findType("TestBeanSimple");
		type.addFieldSnippet("private String foo;");
		
		assertAstEquals("TestBeanSimple.java.testAddSimpleField", type);
	}
	
	@Test
	public void testAddFieldAndMethods() throws Exception {		
		JavaTypeMutator type = findType("TestBeanSimple");
		type.addFieldSnippet("private String foo;");
		type.addMethodSnippet("public void setFoo(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBeanSimple.java.testAddFieldAndMethods", type);
	}
	
	@Test
	public void testAddFieldMethodsWithInnerClasses() throws Exception {
		JavaTypeMutator type = findType("TestBean");
		type.addFieldSnippet("private String foo;");
		type.addMethodSnippet("public String getFoo(){ return this.foo; }");
		type.addMethodSnippet("public void setFoo(String foo){ this.foo = foo; }");
		type.addConstructorSnippet("public TestBean(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBean.java.testAddFieldMethodsWithInnerClasses", type);
	}
	
	private JavaTypeMutator findType(String typeClassName){
		JavaSourceFinder finder = new JavaSourceFinder();
		SourceFinderOptions opts = finder.getOptions();
		opts.includeClassesDir(false);
		opts.includeTestDir(true);
		opts.includeFileName("*/ast/a/" + typeClassName + ".java");
		
		JavaSourceFile srcFile = finder.findSourceFiles().iterator().next();
		JavaSourceFileMutator mutable = new JavaSourceFileMutator(srcFile);
		return mutable.getMainTypeAsMutable();
	}
	
	private void assertAstEquals(String expectPath, JavaTypeMutator actual){
		TestHelper helper = new TestHelper();
		//read the expected result
		JavaSourceFile srcFile = actual.getJavaType().getDeclaringSourceFile();
		String expectSrc = null;
        try {
	        expectSrc = helper.getTestJavaSourceDir().childResource(TestBean.class, expectPath).readAsString();
        } catch (IOException e) {
	        fail("Couldn't read source file " + expectPath);
        }
		CompilationUnit expectCu = srcFile.getAstCreator().parseCompilationUnit(expectSrc);
		AssertingAstMatcher matcher = new AssertingAstMatcher(false);
		CompilationUnit actualCu = actual.getJavaType().getDeclaringSourceFile().getCompilationUnit();
		boolean equals = actualCu.subtreeMatch(matcher, expectCu);
		assertTrue("ast's don't match", equals);
		//assertEquals(expectAst, actualAst);
	}
}
