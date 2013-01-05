package com.bertvanbrakel.codemucker.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.Filter;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchRoots;
import com.bertvanbrakel.codemucker.ast.matcher.AType;

public class JavaSourceFinderTest {

	@Test
	public void testFindClassesWithAnnotations() throws Exception {
		JSourceFinder finder = JSourceFinder.builder()
			.setSearchRoots(SearchRoots.builder()
				.setIncludeClassesDir(false)
				.setIncludeTestDir(true)
			)
			.setFilter(Filter.builder()
				.addIncludeTypes(AType.withAnnotation(MyAnnotation.class))
			)
			.build();
		boolean found = false;
		List<JType> foundTypes = finder.findTypes().toList();
		
		for( JType type:foundTypes){
			assertEquals(ClassWithAnnotation.class.getSimpleName(), type.getSimpleName());
			boolean hasAnon = type.hasAnnotationOfType(MyAnnotation.class,true);
			assertTrue("expected annotation", hasAnon);
			found = true;
		}
		assertEquals(1, foundTypes.size());
		
		assertTrue("Expected type to be found", found);
	}
	
	@Test
	public void testFindWithMethods(){
		JSourceFinder finder = JSourceFinder.builder()
			.setSearchRoots(SearchRoots.builder()
				.setIncludeClassesDir(true)
				.setIncludeTestDir(true)
			)
			.build();
		finder.findMethods();
	}

	public static @interface MyAnnotation {

	}

	@MyAnnotation
	private static class ClassWithAnnotation {

	}

	private static class ClassWithNoAnnotation {

	}
	
	private static class Foo {
		public static @interface MyAnnotation2 {

		}
		
		@MyAnnotation2
		public void bar(){
			
		}
	}
}
