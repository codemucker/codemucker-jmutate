package org.codemucker.jmutate.generate.model.pojo;

import java.lang.reflect.Method;

import org.codemucker.jfind.ReflectedClass;
import org.codemucker.jfind.matcher.AClass;
import org.codemucker.jfind.matcher.AMethod;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jpattern.bean.CloneMethod;

public class CloneMethodExtractor {

	private static final Matcher<JMethod> MATCHER_SOURCE = 
		Logical.any(
			AJMethod.with().annotation(CloneMethod.class).isPublic().numArgs(0).isNotVoidReturn(),
			AJMethod.with().name("clone").isPublic().numArgs(0).isNotVoidReturn().declaringType(AJType.that().isASubclassOf(Cloneable.class)));
	
	
	private static final Matcher<Method> MATCHER_COMPILED  =
		Logical.any(
			AMethod.with().annotation(CloneMethod.class).isPublic().numArgs(0).isNotVoidReturn(),
			AMethod.with().name("clone").isPublic().numArgs(0).isNotVoidReturn().declaringType(AClass.that().isASubclassOf(Cloneable.class)));
	
	
	public JMethod extractCloneMethodOrNull(JType type){
		return type.findMethodsMatching(MATCHER_SOURCE).getFirstOrNull();
	}
	
	public Method extractCloneMethodOrNull(Class<?> type){
		return extractCloneMethodOrNull(ReflectedClass.from(type));
	}
	
	public Method extractCloneMethodOrNull(ReflectedClass type){
		return type.findMethodsMatching(MATCHER_COMPILED).getFirstOrNull();
	}
}
