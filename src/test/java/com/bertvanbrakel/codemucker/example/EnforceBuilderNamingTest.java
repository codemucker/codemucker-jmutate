package com.bertvanbrakel.codemucker.example;

import static com.bertvanbrakel.codemucker.ast.matcher.AMethod.all;
import static com.bertvanbrakel.codemucker.ast.matcher.AMethod.isNotConstructor;
import static com.bertvanbrakel.codemucker.ast.matcher.AMethod.withAccess;
import static com.bertvanbrakel.codemucker.ast.matcher.AMethod.withMethodAnnotation;
import static com.bertvanbrakel.codemucker.ast.matcher.AMethod.withMethodNamed;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.Filter;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.matcher.AType;
import com.bertvanbrakel.test.finder.Roots;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class EnforceBuilderNamingTest 
{
	@Test
	@Ignore
	public void testEnsureBuildersAreCorrectlyNamed()
	{
		Iterable<JType> builders = JSourceFinder.builder()
				.setSearchRoots(Roots.builder()
					.setIncludeClassesDir(true)
					.setIncludeTestDir(true)
				)
				.setFilter(Filter.builder()
					.addIncludeTypes(AType.withSimpleName("*Builder"))
					.addExcludeTypes(AType.isAbstract())
				)
				.build()
				.findTypes();
		
		List<String> ignoreMethodsNamed = Lists.newArrayList("build","builder","copyOf");
		
		for (JType builder : builders) {			
			FindResult<JMethod> buildMethod = builder.findMethodsMatching(withMethodNamed("build"));
			Assert.assertFalse("expect to find a build method on " + builder.getFullName(), buildMethod.isEmpty());
			//TODO:check return type of builder
			//TODO:check no args
			//TODO:check annotated?
			
			String builderTypeName = builder.getSimpleName();
			
			System.out.println("builder: " + builder.getFullName());
			
			for( JMethod method : builder.findAllJMethods().filter(all(withAccess(JAccess.PUBLIC),isNotConstructor()))){
				//System.out.println("method: " + method.getAstNode().getReturnType2() + " " +  method.getClashDetectionSignature());
				
				if( !ignoreMethodsNamed.contains(method.getName()) && !method.getName().startsWith("build")){
					//not getter?
					//has set in it?
					
//					if( !method.getName().startsWith("set") && !method.getName().startsWith("with") && !method.getName().startsWith("add")){
//						String msg = String.format("Expected builder method %s.%s to start with 'set'", method.getJType().getFullName(), method.getName());
//						Assert.fail(msg);		
//					}
//					
					if( !method.getAstNode().getReturnType2().toString().equals(builderTypeName)){
						String msg = String.format("Expected builder method %s.%s to return the enclosing builder '%s' but got '%s' for \n\n method \n%s\n in parent \n%s ", 
									method.getJType().getFullName(),
									method.getClashDetectionSignature(),
									builderTypeName,
									method.getAstNode().getReturnType2().toString(),
									method.getAstNode(),
									method.getType()
								);
						Assert.fail(msg);		
					}
					//TODO:check return type is builder!
				}
			}
		}		
	}
	
	@Test
	public void testEnsureAllTestMethodsStartWithTest()
	{
		Iterable<JMethod> methods = JSourceFinder.builder()
				.setSearchRoots(Roots.builder()
					.setIncludeClassesDir(false)
					.setIncludeTestDir(true)
				)
				.setFilter(Filter.builder()
					//.addIncludeTypes(AType.withFullName("*Test"))
					.addIncludeMethods(withMethodAnnotation(Test.class))
				)
				.build()
				.findMethods();

		Collection<String> failures = Lists.newArrayList();
		
		for (JMethod method : methods) {
			if( !method.getName().startsWith("test") && !method.getName().endsWith("Test") ){
				String msg = String.format("Expected test method %s.%s to start with 'test' or end with 'Test'", method.getJType().getFullName(), method.getName());
				failures.add(msg);
			}
		}
		if( failures.size() > 0){
			Assert.fail("\n" + Joiner.on("\n").join(failures));
		}
	}
	
}
