package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractMatcher;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.PredicateToMatcher;
import org.codemucker.jmatch.AbstractMatcher.AllowNulls;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;

import com.google.common.base.Predicate;

public class AJField extends ObjectMatcher<JField>{

	private static final Matcher<JField> MATCH_ANY  = new AbstractNotNullMatcher<JField>() {
		@Override
		public boolean matchesSafely(JField found, MatchDiagnostics diag) {
			return true;
		}
	};
	
	/**
	 * synonym for with()
	 * @return
	 */
	public static AJField that(){
		return with();
	}
	
	public static AJField with(){
		return new AJField();
	}

	public AJField field(Predicate<JField> predicate){
		predicate(predicate);
		return this;
	}

	public static Matcher<JField> any(){
		return MATCH_ANY;
	}

	public AJField ofType(final Matcher<Type> typeMatcher){
		addMatcher(new AbstractNotNullMatcher<JField>() {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				return typeMatcher.matches(found.getAstNode().getType());
			}
		});
		return this;
	}
	
	public AJField name(final String antPattern){
		addMatcher(new AbstractMatcher<JField>(AllowNulls.NO) {
			private final Matcher<String> nameMatcher = AString.matchingAntPattern(antPattern);		
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
		});
		return this;
	}
	
	public <A extends Annotation> AJField annotation(final Class<A> annotationClass){
		addMatcher(new AbstractNotNullMatcher<JField>() {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				return found.hasAnnotationOfType(annotationClass);
			}
		});
		return this;
	}

	public AJField access(final JAccess access){
		addMatcher(new AbstractNotNullMatcher<JField>() {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				return found.isAccess(access);
			}
		});
		return this;
	}
	
}
