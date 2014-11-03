package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;

public class AJAnnotation extends ObjectMatcher<JAnnotation>{ 
    
    public AJAnnotation() {
        super(JAnnotation.class);
    }

    public static AJAnnotation with(){
        return new AJAnnotation();
    }

    /**
     * This annotation is itself marked with the given annotation
     */
    public AJAnnotation annotatedWith(final Matcher<Annotation> anonMatcher){
        addMatcher(new AbstractNotNullMatcher<JAnnotation>() {
            //let's not needlessly recheck classes we've already tested
            private Map<String,Boolean> cachedClassNameToMatch = new HashMap<>();
            private ClassLoader classLoader = MutateUtil.getClassLoaderForResolving();
            
            @Override
            public boolean matchesSafely(JAnnotation found, MatchDiagnostics diag) {
                String fullCompiledName = NameUtil.sourceNameToCompiledName(found.getQualifiedName());
                Boolean isMatch = cachedClassNameToMatch.get(fullCompiledName);
                if (isMatch != null && isMatch == false) {
                    return false;
                }
                try {
                    Class<?> annotationClass = classLoader.loadClass(fullCompiledName);
                    for (Annotation a : annotationClass.getAnnotations()) {
                        System.out.println("annotation " + a.getClass().getName()  + " on " + fullCompiledName + " loaded");
                        
                        if (diag.tryMatch(this, a, anonMatcher)) {    
                            cachedClassNameToMatch.put(fullCompiledName, true);
                            return true;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("couldn't load annotation " + fullCompiledName + ",ignoring");
                    e.printStackTrace();
                    diag.mismatched("couldn't load annotation " + fullCompiledName + ",ignoring");
                }
                cachedClassNameToMatch.put(fullCompiledName, false);
                return false;
            }

            @Override
            public void describeTo(Description desc) {
                desc.value("is marked with", anonMatcher);
            }
        });
                
        return this;
    }
    
    public <A extends java.lang.annotation.Annotation> AJAnnotation notFullName(final Class<A> annotationClass){
        String name = NameUtil.compiledNameToSourceName(annotationClass);
        fullName(Logical.not(AString.equalTo(name)));
        return this;
    }
    
	public <A extends java.lang.annotation.Annotation> AJAnnotation fullName(final Class<A> annotationClass){
	    String fullName = NameUtil.compiledNameToSourceName(annotationClass);
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
