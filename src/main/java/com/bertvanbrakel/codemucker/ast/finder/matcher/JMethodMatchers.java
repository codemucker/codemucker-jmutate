package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.bertvanbrakel.test.util.TestUtils;

public class JMethodMatchers extends LogicalMatchers {

	/**
	 * Return a matcher which matches using the given ant style method name expression
	 * @param antPattern ant style pattern. E.g. *foo*bar??Ho
	 * @return
	 */
	public static Matcher<JMethod> withMethodNamed(final String antPattern) {
		return new Matcher<JMethod>() {
			private final Pattern pattern = TestUtils.antExpToPattern(antPattern);

			@Override
			public boolean matches(JMethod found) {
				return pattern.matcher(found.getName()).matches();
			}
		};
	}

	@SuppressWarnings("unchecked")
    public static Matcher<JMethod> any() {
		return LogicalMatchers.any();
	}
	
	@SuppressWarnings("unchecked")
    public static Matcher<JMethod> none() {
		return LogicalMatchers.none();
	}

	public static Matcher<JMethod> isConstructor() {
		return new Matcher<JMethod>() {
			@Override
			public boolean matches(JMethod found) {
				return found.isConstructor();
			}
		};
	}
	
	public static Matcher<JMethod> withAccess(final JAccess access) {
		return new Matcher<JMethod>() {
			@Override
			public boolean matches(JMethod found) {
				return found.getJavaModifiers().isAccess(access);
			}
		};
	}

	public static <A extends Annotation> Matcher<JMethod> withMethodLevelAnnotation(final Class<A> annotationClass) {
		return new Matcher<JMethod>() {
			@Override
			public boolean matches(JMethod found) {
				return found.hasAnnotationOfType(annotationClass);
			}
		};
	}

	public static <A extends Annotation> Matcher<JMethod> withParameterAnnotation(final Class<A> annotationClass) {
		return new Matcher<JMethod>() {
			@Override
			public boolean matches(JMethod found) {
				return found.hasParameterAnnotationOfType(annotationClass);
			}
		};
	}

	public static Matcher<JMethod> withNumArgs(final int numArgs) {
		return withNumArgs(IntegerMatchers.equalTo(numArgs));
	}

	public static Matcher<JMethod> withNumArgs(final Matcher<Integer> numArgMatcher) {
		return new Matcher<JMethod>() {
			@Override
			public boolean matches(JMethod found) {
				return numArgMatcher.matches(Integer.valueOf(found.getAstNode().typeParameters().size()));
			}
		};
	}

	public static Matcher<JMethod> withNameAndArgSignature(JMethod method) {
		final String name = method.getName();
		final int numArgs = method.getAstNode().typeParameters().size();
		final String sig = method.toClashDetectionSignature();

		return new Matcher<JMethod>() {
			@Override
			public boolean matches(JMethod found) {
				//test using the quickest and least resource intensive matches first
				return numArgs == found.getAstNode().typeParameters().size() 
					&& name.equals(found.getName()) 
					&& sig.equals(found.toClashDetectionSignature());
			}
		};
	}
	
}
