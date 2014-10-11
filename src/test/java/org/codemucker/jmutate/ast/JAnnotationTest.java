package org.codemucker.jmutate.ast;

import static org.codemucker.jmatch.Assert.assertThat;
import static org.codemucker.jmatch.Assert.isEqualTo;

import java.util.Collection;
import java.util.List;

import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.MutateContext;
import org.codemucker.jmutate.SourceHelper;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.matcher.AnAnnotation;
import org.codemucker.jpattern.IsGenerated;
import org.eclipse.jdt.core.dom.Annotation;
import org.junit.Test;

public class JAnnotationTest {

	private MutateContext context = new SimpleMutateContext();
	
	@Test
	public void test_findAnnotations(){
		SourceTemplate t = context.newSourceTemplate();
		t.v("a1", MyAnnotation1.class);
		t.v("a2", MyAnnotation2.class);
		t.pl("import ${a1};");
		t.pl("import ${a2};");
		t.pl("@${a1}");
		t.pl("class MyClass {");
		t.pl("	@${a2}");
		t.pl(" 	class MySubClass {");
		t.pl("	}");
		t.pl("}");
		
		JType type = t.asResolvedSourceFileNamed("MyClass").getMainType();
		
		List<Annotation> found = JAnnotation.findAnnotations(type.getAstNode());

		Expect
			.that(found)
			.is(AList
				.inOrder()
				.withOnly()
				.item(AnAnnotation.withFqn(MyAnnotation1.class))
				.and(AnAnnotation.withFqn(MyAnnotation2.class)));
	}

	@Test
	public void test_findAnnotations_depth(){
		SourceTemplate t = context.newSourceTemplate();
		t.v("a1", MyAnnotation1.class);
		t.v("a2", MyAnnotation2.class);
		t.pl("import ${a1};");
		t.pl("import ${a2};");
		t.pl("@${a1}");
		t.pl("class MyClass {");
		t.pl("	@${a2}");
		t.pl(" 	class MySubClass {");
		t.pl("	}");
		t.pl("}");
		
		JType type = t.asResolvedSourceFileNamed("MyClass").getMainType();
		
		List<Annotation> found = JAnnotation.findAnnotations(type.getAstNode(), JAnnotation.DIRECT_DEPTH);

		Expect
			.that(found)
			.is(AList.withOnly(AnAnnotation.withFqn(MyAnnotation1.class)));
	}

	public @interface MyAnnotation1 {
		
	}
	
	public @interface MyAnnotation2 {
		
	}
	
	@Test
	public void test_resolveSimpleName(){
		JType type = SourceHelper.findSourceForClass(JAnnotationTest.class).getTypeWithName(TestBean.class);
		Collection<Annotation> annons = type.getAnnotations();
		
		assertThat(annons.size(),isEqualTo(1));
	
		Annotation anon = annons.iterator().next();
		JAnnotation ja = JAnnotation.from(anon);
		
		assertThat(ja.getQualifiedName(),isEqualTo(IsGenerated.class.getName()));
		assertThat(ja.isOfType(IsGenerated.class));	
	}
	
	public static class TestBean {
		
		@IsGenerated
		public void myMethod(){
			
		}
	}
}
