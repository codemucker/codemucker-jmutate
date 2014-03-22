package org.codemucker.jmutate.ast.matcher;

import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.eclipse.jdt.core.dom.Annotation;

public class AnAnnotation { //extends Logical {
    
	public static Matcher<Annotation> withFqn(final Class<? extends java.lang.annotation.Annotation> klass){
		return withFqn(JavaNameUtil.compiledNameToSourceName(klass));
	}
	
	public static Matcher<Annotation> withFqn(final String name){
		return new AbstractNotNullMatcher<Annotation>(){
			
			@Override
            public void describeTo(Description desc) {
				desc.text("fqn '" + name + "'");
            }

			@Override
			public boolean matchesSafely(Annotation found,MatchDiagnostics diag) {
				return name.equals(JAnnotation.from(found).getQualifiedName());
			}
		};
	}
}
