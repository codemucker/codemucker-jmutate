package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JModifiers;
import org.codemucker.jmutate.ast.JType;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.base.Predicate;


public class AJMethod extends ObjectMatcher<JMethod> {
	
	/**
	 * synonym for with()
	 * 
	 * @return
	 */
	public static AJMethod that(){
		return with();
	}
	
	public static AJMethod with(){
		return new AJMethod();
	}

	public AJMethod method(Predicate<JMethod> predicate){
		predicate(predicate);
		return this;
	}

	/**
	 * Return a matcher which matches using the given ant style method name expression
	 * @param antPattern ant style pattern. E.g. *foo*bar??Ho
	 * @return
	 */
	public AJMethod nameMatchingAntPattern(final String antPattern) {
		name(AString.matchingAntPattern(antPattern));
		return this;
	}
	
	public AJMethod name(final String name) {
		name(AString.equalTo(name));
		return this;
	}
	
	public AJMethod name(final Matcher<String> matcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return matcher.matches(found.getName());
			}
		});
		return this;
	}
	
	public AJMethod returning(final Matcher<Type> matcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				Type t = found.getAstNode().getReturnType2();
				return diag.TryMatch(t, matcher);
			}
		});
		return this;
	}
	
	@SafeVarargs
	public static Matcher<JMethod> all(final Matcher<JMethod>... matchers) {
    	return Logical.and(matchers);
    }
	
	public static Matcher<JMethod> any() {
		return Logical.any();
	}
	
	public static Matcher<JMethod> none() {
		return Logical.none();
	}

	public AJMethod isNotConstructor() {
		isConstructor(false);
		return this;
	}

	public AJMethod isConstructor() {
		isConstructor(true);
		return this;
	}
	
	public AJMethod isConstructor(boolean val) {
		Matcher<JMethod> matcher = new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.isConstructor();
			}
		};
		if(!val ){
			matcher = Logical.not(matcher);
		}
		addMatcher(matcher);
		return this;
	}
	
	public AJMethod access(final JAccess access) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.getModifiers().isAccess(access);
			}
		});
		return this;
	}

	public AJMethod isStatic() {
		return isStatic(true);
	}
	
	public AJMethod isStatic(final boolean b) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.getModifiers().isStatic(b);
			}
		});
		return this;
	}
	
	public AJMethod modifier(final Matcher<JModifiers> matcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return diag.TryMatch(found.getModifiers(), matcher);
			}
		});
		return this;
	}
	
	public <A extends Annotation> AJMethod methodAnnotation(final Class<A> annotationClass) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.hasAnnotationOfType(annotationClass);
			}
		});
		return this;
	}

	public <A extends Annotation> AJMethod parameterAnnotation(final Class<A> annotationClass) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.hasParameterAnnotationOfType(annotationClass);
			}
		});
		return this;
	}

	public AJMethod numArgs(final int numArgs) {
		numArgs(AnInt.equalTo(numArgs));
		return this;
	}

	public AJMethod numArgs(final Matcher<Integer> numArgMatcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return numArgMatcher.matches(found.getAstNode().parameters().size());
			}
		});
		return this;
	}

	public AJMethod nameAndArgSignature(JMethod method) {
		final String name = method.getName();
		final int numArgs = method.getAstNode().typeParameters().size();
		final String sig = method.getClashDetectionSignature();

		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				//test using the quickest and least resource intensive matches first
				return numArgs == found.getAstNode().typeParameters().size() 
					&& name.equals(found.getName()) 
					&& sig.equals(found.getClashDetectionSignature());
			}
		});
		return this;
	}
}
