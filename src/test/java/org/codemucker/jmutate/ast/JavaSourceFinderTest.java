package org.codemucker.jmutate.ast;

import static org.codemucker.jmatch.Assert.assertThat;
import static org.codemucker.jmatch.Assert.is;
import static org.codemucker.jmatch.Assert.isFalse;
import static org.codemucker.jmatch.Assert.isTrue;

import java.util.List;

import org.codemucker.jmatch.AList;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmutate.SourceHelper;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.finder.Filter;
import org.codemucker.jmutate.ast.finder.FindResult;
import org.codemucker.jmutate.ast.finder.JSourceFinder;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.junit.Test;


public class JavaSourceFinderTest {

	@Test
	public void testFindClassesWithMethodMatch() throws Exception {
		JSourceFinder finder = SourceHelper.newAllSourcesResolvingFinder()
			.setFilter(Filter.builder()
				.addIncludeTypes(AJType.withMethod(AJMethod.withNameMatchingAntPattern("testFindClassesWithMethodMatch")))
			)
			.build();
		JType type = finder.findTypes().getFirst();
		
		assertThat(type, is(AJType.withName(JavaSourceFinderTest.class)));
	}
	
	@Test
	public void testFindClassesExtending() throws Exception {
		JSourceFinder finder = SourceHelper.newAllSourcesResolvingFinder()
			.setFilter(Filter.builder()
				.addIncludeTypes(AJType.subclassOf(MyClass.class))
			)
			.build();

		assertThat(
			finder.findTypes(),
			is(AList.of(JType.class)
				.inAnyOrder()
				.containingOnly()
				.item(AJType.withName(MySubClass.class))
				.item(AJType.withName(MySubSubClass.class)
			))
		);
	}
	
	@Test
	public void testFindClassesWithAnnotations() throws Exception {
		JSourceFinder finder = SourceHelper.newAllSourcesResolvingFinder()
			.setFilter(Filter.builder()
				.addIncludeTypes(AJType.withAnnotation(MyAnnotation.class))
			)
			.build();
		boolean found = false;
		List<JType> foundTypes = finder.findTypes().toList();
		
		for( JType type:foundTypes){
			assertThat(type.getSimpleName(), is(AString.equalTo(ClassWithAnnotation.class.getSimpleName())));
			boolean hasAnon = type.hasAnnotationOfType(MyAnnotation.class,true);
			
			assertThat(hasAnon,isTrue());
			found = true;
		}
		assertThat(foundTypes.size(),is(AnInt.equalTo(1)));
		assertThat(found,isTrue());
	}
	
	@Test
	public void testFindWithMethods(){
		JSourceFinder finder = SourceHelper.newAllSourcesResolvingFinder().build();
		
		FindResult<JMethod> methods = finder.findMethods();
		assertThat(methods.isEmpty(),isFalse());
	}

	public static @interface MyAnnotation {

	}

	@MyAnnotation
	private static class ClassWithAnnotation {

	}

	private static class ClassWithNoAnnotation {

	}

	private static class MyClass {
	}
	
	private static class MySubClass extends MyClass {
	}
	
	private static class MySubSubClass extends MySubClass {
	}
}
