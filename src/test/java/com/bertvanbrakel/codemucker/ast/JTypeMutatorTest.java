package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Ignore;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.a.TestBean;
import com.bertvanbrakel.codemucker.ast.a.TestBeanSimple;
import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.test.util.TestHelper;

public class JTypeMutatorTest {

	private MutationContext context = new DefaultMutationContext();
	
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
		type.addField("private String foo;");
		type.addMethod("public String getFoo(){ return this.foo; }");
		type.addMethod("public void setFoo(String foo){ this.foo = foo; }");
		type.addCtor("public TestBean(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBean.java.testAddFieldMethodsWithInnerClasses", type);
	}
	
	@Ignore("need to reimplement replacement")
	@Test
	public void testAddDuplicateSimpleField() throws Exception {		
		JTypeMutator type = findType(TestBeanSimple.class.getSimpleName());
		//TOOD:make this work again!	type.addField("private int fieldOne;").replace(true).apply();
		
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
		List<JSourceFile> sources = finder.findSources().toList();
		assertEquals("expected only a single match",1,sources.size());
		JSourceFile srcFile = sources.iterator().next();
		return srcFile.asMutator(context).getMainTypeAsMutable();
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
        CompilationUnit expectCu = context.newSourceTemplate()
        	.setTemplate(expectSrc)
        	.asCompilationUnit();
        
        SourceUtil.assertAstsMatch(expectCu, expectCu);
    }
}
