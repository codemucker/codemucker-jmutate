package com.bertvanbrakel.codemucker.example;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.Filter;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder.JFindListener;
import com.bertvanbrakel.codemucker.ast.finder.SearchPath;
import com.bertvanbrakel.codemucker.ast.matcher.AMethod;
import com.bertvanbrakel.codemucker.ast.matcher.AType;
import com.google.common.collect.Lists;

public class EnforceBuilderNamingTest 
{
	@Test
	public void testEnsureBuildersAreCorrectlyNamed()
	{
		Iterable<JType> builders = JSourceFinder.newBuilder()
				.setSearchPaths(SearchPath.newBuilder()
					.setIncludeClassesDir(true)
					.setIncludeTestDir(true)
				)
				.setFilter(Filter.newBuilder()
					.addIncludeTypes(AType.withSimpleName("*Builder"))
					.addExcludeTypes(AType.isAbstract())
				)
				.setListener(new JFindListener() {
					@Override
					public void onMatched(Object obj) {
						System.out.println("matched:" + obj);
					}
					
					@Override
					public void onIgnored(Object obj) {	
					}
				})
				.build()
				.findTypes();
		
		List<String> ignoreMethodsNamed = Lists.newArrayList("build","newBuilder","copyOf");
		
		for (JType builder : builders) {			
			FindResult<JMethod> buildMethod = builder.findMethodsMatching(AMethod.withMethodNamed("build"));
			Assert.assertFalse("expect to find a build method on " + builder.getFullName(), buildMethod.isEmpty());
			//TODO:check return type of builder
			//TODO:check no args
			//TODO:check annotated?
			
			String builderTypeName = builder.getSimpleName();
			
			System.out.println("builder: " + builder.getFullName());
			
			for( JMethod method : builder.findAllJMethods().filter(AMethod.all(AMethod.withAccess(JAccess.PUBLIC),AMethod.isNotConstructor()))){
				System.out.println("method: " + method.getAstNode().getReturnType2() + " " +  method.toClashDetectionSignature());
				
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
									method.toClashDetectionSignature(),
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
		Iterable<JMethod> methods = JSourceFinder.newBuilder()
				.setSearchPaths(SearchPath.newBuilder()
					.setIncludeClassesDir(false)
					.setIncludeTestDir(true)
				)
				.setFilter(Filter.newBuilder()
					//.addIncludeTypes(AType.withFullName("*Test"))
					.addIncludeMethods(AMethod.withMethodAnnotation(Test.class))
				)
				.build()
				.findMethods();
		for (JMethod method : methods) {
			if( !method.getName().startsWith("test") ){
				String msg = String.format("Expected test method %s.%s to start with 'test'", method.getJType().getFullName(), method.getName());
				Assert.fail(msg);
			}
		}
	}
	
}
