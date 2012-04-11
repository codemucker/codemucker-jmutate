package com.bertvanbrakel.codemucker.ast.finder.matcher;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;

import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;

public class JSourceMatchers extends IntegerMatchers {

	public static final Matcher<JSourceFile> MATCHER_ANONYMOUS = containsType(JTypeMatchers.isAnonymous());
	public static final Matcher<JSourceFile> MATCHER_ENUM = containsType(JTypeMatchers.isEnum());
	public static final Matcher<JSourceFile> MATCHER_INTERFACE = containsType(JTypeMatchers.isInterface());
	
    public static Matcher<JSourceFile> anyClass() {
    	return LogicalMatchers.any();
    }
	
    public static Matcher<JSourceFile> noClass() {
    	return LogicalMatchers.none();
    }
	
	public static Matcher<JSourceFile> assignableTo(Class<?> superClassOrInterface) {
		return containsType(JTypeMatchers.assignableFrom(superClassOrInterface));
	}
	
	public static Matcher<JSourceFile> withAnnotation(Class<? extends Annotation> annotation){
		return containsType(JTypeMatchers.withAnnotation(annotation));
	}
	
	public static Matcher<JSourceFile> withName(Class<?> className){
		return containsType(JTypeMatchers.withName(className));
	}
	
	public static Matcher<JSourceFile> withName(String antPattern){
		return containsType(JTypeMatchers.withName(antPattern));
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
		return LogicalMatchers.not(containsType(typeMatcher));
	}
	
	public static Matcher<JSourceFile> containsType(Matcher<JType> typeMatcher){
		return new JTypeToJSourceMatcherAdapter(typeMatcher);
	}
	
	private static class JTypeToJSourceMatcherAdapter implements Matcher<JSourceFile>{
		private final Matcher<JType> typeMatcher;
		
		JTypeToJSourceMatcherAdapter(Matcher<JType> typeMatcher){
			this.typeMatcher = checkNotNull(typeMatcher,"expect type matcher");
		}
		
		@Override
        public boolean matches(JSourceFile found) {
			for( JType type:found.getJTypes()){
				if( typeMatcher.matches(type)){
					return true;
				}
			}
			return false;
		}
	}

}
