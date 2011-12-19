package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.test.util.TestUtils;

public class JMethodMatchers extends JMatchers {

	public static JMethodMatcher withName(final String antPattern) {
		return new JMethodMatcher() {
			private final Pattern pattern = TestUtils.antExpToPattern(antPattern);

			@Override
			public boolean matches(JMethod found) {
				return pattern.matcher(found.getName()).matches();
			}
		};
	}

	public static JMethodMatcher withAccess(final JAccess access) {
		return new JMethodMatcher() {
			@Override
			public boolean matches(JMethod found) {
				return found.getJavaModifiers().isAccess(access);
			}
		};
	}

	public static <A extends Annotation> JMethodMatcher withMethodLevelAnnotation(final Class<A> annotationClass) {
		return new JMethodMatcher() {
			@Override
			public boolean matches(JMethod found) {
				return found.hasAnnotationOfType(annotationClass);
			}
		};
	}

	public static <A extends Annotation> JMethodMatcher withParameterAnnotation(final Class<A> annotationClass) {
		return new JMethodMatcher() {
			@Override
			public boolean matches(JMethod found) {
				return found.hasParameterAnnotationOfType(annotationClass);
			}
		};
	}

	public static JMethodMatcher withNumArgs(final int numArgs) {
		return withNumArgs(equalTo(numArgs));
	}

	public static JMethodMatcher withNumArgs(final Matcher<Integer> numArgMatcher) {
		return new JMethodMatcher() {
			@Override
			public boolean matches(JMethod found) {
				return numArgMatcher.matches(Integer.valueOf(found.getMethodNode().typeParameters().size()));
			}
		};
	}

}
