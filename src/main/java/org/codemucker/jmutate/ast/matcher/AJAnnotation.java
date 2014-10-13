package org.codemucker.jmutate.ast.matcher;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.util.JavaNameUtil;

public class AJAnnotation extends ObjectMatcher<JAnnotation>{ 
    
    public AJAnnotation() {
        super(JAnnotation.class);
    }

    public static AJAnnotation with(){
        return new AJAnnotation();
    }
    
    public <A extends java.lang.annotation.Annotation> AJAnnotation notFullName(final Class<A> annotationClass){
        String name = JavaNameUtil.compiledNameToSourceName(annotationClass);
        fullName(Logical.not(AString.equalTo(name)));
        return this;
    }
    
	public <A extends java.lang.annotation.Annotation> AJAnnotation fullName(final Class<A> annotationClass){
	    String fullName = JavaNameUtil.compiledNameToSourceName(annotationClass);
	    fullName(fullName);
		return this;
	}
	
	public AJAnnotation fullName(final String name){
	    fullName(AString.equalTo(name));
	    return this;
	}
	
	public AJAnnotation notFullName(final Matcher<String> matcher){
	    fullName(Logical.not(matcher));
	    return this;
	}
	
    public AJAnnotation fullName(final Matcher<String> matcher) {
        addMatcher(new AbstractNotNullMatcher<JAnnotation>() {
            @Override
            public boolean matchesSafely(JAnnotation found, MatchDiagnostics diag) {
                return diag.tryMatch(this, found.getQualifiedName(), matcher);
            }

            @Override
            public void describeTo(Description desc) {
                desc.value("fqn", matcher);
            }
        });
        return this;
    }
}
