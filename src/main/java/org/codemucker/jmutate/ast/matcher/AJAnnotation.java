package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.codemucker.jfind.RootResource;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ResourceLoader;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;

public class AJAnnotation extends ObjectMatcher<JAnnotation>{ 
    
    public AJAnnotation() {
        super(JAnnotation.class);
    }

    public static AJAnnotation with(){
        return new AJAnnotation();
    }

    public AJAnnotation annotationPresent(final Class<? extends Annotation> anon){
        annotationPresent(AString.equalTo(NameUtil.compiledNameToSourceName(anon)));
    	return this;
    }
    
    public AJAnnotation annotationPresent(final String fullName){
    	annotationPresent(AString.equalTo(NameUtil.compiledNameToSourceName(fullName)));
        return this;
    }
    
    /**
     * This annotation is itself marked with the given annotation
     */
    public AJAnnotation annotationPresent(final Matcher<String> nameMatcher){
        addMatcher(newMatcher(nameMatcher));
        return this;
    }
    
    private Matcher<JAnnotation> newMatcher(final Matcher<String> nameMatcher){
        return new AbstractNotNullMatcher<JAnnotation>() {
            //let's not needlessly recheck classes we've already tested
            private Map<String,Boolean> cachedClassNameToMatch = new HashMap<>();
            private ClassLoader classLoader = MutateUtil.getClassLoaderForResolving();
            
            private final Object lock = new Object();
            
            @Override
            public boolean matchesSafely(JAnnotation found, MatchDiagnostics diag) {
            	String name = found.getQualifiedName();
                
            	
            	//String fullCompiledName = NameUtil.sourceNameToCompiledName(found.getQualifiedName());
                Boolean isMatch = null;
                
                synchronized (lock) {
                	isMatch = cachedClassNameToMatch.get(name);
                }
                if (isMatch != null) {
                    return isMatch.booleanValue();
                }
                ResourceLoader resourceLoader = MutateUtil.getResourceLoader(found.getAstNode());
                RootResource resource = resourceLoader.getResourceOrNullFromClassName(name);
                
            	if(resource != null){
            		JAstParser parser = MutateUtil.getParser(found.getAstNode());
                    
            		JType type = JSourceFile.fromResource(resource, parser).getTypeWithFullName(name);
            		if(type.getAnnotations().contains(AJAnnotation.with().fullName(nameMatcher))){
            			cacheResult(name, true);
                        return true;
            		}
            		cacheResult(name, false);
                    return false;
            	}
                try {
                    Class<?> annotationClass = classLoader.loadClass(NameUtil.sourceNameToCompiledName(name));
                    for (Annotation a : annotationClass.getAnnotations()) {
                        //System.out.println("annotation " + a.getClass().getName()  + " on " + fullCompiledName + " loaded");
                        
                        if (diag.tryMatch(this, NameUtil.compiledNameToSourceName(a.getClass()), nameMatcher)) {
                        	cacheResult(name, true);
                            return true;
                        }
                    }
                } catch (ClassNotFoundException e) {
                	
//                    System.out.println("couldn't load annotation " + fullCompiledName + " for " + found.getJCompilationUnit().getResource().getFullPath() + ",ignoring");
//                    e.printStackTrace();
//                    
                    diag.mismatched("couldn't load annotation " + name + " for " + found.getJCompilationUnit().getResource().getFullPath() + ",ignoring");
                }
                cacheResult(name, false);
                return false;
            }
            
            private void cacheResult(String fullClassName,boolean present){
            	synchronized(lock){
            		cachedClassNameToMatch.put(fullClassName, false);
            	}
            }

            @Override
            public void describeTo(Description desc) {
                desc.value("is marked with annotation with name", nameMatcher);
            }
        };
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
