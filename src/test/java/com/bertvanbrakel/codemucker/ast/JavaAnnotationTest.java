package com.bertvanbrakel.codemucker.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.jdt.core.dom.Annotation;
import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.Generated;
import com.bertvanbrakel.codemucker.ast.finder.Filter;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchRoots;
import com.bertvanbrakel.codemucker.ast.matcher.ASourceFile;

public class JavaAnnotationTest {

	@Test
	public void test_resolveSimpleName(){
		JSourceFinder finder = JSourceFinder.newBuilder()
			.setSearchRoots(SearchRoots.newBuilder()
				.setIncludeClassesDir(false)
				.setIncludeTestDir(true)
			)
			.setFilter(Filter.newBuilder()
				.setIncludeSource(ASourceFile.withName(JavaAnnotationTest.class))
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
