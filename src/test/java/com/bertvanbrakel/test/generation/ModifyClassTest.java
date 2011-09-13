package com.bertvanbrakel.test.generation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

import com.bertvanbrakel.test.TestHelper;
import com.bertvanbrakel.test.finder.AssertingAstMatcher;
import com.bertvanbrakel.test.finder.ClassFinderOptions;
import com.bertvanbrakel.test.finder.JavaSourceFile;
import com.bertvanbrakel.test.finder.SourceFinder;
import com.bertvanbrakel.test.generation.a.TestBean;
import com.bertvanbrakel.test.util.SourceUtil;

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
		SourceFinder finder = new SourceFinder();
		ClassFinderOptions opts = finder.getOptions();
		opts.includeClassesDir(false);
		opts.includeTestDir(true);
		opts.includeFileName("*/generation/a/" + typeClassName + ".java");

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
		boolean equals = expectCu.subtreeMatch(matcher, actualCu);
		assertTrue("ast's don't match", equals);
		//assertEquals(expectAst, actualAst);
	}
	
	private String cleanSpaces(String s){
		s = s.replaceAll("\\r\\n+", "\n");
		s = s.replaceAll("\\s+", " ");
		s = s.replaceAll("\\t+", "    ");
		return s;
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
