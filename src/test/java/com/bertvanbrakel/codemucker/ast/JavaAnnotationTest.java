package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.jdt.core.dom.Annotation;
import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.Generated;
import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.codemucker.ast.matcher.JSourceMatchers;

public class JavaAnnotationTest {

	@Test
	public void test_resolveSimpleName(){
		JSourceFinder finder = JSourceFinder.newBuilder()
			.setSearchPaths(SearchPathsBuilder.newBuilder()
				.setIncludeClassesDir(false)
				.setIncludeTestDir(true)
			)
			.setFilter(FilterBuilder.newBuilder()
				.setIncludeSource(JSourceMatchers.withName(JavaAnnotationTest.class))
			)
			.build();
	
		FindResult<JSourceFile> found = finder.findSources();
		assertEquals(1,found.toList().size());
		JSourceFile sf = found.iterator().next();
		JType type = sf.getTypeWithName(TestBean.class);
		Collection<Annotation> annons = type.getAnnotations();
		
		assertEquals(1, annons.size());
	
		Annotation anon = annons.iterator().next();
		JAnnotation ja = new JAnnotation(anon);
		
		assertEquals(Generated.class.getName(), ja.getQualifiedName());
		assertTrue(ja.isOfType(Generated.class));	
	}
	
	public static class TestBean {
		
		@Generated
		public void myMethod(){
			
		}
	}
}
