package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.bertvanbrakel.test.util.TestUtils;
import com.google.common.base.Strings;
/**
 * Provides convenience static methods for creating {@link Matcher<JType>} matchers
 */
public class JTypeMatchers extends LogicalMatchers {

	private static final Matcher<JType> ANONYMOUS_MATCHER = new Matcher<JType>() {
		@Override
		public boolean matches(JType found) {
			return found.isAnonymousClass();
		}
	};

	private static Matcher<JType> ANNOTATION_MATCHER = new Matcher<JType>() {
		@Override
		public boolean matches(JType found) {
			return found.isAnnotation();
		}
	};
	
	private static Matcher<JType> INTERFACE_MATCHER = new Matcher<JType>() {
		@Override
		public boolean matches(JType found) {
			return found.isInterface();
		}
	};
	
	private static Matcher<JType> INNER_CLASS_MATCHER = new Matcher<JType>() {
		@Override
		public boolean matches(JType found) {
			return found.isInnerClass();
		}
	};
	
	private static Matcher<JType> ENUM_MATCHER = new Matcher<JType>() {
		@Override
		public boolean matches(JType found) {
			return found.isEnum();
		}
	};
	
	private JTypeMatchers() {
    	//prevent instantiation
	}
	
	@SuppressWarnings("unchecked")
    public static Matcher<JType> any() {
		return LogicalMatchers.any();
	}
	
	@SuppressWarnings("unchecked")
    public static Matcher<JType> none() {
		return LogicalMatchers.none();
	}
	
	public static Matcher<JType> inPackage(final String pkgAntExpression){
		return new Matcher<JType>() {
			private final Pattern pattern = TestUtils.antExpToPattern(pkgAntExpression);

			@Override
			public boolean matches(JType found) {
				String pkgName = Strings.emptyToNull(found.getPackageName());
				if (pkgName != null) {
					return pattern.matcher(found.getPackageName()).matches();
				}
				return false;
			}
		};
	}
	
	public static Matcher<JType> assignableFrom(final Class<?> superClassOrInterface){
		return new Matcher<JType>() {
			@Override
			public boolean matches(JType found) {
				return found.isImplementing(superClassOrInterface);
			}
		};
	}
	
	public static Matcher<JType> isAnonymous(){		
		return ANONYMOUS_MATCHER;
	}
	
	public static Matcher<JType> isInterface(){
		return INTERFACE_MATCHER;
	}
	
	public static Matcher<JType> isAnnotation(){
		return ANNOTATION_MATCHER;
	}
	
	public static Matcher<JType> isInnerClass(){
		return INNER_CLASS_MATCHER;
	}

	public static Matcher<JType> isEnum(){
		return ENUM_MATCHER;
	}
	
	public static Matcher<JType> withAccess(final JAccess access){
		return new Matcher<JType>() {
			@Override
			public boolean matches(JType found) {
				return found.isAccess(access);
			}
		};
	}

	public static Matcher<JType> withFullName(final Class<?> matchingClassName){
		return withFullName(convertInnerClassName(matchingClassName.getName()));
	}
	
	private static String convertInnerClassName(String classname){
		return classname.replace('$', '.');
	}
	
	public static Matcher<JType> withFullName(final String antPattern){
		return new Matcher<JType>() {
			private final Pattern pattern = TestUtils.antExpToPattern(antPattern);
			@Override
			public boolean matches(JType found) {
				return pattern.matcher(found.getFullName()).matches();
			}
		};
	}
	
	public static Matcher<JType> withSimpleName(final String antPattern){
		return new Matcher<JType>() {
			private final Pattern pattern = TestUtils.antExpToPattern(antPattern);
			@Override
			public boolean matches(JType found) {
				return pattern.matcher(found.getSimpleName()).matches();
			}
		};
	}

	public static Matcher<JType> withMethod(final Matcher<JMethod> methodMatcher){
		return new Matcher<JType>() {
			@Override
			public boolean matches(JType found) {
				return found.findMethodsMatching(methodMatcher).toList().size() > 0;
			}
		};
	}
	
	public static <A extends Annotation> Matcher<JType> withAnnotation(final Class<A> annotation){
		return new Matcher<JType>() {
			@Override
			public boolean matches(JType found) {
				return found.hasAnnotationOfType(annotation, false);
			}
		};
	}
}
