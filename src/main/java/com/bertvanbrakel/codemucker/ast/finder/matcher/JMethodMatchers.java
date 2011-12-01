package com.bertvanbrakel.codemucker.ast.finder.matcher;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.test.util.TestUtils;

public class JMethodMatchers {

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

	public static JMethodMatcher withNumArgs(final IntMatcher intMatcher) {
		return new JMethodMatcher() {
			@Override
			public boolean matches(JMethod found) {
				return intMatcher.matches(Integer.valueOf(found.getMethodNode().typeParameters().size()));
			}
		};
	}

	public static IntMatcher equalTo(final int require) {
		return new IntMatcher() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() == require;
			}
		};
	}

	public static IntMatcher greaterThan(final int require) {
		return new IntMatcher() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() > require;
			}
		};
	}

	public static IntMatcher greaterOrEqualTo(final int require) {
		return new IntMatcher() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() >= require;
			}
		};
	}

	public static IntMatcher lessThan(final int require) {
		return new IntMatcher() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() > require;
			}
		};
	}

	public static IntMatcher lessOrEqualTo(final int require) {
		return new IntMatcher() {
			@Override
			public boolean matches(Integer found) {
				return found.intValue() <= require;
			}
		};
	}

	public static IntMatcher inRange(final int from, final int to) {
		checkArgument(from >= 0, "Expect 'from' to be >= 0");
		checkArgument(to >= 0, "Expect 'to' to be >= 0");
		checkArgument(from <= to, "Expect 'from' to be <= 'to'");

		return new IntMatcher() {
			@Override
			public boolean matches(Integer found) {
				int val = found.intValue();
				return val >= from && val <= to;
			}
		};
	}
}
