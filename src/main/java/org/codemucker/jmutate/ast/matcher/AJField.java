package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractMatcher;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JModifier;
import org.eclipse.jdt.core.dom.Type;

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
	
	public AJField(){
	    super(JField.class);
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
			
			@Override
			public void describeTo(Description desc) {
			    desc.value("field type", typeMatcher);
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
				desc.value("name", nameMatcher);
			}
		});
		return this;
	}
	
    public <A extends Annotation> AJField annotation(final Class<A> annotationClass) {
        annotation(AJAnnotation.with().fullName(annotationClass));
        return this;
    }

    public AJField annotation(final Matcher<JAnnotation> matcher) {
        addMatcher(new AbstractNotNullMatcher<JField>() {
            @Override
            public boolean matchesSafely(JField found, MatchDiagnostics diag) {
                return found.getAnnotations().contains(matcher);
            }

            @Override
            public void describeTo(Description desc) {
                // super.describeTo(desc);
                desc.value("with annotation", matcher);
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
			
			@Override
			public void describeTo(Description desc) {
			    desc.text("access " + access.name());
			}
		});
		return this;
	}
	
	public AJField modifiers(final Matcher<JModifier> matcher){
        addMatcher(new AbstractNotNullMatcher<JField>() {
            @Override
            public boolean matchesSafely(JField found, MatchDiagnostics diag) {
                return diag.tryMatch(this, found.getJavaModifiers(), matcher);
            }
            
            @Override
            public void describeTo(Description desc) {
                desc.value("modifer ", matcher);
            }
        });
        return this;
    }
}
