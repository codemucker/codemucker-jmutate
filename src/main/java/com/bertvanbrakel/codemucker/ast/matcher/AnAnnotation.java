package com.bertvanbrakel.codemucker.ast.matcher;

import org.eclipse.jdt.core.dom.Annotation;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.bertvanbrakel.codemucker.ast.JAnnotation;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;

public class AnAnnotation extends LogicalMatchers {
    
	public static Matcher<Annotation> withFqn(final Class<? extends java.lang.annotation.Annotation> klass){
		return withFqn(klass.getName());
	}
	
	public static Matcher<Annotation> withFqn(final String name){
		return new TypeSafeMatcher<Annotation>(Annotation.class){
			@Override
            public void describeTo(Description desc) {
				desc.appendText("fqn '" + name + "'");
            }

			@Override
			public boolean matchesSafely(Annotation found) {
				return name.equals(JAnnotation.from(found).getQualifiedName());
			}
		};
	}
}
