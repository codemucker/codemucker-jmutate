package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.a.TestBean;
import com.bertvanbrakel.codemucker.ast.a.TestBeanSimple;
import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.test.util.TestHelper;

public class JTypeMutatorTest {

	private JContext context = new DefaultJContext();
	
	@Test
	public void testAddSimpleField() throws Exception {		
		JTypeMutator type = findType(TestBeanSimple.class.getSimpleName());
		type.addField("private String foo;");
		
		assertAstEquals("TestBeanSimple.java.testAddSimpleField", type);
	}
	
	@Test
	public void testAddFieldAndMethods() throws Exception {		
		JTypeMutator type = findType(TestBeanSimple.class.getSimpleName());
		type.addField("private String foo;");
		type.addMethod("public void setFoo(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBeanSimple.java.testAddFieldAndMethods", type);
	}
	
	@Test
	public void testAddFieldMethodsWithInnerClasses() throws Exception {
		JTypeMutator type = findType(TestBean.class.getSimpleName());
		type.fieldChange("private String foo;").apply();
		type.methodChange("public String getFoo(){ return this.foo; }").apply();
		type.methodChange("public void setFoo(String foo){ this.foo = foo; }").apply();
		type.ctorChange("public TestBean(String foo){ this.foo = foo; }").apply();
		
		assertAstEquals("TestBean.java.testAddFieldMethodsWithInnerClasses", type);
	}
	
	@Test
	public void testAddDuplicateSimpleField() throws Exception {		
		JTypeMutator type = findType(TestBeanSimple.class.getSimpleName());
		type.fieldChange("private int fieldOne;").replace(true).apply();
		
		assertAstEquals("TestBeanSimple.java.testAddDuplicateSimpleField", type);
	}
	
	
	private JTypeMutator findType(String simpleClassName){
		JSourceFinder finder = JSourceFinder.newBuilder()
			.setSearchPaths(
				SearchPathsBuilder.newBuilder()
					.setIncludeClassesDir(false)
					.setIncludeTestDir(true)
			)
			.setFilter(
				FilterBuilder.newBuilder()
					.setIncludeFileName("*/ast/a/" + simpleClassName + ".java")
			)
			.build();
		List<JSourceFile> sources = finder.findSources().asList();
		assertEquals("expected only a single match",1,sources.size());
		JSourceFile srcFile = sources.iterator().next();
		return srcFile.asMutator().getMainTypeAsMutable();
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