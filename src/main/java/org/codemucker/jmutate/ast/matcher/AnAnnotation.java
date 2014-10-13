package org.codemucker.jmutate.ast.matcher;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.eclipse.jdt.core.dom.Annotation;

public class AnAnnotation { 
    
    public static <A extends java.lang.annotation.Annotation> Matcher<Annotation> notWithFqdn(final Class<A> anotationClass){
        return Logical.not(withFqn(JavaNameUtil.compiledNameToSourceName(anotationClass)));
    }
    
	public static <A extends java.lang.annotation.Annotation> Matcher<Annotation> withFqn(final Class<A> annotationClass){
		return withFqn(JavaNameUtil.compiledNameToSourceName(annotationClass));
	}
	
	public static Matcher<Annotation> withFqn(final String name){
	    return withFqn(AString.equalTo(name));
	}
	
	public static Matcher<Annotation> notWithFqn(final Matcher<String> matcher){
	    return Logical.not(withFqn(matcher));
	}
	
    public static Matcher<Annotation> withFqn(final Matcher<String> matcher) {
        return new AbstractNotNullMatcher<Annotation>() {
            @Override
            public boolean matchesSafely(Annotation found, MatchDiagnostics diag) {
                return diag.tryMatch(this, JAnnotation.from(found).getQualifiedName(), matcher);
            }

            @Override
            public void describeTo(Description desc) {
                desc.value("fqn", matcher);
            }
        };
    }
}
