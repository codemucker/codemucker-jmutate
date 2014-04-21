package org.codemucker.jmutate.ast.matcher;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.JavaNameUtil;

import com.google.common.base.Predicate;


public class AJSourceFile extends ObjectMatcher<JSourceFile> {

	public static AJSourceFile that(){
		return with();
	}
	
	public static AJSourceFile with(){
		return new AJSourceFile();
	}
	
	public AJSourceFile source(Predicate<JSourceFile> predicate){
		predicate(predicate);
		return this;
	}

    public static Matcher<JSourceFile> any() {
    	return Logical.any();
    }
	
    public static Matcher<JSourceFile> none() {
    	return Logical.none();
    }
	
	public AJSourceFile isSubclassOf(Class<?> superClassOrInterface) {
		return contains(AJType.with().isASubclassOf(superClassOrInterface));
	}
	
	public AJSourceFile annotation(Class<? extends Annotation> annotation){
		return contains(AJType.with().annotation(annotation));
	}
	
	public AJSourceFile typeFullName(Class<?> className){
		return contains(AJType.with().name(className));
	}
	
	public AJSourceFile typeFullName(String classNameAntPattern){
		return contains(AJType.with().fullName(classNameAntPattern));
	}
	
	public AJSourceFile isEnum() {
		return isEnum(true);
	}

	public AJSourceFile isEnum(boolean b) {
		contains(AJType.with().isEnum(b));
		return this;
	}
	
	public AJSourceFile isAnonymous(){
		isAnonymous(true);
		return this;
	}
	
	public AJSourceFile isAnonymous(boolean b) {
		contains(AJType.with().isAnonymous(b));
		return this;
	}
	
	public AJSourceFile isInterface(){
		isInterface(true);
		return this;
	}
	
	public AJSourceFile isInterface(boolean b) {
		contains(AJType.with().isInterface(b));
		return this;
	}
	
	public AJSourceFile notContains(Matcher<JType> typeMatcher){
		addMatcher(Logical.not(contains(typeMatcher)));
		return this;
	}
	
	public AJSourceFile contains(Matcher<JType> typeMatcher){
		addMatcher(new JTypeToJSourceMatcherAdapter(typeMatcher));
		return this;
	}
	
	private static class JTypeToJSourceMatcherAdapter extends AbstractNotNullMatcher<JSourceFile>{
		private final Matcher<JType> typeMatcher;
		
		JTypeToJSourceMatcherAdapter(Matcher<JType> typeMatcher){
			this.typeMatcher = checkNotNull(typeMatcher,"expect type matcher");
		}
		
		@Override
        public boolean matchesSafely(JSourceFile found, MatchDiagnostics diag) {
			for( JType type:found.getTopJTypes()){
				if( typeMatcher.matches(type)){
					return true;
				}
			}
			return false;
		}
	}

}
