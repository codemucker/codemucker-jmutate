package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.lang.annotation.Annotation;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.finder.IncludeExcludeMatcher;
import com.bertvanbrakel.codemucker.ast.finder.IncludeExcludeMatcher.IncludeMode;

public class JMethodCriteria implements JMethodMatcher {

	private final IncludeExcludeMatcher<JMethod> matchers = new IncludeExcludeMatcher<JMethod>(IncludeMode.ALL);

	@Override
	public boolean matches(JMethod found) {
		return matchers.matches(found);
	}

	public JMethodCriteria withName(final String antPattern) {
		matchers.addInclude(JMethodMatchers.withName(antPattern));
		return this;
	}

	public JMethodCriteria withAccess(final JAccess access) {
		matchers.addInclude(JMethodMatchers.withAccess(access));
		return this;
	}

	public <A extends Annotation> JMethodCriteria withMethodLevelAnnotation(final Class<A> annotationClass) {
		matchers.addInclude(JMethodMatchers.withMethodLevelAnnotation(annotationClass));
		return this;
	}

	public <A extends Annotation> JMethodCriteria withParameterAnnotation(final Class<A> annotationClass) {
		matchers.addInclude(JMethodMatchers.withParameterAnnotation(annotationClass));
		return this;
	}

	public JMethodCriteria withNumArgs(final Matcher<Integer> intMatcher) {
		matchers.addInclude(JMethodMatchers.withNumArgs(intMatcher));
		return this;
	}

	public JMethodCriteria withNumArgs(final int numArgs) {
		matchers.addInclude(JMethodMatchers.withNumArgs(numArgs));
		return this;
	}

}
