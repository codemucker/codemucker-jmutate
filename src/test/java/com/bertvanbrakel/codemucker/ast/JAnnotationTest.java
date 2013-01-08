package com.bertvanbrakel.codemucker.ast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.Generated;
import com.bertvanbrakel.codemucker.ast.finder.Filter;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.matcher.ASourceFile;
import com.bertvanbrakel.codemucker.ast.matcher.AnAnnotation;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.lang.matcher.AList;
import com.bertvanbrakel.test.finder.Roots;

public class JAnnotationTest {

	private MutationContext context = new SimpleMutationContext();
	
	@Test
	public void test_findAnnotations(){
		SourceTemplate t = context.newSourceTemplate();
		t.v("a1", MyAnnotation1.class.getName());
		t.v("a2", MyAnnotation2.class.getName());
		t.pl("import ${a1};");
		t.pl("import ${a2};");
		t.pl("@${a1}");
		t.pl("class MyClass {");
		t.pl("	@${a2}");
		t.pl(" 	class MySubClass {");
		t.pl("	}");
		t.pl("}");
		
		JType type = t.asSourceFile().getMainType();
		
		List<Annotation> found = JAnnotation.findAnnotations(type.getAstNode());

		assertThat(found, 
			AList.of(Annotation.class)
				.inOrder()
				.containingOnly()
				.item(AnAnnotation.withFqn(MyAnnotation1.class))
				.item(AnAnnotation.withFqn(MyAnnotation2.class))
		);
	}

	@Test
	public void test_findAnnotations_depth(){
		SourceTemplate t = context.newSourceTemplate();
		t.v("a1", MyAnnotation1.class.getName());
		t.v("a2", MyAnnotation2.class.getName());
		t.pl("import ${a1};");
		t.pl("import ${a2};");
		t.pl("@${a1}");
		t.pl("class MyClass {");
		t.pl("	@${a2}");
		t.pl(" 	class MySubClass {");
		t.pl("	}");
		t.pl("}");
		
		JType type = t.asSourceFile().getMainType();
		
		List<Annotation> found = JAnnotation.findAnnotations(type.getAstNode(), JAnnotation.DIRECT_DEPTH);

		assertThat(found, AList.ofOnly(AnAnnotation.withFqn(MyAnnotation1.class)));
	}

	private @interface MyAnnotation1 {
		
	}
	
	private @interface MyAnnotation2 {
		
	}
	
	@Test
	public void test_resolveSimpleName(){
		JSourceFinder finder = JSourceFinder.builder()
			.setSearchRoots(Roots.builder()
				.setIncludeClassesDir(false)
				.setIncludeTestDir(true)
			)
			.setFilter(Filter.builder()
				.setIncludeSource(ASourceFile.withName(JAnnotationTest.class))
			)
			.build();
	
		FindResult<JSourceFile> found = finder.findSources();
		assertEquals(1,found.toList().size());
		JSourceFile sf = found.iterator().next();
		JType type = sf.getTypeWithName(TestBean.class);
		Collection<Annotation> annons = type.getAnnotations();
		
		assertEquals(1, annons.size());
	
		Annotation anon = annons.iterator().next();
		JAnnotation ja = JAnnotation.from(anon);
		
		assertEquals(Generated.class.getName(), ja.getQualifiedName());
		assertTrue(ja.isOfType(Generated.class));	
	}
	
	public static class TestBean {
		
		@Generated
		public void myMethod(){
			
		}
	}
}
