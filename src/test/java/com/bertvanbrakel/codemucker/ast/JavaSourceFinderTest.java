package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.codemucker.ast.matcher.AType;

public class JavaSourceFinderTest {

	@Test
	public void testFindClassesWithAnnotations() throws Exception {
		JSourceFinder finder = JSourceFinder.newBuilder()
			.setSearchPaths(SearchPathsBuilder.newBuilder()
				.setIncludeClassesDir(false)
				.setIncludeTestDir(true)
			)
			.setFilter(FilterBuilder.newBuilder()
				.addIncludeTypes(AType.withAnnotation(MyAnnotation.class))
			)
			.build();
		boolean found = false;
		List<JType> foundTypes = list(finder.findTypes());
		
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
		JSourceFinder finder = JSourceFinder.newBuilder()
			.setSearchPaths(SearchPathsBuilder.newBuilder()
				.setIncludeClassesDir(true)
				.setIncludeTestDir(true)
			)
			.build();
		finder.findMethods();
	}
	
	
	private static <T> List<T> list(Iterable<T> it) {
		List<T> list = new ArrayList<T>();
		for (T item : it) {
			list.add(item);
		}
		return list;
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
