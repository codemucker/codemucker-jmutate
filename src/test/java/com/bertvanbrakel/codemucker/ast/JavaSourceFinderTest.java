package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.fail;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.JavaSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SourceFinderOptions;

public class JavaSourceFinderTest {

	@Test
	public void testFindClassesWithAnnotations() throws Exception {
		JavaSourceFinder finder = new JavaSourceFinder();
		SourceFinderOptions opts = finder.getOptions();
		opts.includeClassesDir(false);
		opts.includeTestDir(true);


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
}
