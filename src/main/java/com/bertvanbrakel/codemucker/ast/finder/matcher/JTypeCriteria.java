package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.lang.annotation.Annotation;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.finder.IncludeExcludeMatcher;
import com.bertvanbrakel.codemucker.ast.finder.IncludeExcludeMatcher.IncludeMode;

public class JTypeCriteria implements JTypeMatcher {

	private final IncludeExcludeMatcher<JType> matchers = new IncludeExcludeMatcher<JType>(IncludeMode.ALL);

	public static JTypeCriteria all(JTypeMatcher... matchers) {
		return new JTypeCriteria().andAll(matchers);
	}

	public static JTypeCriteria any(JTypeMatcher... matchers) {
		return new JTypeCriteria().andAny(matchers);
	}

	public JTypeCriteria withAccess(JAccess access) {
		matchers.addInclude(JTypeMatchers.withAccess(access));
		return this;
	}

	public JTypeCriteria withName(String regexp) {
		matchers.addInclude(JTypeMatchers.withName(regexp));
		return this;
	}

	public JTypeCriteria inPackage(String pkg) {
		matchers.addInclude(JTypeMatchers.inPackage(pkg));
		return this;
	}

	public <A extends Annotation> JTypeCriteria withAnnotation(Class<A> annotationClass) {
		matchers.addInclude(JTypeMatchers.withAnnotation(annotationClass));
		return this;
	}

	public JTypeCriteria assignableFrom(Class<?> superClassOrInterface) {
		matchers.addInclude(JTypeMatchers.assignableFrom(superClassOrInterface));
		return this;
	}

	public JTypeCriteria andAll(JTypeMatcher... matchers) {
		this.matchers.addIncludeAll(matchers);
		return this;
	}

	public JTypeCriteria andAny(JTypeMatcher... matchers) {
		this.matchers.addInclude(JTypeMatchers.any(matchers));
		return this;
	}

	@Override
	public boolean matches(JType found) {
		return matchers.matches(found);
	}
}