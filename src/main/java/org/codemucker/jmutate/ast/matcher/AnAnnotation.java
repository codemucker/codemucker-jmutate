package org.codemucker.jmutate.ast.matcher;

import org.codemucker.jmutate.ast.JAnnotation;
import org.eclipse.jdt.core.dom.Annotation;

import com.bertvanbrakel.lang.matcher.AbstractNotNullMatcher;
import com.bertvanbrakel.lang.matcher.Description;
import com.bertvanbrakel.lang.matcher.Logical;
import com.bertvanbrakel.lang.matcher.Matcher;

public class AnAnnotation extends Logical {
    
	public static Matcher<Annotation> withFqn(final Class<? extends java.lang.annotation.Annotation> klass){
		return withFqn(compiledNameToSourceName(klass));
	}
	
	private static String compiledNameToSourceName(Class<?> klass){
		return klass.getName().replace('$', '.');
	}
	
	public static Matcher<Annotation> withFqn(final String name){
		return new AbstractNotNullMatcher<Annotation>(){
			
			@Override
            public void describeTo(Description desc) {
				desc.text("fqn '" + name + "'");
            }

			@Override
			public boolean matchesSafely(Annotation found) {
				return name.equals(JAnnotation.from(found).getQualifiedName());
			}
		};
	}
}
