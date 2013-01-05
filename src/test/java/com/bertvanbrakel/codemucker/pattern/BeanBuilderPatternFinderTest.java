package com.bertvanbrakel.codemucker.pattern;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.Filter;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchRoots;
import com.bertvanbrakel.codemucker.ast.matcher.AMethod;
import com.bertvanbrakel.codemucker.ast.matcher.AType;
import com.bertvanbrakel.codemucker.matcher.AInt;
import com.bertvanbrakel.test.finder.matcher.Matcher;

public class BeanBuilderPatternFinderTest {

	@Test
	public void testFindBuilders() {
		//find classes with the name 'Builder' - confidence 90%
		//classes which subclass anything called 'builder' - confidence 80%
		//classes where most methods return itself - confidence 60%
		//classes which contain a method starting with 'build' - confidence 70%
		
		FindResult<JType> foundBuilders = JSourceFinder.builder()
				.setSearchRoots(SearchRoots.builder()
						.setIncludeClassesDir(true)
						.setIncludeTestDir(true)
					)
					.setFilter(Filter.builder()
						//.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
						//TODO:have matchers return confidences?? then finder can add that to results..
						.addIncludeTypes(AType.withFullName("*Builder"))
						//.addIncludeTypesWithMethods(JMethodMatchers.withMethodNamed("build*"))
					)	
			.build()
			.findTypes();
		
		//System.out.println("found potential builders:\n" + Joiner.on("\n================================================").join(foundBuilders) );
	
		for (JType type : foundBuilders) {
			System.out.println( type.getFullName());
			
			//builds what???
			FindResult<JMethod> methods = type.findMethodsMatching(AMethod.withMethodNamed("build*"));
			for (JMethod method : methods) {
				//could do checks on the build method here. COntains args? maybe not good?
				//return null? another warning
				//no build methods? another errors
				System.out.println("\t--> " + method.getAstNode().getReturnType2());
			}	
		}
	}
	
	@Test
	public void testFindLongCtorClasses() {
		Matcher<JMethod> methodMatcher =
			AMethod.all(
				AMethod.isConstructor(), 
				AMethod.withNumArgs(AInt.greaterOrEqualTo(3))
			);
		Iterable<JMethod> found = JSourceFinder.builder()
				.setSearchRoots(SearchRoots.builder()
						.setIncludeClassesDir(true)
						.setIncludeTestDir(true)
					)
					.setFilter(Filter.builder()
						//.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
						//TODO:have matchers return confidences?? then finder can add that to results..
						.addIncludeTypesWithMethods(methodMatcher)
						.addIncludeMethods(methodMatcher)
						//.addIncludeTypesWithMethods(JMethodMatchers.withMethodNamed("build*"))
					)	
			.build()
			.findMethods();

		for (JMethod method : found) {
			System.out.print( method.getAstNode().parameters().size());
			System.out.println( " " + method.getClashDetectionSignature());	
		}
	}
	
	@Test
	public void testFindImmutableClasses() {
		//find classes where fields are not all final
		//where the fields are modifiable via public methods (or their call chains)
		 //(so find all caller of this method, and their callers etc...)
		// -->all references to this class, and a method call is made, object db?
	
	
	
	}
}
