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
import com.bertvanbrakel.test.finder.SourceFile;
import com.bertvanbrakel.test.finder.SourceFinder;
import com.bertvanbrakel.test.generation.a.TestBean;
import com.bertvanbrakel.test.util.SourceUtil;

public class ModifyClassTest {

	@Test
	public void testAddSimpleField() throws Exception {		
		MutableSourceFile src = getSource("TestBeanSimple");
		src.addFieldSnippet("private String foo;");
		
		assertAstEquals("TestBeanSimple.java.testAddSimpleField", src);
	}
	
	@Test
	public void testAddFieldAndMethods() throws Exception {		
		MutableSourceFile src = getSource("TestBeanSimple");
		src.addFieldSnippet("private String foo;");
		src.addMethodSnippet("public void setFoo(String foo){ this.foo = foo; }");
		assertAstEquals("TestBeanSimple.java.testAddFieldAndMethods", src);
	}
	
	
	@Test
	public void testAddFieldMethodsWithInnerClasses() throws Exception {
		MutableSourceFile src = getSource("TestBean");
		src.addFieldSnippet("private String foo;");
		src.addMethodSnippet("public String getFoo(){ return this.foo; }");
		src.addMethodSnippet("public void setFoo(String foo){ this.foo = foo; }");
		src.addConstructorSnippet("public TestBean(String foo){ this.foo = foo; }");
		
		assertAstEquals("TestBean.java.testAddFieldMethodsWithInnerClasses", src);
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
	
	private MutableSourceFile getSource(String beanName){
		SourceFinder finder = new SourceFinder();
		ClassFinderOptions opts = finder.getOptions();
		opts.includeClassesDir(false);
		opts.includeTestDir(true);
		opts.includeFileName("*/generation/a/" + beanName + ".java");

		TypeLoggingVisitor visitor = new TypeLoggingVisitor();

		for( SourceFile srcFile:finder.findSourceFiles()){
			srcFile.visit(visitor);
			CompilationUnit cu = srcFile.getCompilationUnit();
			List<AbstractTypeDeclaration> types = cu.types();
			assertEquals(1, types.size());
			
			AbstractTypeDeclaration type = types.iterator().next();
			System.out.println("type=" + type.getName());
			MutableSourceFile src = new MutableSourceFile(srcFile, type);
			if( src.isClass()){
				assertEquals(beanName, src.asType().getName().getIdentifier());
				return src;				
			}
		}
		
		fail("Can't find " + beanName);
		return null;
	}
	
	private void assertAstEquals(String expectPath, MutableSourceFile actual){

		TestHelper helper = new TestHelper();
		//read the expected result
		SourceFile srcFile = actual.getSourceFile();
		String expectSrc = null;
        try {
	        expectSrc = helper.getTestJavaSourceDir().childResource(TestBean.class, expectPath).readAsString();
        } catch (IOException e) {
	        fail("Couldn't read source file " + expectPath);
        }
		CompilationUnit expectAst = srcFile.getAstCreator().parseCompilationUnit(expectSrc);

		boolean compareAST = true;
		if( compareAST ){
			AssertingAstMatcher matcher = new AssertingAstMatcher(false);
			CompilationUnit actualAst = actual.getSourceFile().getCompilationUnit();
			boolean equals = expectAst.subtreeMatch(matcher, actualAst);
			assertTrue("ast's don't match", equals);
			//assertEquals(expectAst, actualAst);
		} else {
    		File tmp = helper.createTempFile();
    		actual.writeChangesToFile(tmp);
    		
    		String actualSrc = SourceUtil.readSource(tmp);
    		assertEquals(cleanSpaces(expectSrc), cleanSpaces(actualSrc));
    	}
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
