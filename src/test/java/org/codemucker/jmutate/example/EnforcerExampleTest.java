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
import org.codemucker.jmutate.ast.JAstMatcher;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJSourceFile;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.ast.matcher.AType;
import org.codemucker.jmutate.transform.JFieldBuilder;
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
		FindResult<JType> matchers = SourceFinder.with()
			.searchRoots(Roots.with()
				.mainSrcDir(true)
				.testSrcDir(false))
			.filter(SourceFilter.with()
				.includeType(AJType.that().isASubclassOf(ObjectMatcher.class).isAbstract(false)))
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
		FindResult<JType> builders = SourceFinder.with()
				.searchRoots(Roots.with()
					.mainSrcDir(true)
					.testSrcDir(true))
				.filter(SourceFilter.with()
					.excludeSource(AJSourceFile.with().name(JAstMatcher.class))
					.excludeType(AJType.with().packageName(JFieldBuilder.class))
					.includeType(AJType.that()
						.isAbstract(false)
						.simpleNameMatchesAntPattern("*Builder"))
					.includeType(AJType.that()
						.isAbstract(false)
						.isASubclassOf(IBuilder.class))
					.includeType(AJType.with()
						.method(AJMethod.with().name("build"))))
				.build()
				.findTypes();
		
		List<String> failMsgs = new ArrayList<>();
		
		for (JType builder : builders) {			
			FindResult<JMethod> buildMethod = builder.findMethodsMatching(AJMethod.with().nameMatchingAntPattern("build"));
			
			if(buildMethod.isEmpty()){
				failMsgs.add("FAIL: expect to find a build method on " + builder.getFullName());
				continue;
			} 
			
			JType parent = builder.getParentJType();
			if(parent != null && !parent.hasMethodsMatching(AJMethod.with().name("with").numArgs(0).isStatic(true).returning(AType.that().isEqualTo(builder)))){
				failMsgs.add("FAIL: expect to find a static builder factory method named 'with' on " + parent.getFullName());
			}
//			if(parent != null && !parent.hasMethodsMatching(AJMethod.with().name("that").numArgs(0).isStatic(true).returning(AType.that().isEqualTo(builder)))){
//				failMsgs.add("FAIL: expect to find a static builder factory method named 'that' on " + parent.getFullName());
//			}
			
			String builderTypeName = builder.getSimpleName();
			FindResult<JMethod> methods = builder.findMethodsMatching(AJMethod.with()
					.access(JAccess.PUBLIC)
					.isConstructor(false)
					.name(not(AString.equalToAny("build","copyOf")))
					.name(not(AString.withAntPattern("build*"))));
			
			for(JMethod method : methods){
				
				//ensure method name down's start with bad prefix
				if( method.getName().startsWith("get") || method.getName().startsWith("set") || method.getName().startsWith("with") ) {
					String msg = String.format("FAIL: expected builder method %s.%s to _not_ start with any of 'get,set,with'", method.getEnclosingJType().getFullName(), method.getFullSignature());
					failMsgs.add(msg);
				}
				
				//ensure return type is builder
				if( !method.getAstNode().getReturnType2().toString().equals(builderTypeName)){
					String msg = String.format("FAIL : expected builder method %s.%s to return the enclosing builder '%s' but got '%s' for \n\n method \n%s\n in parent \n%s ", 
						method.getEnclosingJType().getFullName(),
						method.getFullSignature(),
						builderTypeName,
						method.getAstNode().getReturnType2().toString(),
						method.getAstNode(),
						method.getEnclosingType());
					failMsgs.add(msg);
				}
				
				//TODO:all builder methods taking a boolean, should be named isX(..) or hasX(..)
			}
		}	
		if(failMsgs.size() > 0){
			Assert.fail(failMsgs.size() + " failures\n" + Joiner.on("\n").join(failMsgs));
		}
	}
}
