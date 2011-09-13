package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.AssertingAstMatcher;
import com.bertvanbrakel.codemucker.ast.JavaSourceFile;
import com.bertvanbrakel.codemucker.ast.MutableJavaSourceFile;
import com.bertvanbrakel.codemucker.ast.MutableJavaType;
import com.bertvanbrakel.codemucker.ast.SourceFileVisitor;
import com.bertvanbrakel.codemucker.ast.a.TestBean;
import com.bertvanbrakel.codemucker.ast.finder.JavaSourceFinder;
import com.bertvanbrakel.test.finder.ClassFinderOptions;
import com.bertvanbrakel.test.util.TestHelper;

public class ModifyClassTest {

	@Test
	public void testAddSimpleField() throws Exception {		
		MutableJavaType type = findType("TestBeanSimple");
		type.addFieldSnippet("private String foo;");
		
		assertAstEquals("TestBeanSimple.java.testAddSimpleField", type);
	}
	
	@Test
	public void testAddFieldAndMethods() throws Exception {		
		MutableJavaType type = findType("TestBeanSimple");
		type.addFieldSnippet("private String foo;");
		type.addMethodSnippet("public void setFoo(String foo){ this.foo = foo; }");
		assertAstEquals("TestBeanSimple.java.testAddFieldAndMethods", type);
	}
	
	
	@Test
	public void testAddFieldMethodsWithInnerClasses() throws Exception {
		MutableJavaType type = findType("TestBean");
		type.addFieldSnippet("private String foo;");
		type.addMethodSnippet("public String getFoo(){ return this.foo; }");
		type.addMethodSnippet("public void setFoo(String foo){ this.foo = foo; }");
		type.addConstructorSnippet("public TestBean(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBean.java.testAddFieldMethodsWithInnerClasses", type);
	}
	
	@Test
	public void testFindClassesWithAnnotations() throws Exception {
		fail("TODO");
		
//		TypeSource src = getSource("TestBeanSimple");
//		src.addFieldSnippet("private String foo;");
		//src.addMethodSnippet("public String getFoo(){ return this.foo; }");
		//src.addMethodSnippet("public void setFoo(String foo){ this.foo = foo; }");
		//src.addConstructorSnippet("public TestBean(String foo){ this.foo = foo; }");

//		finder.visit(visitor);
//
//		new FieldMatcher().annotation(BeanProperty.class).inClass().pkgAntPattern("*generation.a");
//		new MethodMatcher().nameAntPattern("get*").numArgs(0);

		// todo:get all classes with annotation X

	}
	
	private MutableJavaType findType(String typeClassName){
		JavaSourceFinder finder = new JavaSourceFinder();
		ClassFinderOptions opts = finder.getOptions();
		opts.includeClassesDir(false);
		opts.includeTestDir(true);
		opts.includeFileName("*/ast/a/" + typeClassName + ".java");

		//TypeLoggingVisitor visitor = new TypeLoggingVisitor();

		for( JavaSourceFile srcFile:finder.findSourceFiles()){
			//srcFile.visit(visitor);
			MutableJavaSourceFile src = new MutableJavaSourceFile(srcFile);
			return src.getMainTypeAsMutable();
		}
		
		fail("Can't find " + typeClassName);
		return null;
	}
	
	private void assertAstEquals(String expectPath, MutableJavaType actual){
		TestHelper helper = new TestHelper();
		//read the expected result
		JavaSourceFile srcFile = actual.getDeclaringFile().getSourceFile();
		String expectSrc = null;
        try {
	        expectSrc = helper.getTestJavaSourceDir().childResource(TestBean.class, expectPath).readAsString();
        } catch (IOException e) {
	        fail("Couldn't read source file " + expectPath);
        }
		CompilationUnit expectCu = srcFile.getAstCreator().parseCompilationUnit(expectSrc);
		AssertingAstMatcher matcher = new AssertingAstMatcher(false);
		CompilationUnit actualCu = actual.getDeclaringFile().getCompilationUnit();
		boolean equals = actualCu.subtreeMatch(matcher, expectCu);
		assertTrue("ast's don't match", equals);
		//assertEquals(expectAst, actualAst);
	}
	
	// todo:turn this into a saearch index/pull parser thingy? so we can ask for
	// a class with certain annotations and ask for all methods
	// maybe point to source locations so we can perform fast lookup on methods
	// instad of holding all in memory...
	public static class TypeLoggingVisitor extends SourceFileVisitor {

		@Override
		public boolean visit(File rootDir, String relPath, File srcFile) {
			return log("file", relPath);
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			return log("typeDec", node.getName());
		}

		private boolean log(String msg, Object obj) {
			System.out.println(msg + "---->" + obj.toString());
			return true;
		}
	}
}
