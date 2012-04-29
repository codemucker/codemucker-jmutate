package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.Type;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.bertvanbrakel.test.util.TestUtils;

public class JFieldMatchers extends LogicalMatchers  {

	private static final Matcher<JField> MATCH_ANY  = new Matcher<JField>() {
		
		@Override
		public boolean matches(JField found) {
			return true;
		}
	};
	
	@SuppressWarnings("unchecked")
    public static Matcher<JField> any(){
		return MATCH_ANY;
	}
	
	public static Matcher<JField> withFQN(final Matcher<String> fqnMatcher){
		return new Matcher<JField>() {
			@Override
			public boolean matches(JField found) {
				
				return typeMatcher.matches(found.getAstNode().getType());
			}
		};
	}
	
	public static Matcher<JField> ofType(final Matcher<Type> typeMatcher){
		return new Matcher<JField>() {
			@Override
			public boolean matches(JField found) {
				return typeMatcher.matches(found.getAstNode().getType());
			}
		};
	}
	
	public static Matcher<JField> withName(final String antPattern){
		return new Matcher<JField>() {
			private final Pattern pattern = TestUtils.antExpToPattern(antPattern);		
			@Override
			public boolean matches(JField found) {
				for(String name:found.getNames()){
					if( pattern.matcher(name).matches()){
						return true;
					}
				}
				return false;
			}
		};
	}
	
	public static <A extends Annotation> Matcher<JField> withAnnotation(final Class<A> annotationClass){
		return new Matcher<JField>() {
			@Override
			public boolean matches(JField found) {
				return found.hasAnnotationOfType(annotationClass);
			}
		};
	}

	public static  Matcher<JField> hasAccess(final JAccess access){
		return new Matcher<JField>() {
			@Override
			public boolean matches(JField found) {
				return found.isAccess(access);
			}
		};
	}
	
}
