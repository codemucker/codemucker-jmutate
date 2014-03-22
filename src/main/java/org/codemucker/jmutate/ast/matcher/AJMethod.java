package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JMethod;


public class AJMethod {

	/**
	 * Return a matcher which matches using the given ant style method name expression
	 * @param antPattern ant style pattern. E.g. *foo*bar??Ho
	 * @return
	 */
	public static Matcher<JMethod> withNameMatchingAntPattern(final String antPattern) {
		return withName(AString.withAntPattern(antPattern));
	}
	
	public static Matcher<JMethod> withName(final String name) {
		return withName(AString.equalTo(name));
	}
	
	public static Matcher<JMethod> withName(final Matcher<String> matcher) {
		return new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return matcher.matches(found.getName());
			}
		};
	}
	
	public static Matcher<JMethod> all(final Matcher<JMethod>... matchers) {
    	return Logical.and(matchers);
    }
	
	public static Matcher<JMethod> any() {
		return Logical.any();
	}
	
	public static Matcher<JMethod> none() {
		return Logical.none();
	}

	public static Matcher<JMethod> isNotConstructor() {
		return Logical.not(isConstructor());
	}
	
	public static Matcher<JMethod> isConstructor() {
		return new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.isConstructor();
			}
		};
	}
	
	public static Matcher<JMethod> withAccess(final JAccess access) {
		return new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.getJavaModifiers().isAccess(access);
			}
		};
	}

	public static <A extends Annotation> Matcher<JMethod> withMethodAnnotation(final Class<A> annotationClass) {
		return new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.hasAnnotationOfType(annotationClass);
			}
		};
	}

	public static <A extends Annotation> Matcher<JMethod> withParameterAnnotation(final Class<A> annotationClass) {
		return new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.hasParameterAnnotationOfType(annotationClass);
			}
		};
	}

	public static Matcher<JMethod> withNumArgs(final int numArgs) {
		return withNumArgs(AnInt.equalTo(numArgs));
	}

	public static Matcher<JMethod> withNumArgs(final Matcher<Integer> numArgMatcher) {
		return new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return numArgMatcher.matches(found.getAstNode().parameters().size());
			}
		};
	}

	public static Matcher<JMethod> withNameAndArgSignature(JMethod method) {
		final String name = method.getName();
		final int numArgs = method.getAstNode().typeParameters().size();
		final String sig = method.getClashDetectionSignature();

		return new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				//test using the quickest and least resource intensive matches first
				return numArgs == found.getAstNode().typeParameters().size() 
					&& name.equals(found.getName()) 
					&& sig.equals(found.getClashDetectionSignature());
			}
		};
	}
}
