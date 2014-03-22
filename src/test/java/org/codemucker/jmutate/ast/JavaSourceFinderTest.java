package org.codemucker.jmutate.ast;

import static org.codemucker.match.Assert.assertThat;
import static org.codemucker.match.Assert.is;
import static org.codemucker.match.Assert.isFalse;

import java.util.List;

import org.codemucker.jmutate.SourceHelper;
import org.codemucker.jmutate.ast.finder.Filter;
import org.codemucker.jmutate.ast.finder.FindResult;
import org.codemucker.jmutate.ast.finder.JSourceFinder;
import org.codemucker.jmutate.ast.finder.JSourceFinder.JFindListener;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.match.AList;
import org.codemucker.match.Expect;
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
				.addIncludeTypes(AJType.subclassOf(MyClass.class)))
			.build();

		Expect
			.that(finder.findTypes())
			.is(AList.of(JType.class)
				.inAnyOrder()
				.withOnly()
				.item(AJType.withName(MySubClass.class))
				.item(AJType.withName(MySubSubClass.class))
				.item(AJType.withName(org.codemucker.jmutate.ast.JavaSourceFinderTest.SomeClass.SomeEmbeddedSubClass.class)));
	}
	
	@Test
	public void testFindClassesWithAnnotations() throws Exception {
		JSourceFinder finder = SourceHelper.newAllSourcesResolvingFinder()
			.setFilter(Filter.builder()
				.addIncludeTypes(AJType.withAnnotation(MyAnnotation.class))
			)
			.setListener(new JFindListener(){

				@Override
				public void onMatched(Object result) {
	
				}

				@Override
				public void onIgnored(Object result) {
					if( result instanceof JType){
						
						JType src = (JType)result;
						
						if( src.getFullName().contains("ClassWithAnnotation")){
							System.out.println("ignored=" + src.getFullName());
							List<org.eclipse.jdt.core.dom.Annotation> as = src.getAnnotations();
							for(org.eclipse.jdt.core.dom.Annotation a:as){
								System.out.println("a=" + as);
							}
							System.out.println("getAnnotation=" + src.getAnnotationOfType(MyAnnotation.class));
							System.out.println("hasAnnotation=" + src.hasAnnotationOfType(MyAnnotation.class));
						}
					}
				}
				
			})
			.build();
		/*boolean found = false;
		List<JType> foundTypes = finder.findTypes().toList();
		*/
		Expect
			.that(finder.findTypes())
			.is(AList.of(JType.class)
				.inAnyOrder()
				.withOnly()
				.item(AJType.withName(ClassWithAnnotation.class)));
			
		/*for( JType type:foundTypes){
			assertThat(type.getSimpleName(), is(AString.equalTo(ClassWithAnnotation.class.getSimpleName())));
			boolean hasAnon = type.hasAnnotationOfType(MyAnnotation.class,true);
			
			assertThat(hasAnon,isTrue());
			found = true;
		}
		assertThat(foundTypes.size(),is(AnInt.equalTo(1)));
		assertThat(found,isTrue());
		*/
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
	
	private static class SomeClass {
		public static class SomeEmbeddedSubClass extends MySubClass {
			
		}
		
		public static class IgnoreMe {
			
		}
	}
}
