package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.jdt.core.dom.Annotation;
import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.Generated;
import com.bertvanbrakel.codemucker.ast.finder.JavaSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SourceFinderOptions;
import com.bertvanbrakel.codemucker.ast.finder.matcher.FileMatchers;

public class JavaAnnotationTest {

	@Test
	public void test_resolveSimpleName(){
		
		JavaSourceFinder finder = new JavaSourceFinder();
		SourceFinderOptions opts = finder.getOptions();
		opts.includeTestDir(true);
		opts.includeFile(FileMatchers.withName(JavaAnnotationTest.class));
		
		JavaSourceFile sf = finder.findSources().iterator().next();
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
