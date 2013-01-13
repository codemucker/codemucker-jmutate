package org.codemucker.jmutate.ast.matcher;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;


public class AJSourceFile extends AnInt {

	public static final Matcher<JSourceFile> MATCHER_ANONYMOUS = containsType(AJType.isAnonymous());
	public static final Matcher<JSourceFile> MATCHER_ENUM = containsType(AJType.isEnum());
	public static final Matcher<JSourceFile> MATCHER_INTERFACE = containsType(AJType.isInterface());
	
    public static Matcher<JSourceFile> anyClass() {
    	return Logical.any();
    }
	
    public static Matcher<JSourceFile> noClass() {
    	return Logical.none();
    }
	
	public static Matcher<JSourceFile> assignableTo(Class<?> superClassOrInterface) {
		return containsType(AJType.assignableFrom(superClassOrInterface));
	}
	
	public static Matcher<JSourceFile> withAnnotation(Class<? extends Annotation> annotation){
		return containsType(AJType.withAnnotation(annotation));
	}
	
	public static Matcher<JSourceFile> withName(Class<?> className){
		return containsType(AJType.withName(className));
	}
	
	public static Matcher<JSourceFile> withName(String antPattern){
		return containsType(AJType.withFullName(antPattern));
	}
	
	public static Matcher<JSourceFile> excludeEnum() {
		return not(MATCHER_ENUM);
	}

	public static Matcher<JSourceFile> excludeAnonymous() {
		return not(MATCHER_ANONYMOUS);
	}

	public static Matcher<JSourceFile> excludeInterfaces() {
		return not(MATCHER_INTERFACE);
	}

	public static Matcher<JSourceFile> includeEnum() {
		return MATCHER_ENUM;
	}

	public static Matcher<JSourceFile> includeAnonymous() {
		return MATCHER_ANONYMOUS;
	}

	public static Matcher<JSourceFile> includeInterfaces() {
		return MATCHER_INTERFACE;
	}
	
	public static Matcher<JSourceFile> notContainsType(Matcher<JType> typeMatcher){
		return Logical.not(containsType(typeMatcher));
	}
	
	public static Matcher<JSourceFile> containsType(Matcher<JType> typeMatcher){
		return new JTypeToJSourceMatcherAdapter(typeMatcher);
	}
	
	private static class JTypeToJSourceMatcherAdapter extends AbstractNotNullMatcher<JSourceFile>{
		private final Matcher<JType> typeMatcher;
		
		JTypeToJSourceMatcherAdapter(Matcher<JType> typeMatcher){
			this.typeMatcher = checkNotNull(typeMatcher,"expect type matcher");
		}
		
		@Override
        public boolean matchesSafely(JSourceFile found) {
			for( JType type:found.getTopJTypes()){
				if( typeMatcher.matches(type)){
					return true;
				}
			}
			return false;
		}
	}

}
