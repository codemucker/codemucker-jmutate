package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.Type;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JField;
import com.bertvanbrakel.test.util.TestUtils;

public class JFieldMatchers extends JMatchers  {

	private static final JFieldMatcher MATCH_ANY  = new JFieldMatcher() {
		
		@Override
		public boolean matches(JField found) {
			return true;
		}
	};
	
	public static JFieldMatcher any(){
		return MATCH_ANY;
	}
	public static JFieldMatcher ofType(final Matcher<Type> typeMatcher){
		return new JFieldMatcher() {
			@Override
			public boolean matches(JField found) {
				return typeMatcher.matches(found.getFieldNode().getType());
			}
		};
	}
	
	public static JFieldMatcher withName(final String antPattern){
		return new JFieldMatcher() {
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
	
	public static <A extends Annotation> JFieldMatcher withAnnotation(final Class<A> annotationClass){
		return new JFieldMatcher() {
			@Override
			public boolean matches(JField found) {
				return found.hasAnnotationOfType(annotationClass);
			}
		};
	}

	public static  JFieldMatcher hasAccess(final JAccess access){
		return new JFieldMatcher() {
			@Override
			public boolean matches(JField found) {
				return found.isAccess(access);
			}
		};
	}
	
}
