package org.codemucker.jmutate.example;

import static org.codemucker.jmatch.Logical.not;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.codemucker.jfind.AMethod;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.JClass;
import org.codemucker.jfind.MatchListener;
import org.codemucker.jfind.Roots;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceFinder;
import org.codemucker.jmutate.SourceHelper;
import org.codemucker.jmutate.ast.BaseASTVisitor;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJSourceFile;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.ast.matcher.AType;
import org.codemucker.lang.IBuilder;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

public class EnforcerExampleTest 
{
	@Test
	public void baseASTVisitorOverridesAllParentVisitMethods(){
		JType baseVisitor = SourceHelper.findSourceForClass(BaseASTVisitor.class).getMainType();
		List<String> haveMethodSigs = baseVisitor
				.findMethodsMatching(AJMethod.with().name(AString.equalToAny("visit", "endVisit")))
				.transform(new Function<JMethod, String>() {
						@Override
						public String apply(JMethod m) {		
							return m.getClashDetectionSignature();
						}
					})	
				.toList();
		
		JClass astVisitor = new JClass(ASTVisitor.class);
		List<String> expectMethodSigs = astVisitor
				.findMethodsMatching(AMethod.with().name(AString.equalToAny("visit", "endVisit")))
				.transform(new Function<Method, String>() {
					@Override
					public String apply(Method m) {
						StringBuilder sb = new StringBuilder();
						for(Class<?> t : m.getParameterTypes()){
							if(sb.length() > 0){
								sb.append(',');
							}
							sb.append(t.getName());
						}
						return m.getName() + "(" + sb.toString() + ")";
					}
				})
				.toList();
		expectMethodSigs.removeAll(haveMethodSigs);
		
		if(expectMethodSigs.size() > 0){
			Assert.fail("expected " + BaseASTVisitor.class.getName() + " to override [" +  Joiner.on(',').join(expectMethodSigs) + "]");
		}
	}
	
	@Test
	public void ensureMatcherBuildersAreCorrectlyNamed()
	{
		FindResult<JType> matchers = SourceFinder.with()
			.searchRoots(Roots.with().mainSrcDir(true))
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
				.listener(new MatchListener<Object>() {
					@Override
					public void onMatched(Object result) {
					}
					
					@Override
					public void onIgnored(Object result) {
					}
					
					@Override
					public void onError(Object record, Exception e) throws Exception {
					}
				})
				.filter(SourceFilter.with()
					.includeSource(AJSourceFile.with().typeFullName("**AbstractBuilder"))
					.includeType(AJType.with().simpleNameMatchingAntPattern("*Builder"))
					.includeType(AJType.that().isASubclassOf(IBuilder.class))
					.includeType(AJType.with().method(AJMethod.with().nameMatchingAntPattern("build*"))))
				.build()
				.findTypes();
		
		List<String> failMsgs = new ArrayList<>();
		
		for (JType builder : builders) {			
			FindResult<JMethod> buildMethod = builder.findMethodsMatching(AJMethod.with().nameMatchingAntPattern("build"));
			
			if(!builder.isAbstract()){//don't expect build methods on abstract builders
				if(buildMethod.isEmpty()){
					failMsgs.add("FAIL: expect to find a build method on " + builder.getFullName());
					continue;
				} 
				
				JType parent = builder.getParentJType();
				if(parent != null && !parent.hasMethodsMatching(AJMethod.with().name("with").numArgs(0).isStatic(true).returning(AType.that().isEqualTo(builder)))){
					failMsgs.add("FAIL: expect to find a static builder factory method named 'with' on " + parent.getFullName());
				}
			}
			String builderTypeName = builder.getSimpleName();
			String builderTypeFullName = builder.getFullName();
			
			FindResult<JMethod> builderMethods = builder.findMethodsMatching(
					AJMethod.all(
							AJMethod.with()
								.access(JAccess.PUBLIC)
								.isConstructor(false)
					    	,AJMethod.with()
								.name(not(AString.equalToAny("build","copyOf")))
								.name(not(AString.matchingAntPattern("build*")))
							,not(AJMethod.with()
								.name(AString.matchingAntPattern("get*"))
								.numArgs(0))
							,not(AJMethod.with()
								.name(AString.matchingAntPattern("is??*"))
								.numArgs(0)
								.returning(AType.BOOL_PRIMITIVE))));
			
			String builderSelfTypeName = builder.getSelfTypeGenericParam();
			
			for(JMethod method : builderMethods){
				
				//ensure method names don't start with bad prefixes
				if( method.getName().startsWith("set") || (method.getName().startsWith("with") && !(method.getName().equals("with") && method.isStatic()))) {
					String msg = String.format("FAIL: expected builder method %s.%s to _not_ start with any of 'set,with'", method.getEnclosingJType().getFullName(), method.getFullSignature());
					failMsgs.add(msg);
				}
				
				//ensure return type is builder. Slightly tricky in that the builder could return 'Self' which could
				//be a generic type so that it can be sub classed
				String returnType = method.getReturnTypeFullName();
				if( !returnType.equals(builderTypeFullName)){
					
					
					if(builderSelfTypeName != null && builderSelfTypeName.equals(returnType)){
						continue;
					}
					
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
