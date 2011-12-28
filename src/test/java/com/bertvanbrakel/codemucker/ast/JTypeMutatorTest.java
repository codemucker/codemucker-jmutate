package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.a.TestBean;
import com.bertvanbrakel.codemucker.ast.finder.JavaSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SourceFinderOptions;
import com.bertvanbrakel.codemucker.ast.finder.matcher.FileMatchers;
import com.bertvanbrakel.test.util.TestHelper;

public class JTypeMutatorTest {

	private JContext context = new DefaultJContext();
	
	@Test
	public void testAddSimpleField() throws Exception {		
		JTypeMutator type = findType("TestBeanSimple");
		type.addField("private String foo;");
		
		assertAstEquals("TestBeanSimple.java.testAddSimpleField", type);
	}
	
	@Test
	public void testAddFieldAndMethods() throws Exception {		
		JTypeMutator type = findType("TestBeanSimple");
		type.addField("private String foo;");
		type.addMethod("public void setFoo(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBeanSimple.java.testAddFieldAndMethods", type);
	}
	
	@Test
	public void testAddFieldMethodsWithInnerClasses() throws Exception {
		JTypeMutator type = findType("TestBean");
		type.addField("private String foo;");
		type.addMethod("public String getFoo(){ return this.foo; }");
		type.addMethod("public void setFoo(String foo){ this.foo = foo; }");
		type.addCtor("public TestBean(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBean.java.testAddFieldMethodsWithInnerClasses", type);
	}
	
	private JTypeMutator findType(String typeClassName){
		JavaSourceFinder finder = new JavaSourceFinder(context);
		SourceFinderOptions opts = finder.getOptions();
		opts.includeClassesDir(false);
		opts.includeTestDir(true);
		opts.includeFile(FileMatchers.withPath("*/ast/a/" + typeClassName + ".java"));
		
		JavaSourceFile srcFile = finder.findSources().iterator().next();
		JavaSourceFileMutator mutable = new JavaSourceFileMutator(srcFile);
		return mutable.getMainTypeAsMutable();
	}
	
	private void assertAstEquals(String expectPath, JTypeMutator actual){
		TestHelper helper = new TestHelper();
		//read the expected result
		//JavaSourceFile srcFile = actual.getJavaType().getSource();
		String expectSrc = null;
        try {
	        expectSrc = helper.getTestJavaSourceDir().childResource(TestBean.class, expectPath).readAsString();
        } catch (IOException e) {
	        fail("Couldn't read source file " + expectPath);
        }
		CompilationUnit expectCu = context.getAstCreator().parseCompilationUnit(expectSrc);
		AssertingAstMatcher matcher = new AssertingAstMatcher(false);
		CompilationUnit actualCu = actual.getJavaType().getCompilationUnit();
		boolean equals = expectCu.subtreeMatch(matcher, actualCu);
		assertTrue("ast's don't match", equals);
		//assertEquals(expectAst, actualAst);
	}
}
