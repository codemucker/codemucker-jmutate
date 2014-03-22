package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.match.AString;
import org.codemucker.match.AbstractMatcher;
import org.codemucker.match.AbstractNotNullMatcher;
import org.codemucker.match.Description;
import org.codemucker.match.MatchDiagnostics;
import org.codemucker.match.Matcher;
import org.codemucker.match.AbstractMatcher.AllowNulls;
import org.eclipse.jdt.core.dom.Type;

public class AJField {// extends Logical  {

	private static final Matcher<JField> MATCH_ANY  = new AbstractNotNullMatcher<JField>() {
		@Override
		public boolean matchesSafely(JField found, MatchDiagnostics diag) {
			return true;
		}
	};
	
	@SuppressWarnings("unchecked")
    public static Matcher<JField> any(){
		return MATCH_ANY;
	}
	
//	public static Matcher<JField> withFQN(final Matcher<String> fqnMatcher){
//		return new Matcher<JField>() {
//			@Override
//			public boolean matchesSafely(JField found) {
//				return fqnMatcher.matches(found.getAstNode().getType());
//			}
//		};
//	}
//	
	public static Matcher<JField> ofType(final Matcher<Type> typeMatcher){
		return new AbstractNotNullMatcher<JField>() {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				return typeMatcher.matches(found.getAstNode().getType());
			}
		};
	}
	
	public static Matcher<JField> withName(final String antPattern){
		return new AbstractMatcher<JField>(AllowNulls.NO) {
			private final Matcher<String> nameMatcher = AString.withAntPattern(antPattern);		
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				for (String name : found.getNames()) {
					if (nameMatcher.matches(name)) {
						return true;
					}
				}
				return false;
			}
			
			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.text("a JField");
				desc.value("name", nameMatcher);
			}
		};
	}
	
	public static <A extends Annotation> Matcher<JField> withAnnotation(final Class<A> annotationClass){
		return new AbstractNotNullMatcher<JField>() {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				return found.hasAnnotationOfType(annotationClass);
			}
		};
	}

	public static  Matcher<JField> hasAccess(final JAccess access){
		return new AbstractNotNullMatcher<JField>() {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				return found.isAccess(access);
			}
		};
	}
	
}
