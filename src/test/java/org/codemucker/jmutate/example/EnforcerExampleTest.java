package org.codemucker.jmutate.example;

import static org.codemucker.jmatch.Logical.not;

import java.util.ArrayList;
import java.util.List;

import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Roots;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceFinder;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.lang.IBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;

import static org.codemucker.jmatch.Logical.*;

public class EnforcerExampleTest 
{
	@Test
	public void ensureMatcherBuildersAreCorrectlyNamed()
	{
		FindResult<JType> matchers = SourceFinder.builder()
			.setSearchRoots(Roots.builder()
				.setIncludeMainSrcDir(true)
				.setIncludeTestSrcDir(false))
			.setFilter(SourceFilter.builder()
				.addInclude(AJType.that().isASubclassOf(ObjectMatcher.class).isAbstract(false)))
			.build()
			.findTypes();
		
		List<String> failMsgs = new ArrayList<>();
		
		for(JType t : matchers){
			FindResult<JMethod> methods = t.findMethodsMatching(AJMethod.with()
				.access(JAccess.PUBLIC)
				.name(not(AString.equalToAny("with", "that", "any", "none", "all"))));

			for(JMethod m : methods){
				if(m.getModifiers().isStatic()){
					failMsgs.add("FAILED: " + t.getFullName() + "." + m.getFullSignature() + " ==>isStatic");
				}
				if(AString.startingWithIgnoreCase("with").matches(m.getName())){
					failMsgs.add("FAILED: " + t.getFullName() + "." + m.getFullSignature() + " ==>starts with 'with'");
				}
			}
		}
		if(failMsgs.size() > 0){
			Assert.fail(Joiner.on("\n").join(failMsgs));
		}
	}
	
	@Test
	public void ensureBuildersAreCorrectlyNamed()
	{
		FindResult<JType> builders = SourceFinder.builder()
				.setSearchRoots(Roots.builder()
					.setIncludeMainSrcDir(true)
					.setIncludeTestSrcDir(true))
				.setFilter(SourceFilter.builder()
					.addInclude(
						any(
							AJType.with()
								.simpleNameMatchesAntPattern("*Builder")
								.isAbstract(false),
							AJType.that()
								.isASubclassOf(IBuilder.class)
								.isAbstract(false),
							AJType.with()
								.method(AJMethod.with().name("build")))))
				.build()
				.findTypes();
		
		List<String> failMsgs = new ArrayList<>();
		
		for (JType builder : builders) {			
			FindResult<JMethod> buildMethod = builder.findMethodsMatching(AJMethod.with().nameMatchingAntPattern("build"));
			
			if(buildMethod.isEmpty()){
				failMsgs.add("FAIL: expect to find a build method on " + builder.getFullName());
				continue;
			} 
			
			String builderTypeName = builder.getSimpleName();
			FindResult<JMethod> methods = builder.findMethodsMatching(AJMethod.with()
					.access(JAccess.PUBLIC)
					.isConstructor(false)
					.name(not(AString.equalToAny("build", "builder","copyOf")))
					.name(not(AString.withAntPattern("build*"))));
			
			for(JMethod method : methods){
				
				//ensure method name down's start with bad prefix
				if( method.getName().startsWith("get") || method.getName().startsWith("set") || method.getName().startsWith("with") ) {
					String msg = String.format("FAIL: expected builder method at %s.%s to _not_ start with any of 'get,set,with'", method.getJType().getFullName(), method.getFullSignature());
					failMsgs.add(msg);
				}
				
				//ensure return type is builder
				if( !method.getAstNode().getReturnType2().toString().equals(builderTypeName)){
					String msg = String.format("FAIL : expected builder method at %s.%s to return the enclosing builder '%s' but got '%s' for \n\n method \n%s\n in parent \n%s ", 
						method.getJType().getFullName(),
						method.getFullSignature(),
						builderTypeName,
						method.getAstNode().getReturnType2().toString(),
						method.getAstNode(),
						method.getType());
					failMsgs.add(msg);
				}
				
				//TODO:all builder methods taking a boolean, should be named isX(..) or hasX(..)
			}
		}	
		if(failMsgs.size() > 0){
			Assert.fail(Joiner.on("\n").join(failMsgs));
		}
	}
}
