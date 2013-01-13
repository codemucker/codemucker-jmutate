package com.bertvanbrakel.codemucker.ast.matcher;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.lang.matcher.AString;
import com.bertvanbrakel.lang.matcher.AbstractNotNullMatcher;
import com.bertvanbrakel.lang.matcher.Description;
import com.bertvanbrakel.lang.matcher.Logical;
import com.bertvanbrakel.lang.matcher.Matcher;
import com.bertvanbrakel.test.util.TestUtils;
import com.google.common.base.Strings;
/**
 * Provides convenience static methods for creating {@link Matcher<JType>} matchers
 */
public class AJType extends Logical {

	private static final Matcher<JType> ANONYMOUS_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found) {
			return found.isAnonymousClass();
		}
	};

	private static Matcher<JType> ANNOTATION_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found) {
			return found.isAnnotation();
		}
	};
	
	private static Matcher<JType> INTERFACE_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found) {
			return found.isInterface();
		}
	};
	
	private static Matcher<JType> INNER_CLASS_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found) {
			return found.isInnerClass();
		}
	};
	
	private static Matcher<JType> ENUM_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found) {
			return found.isEnum();
		}
	};
	
	private static Matcher<JType> ABSTRACT_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found) {
			return found.isAbstract();
		}
	};
	
	private AJType() {
    	//prevent instantiation
	}
	
	@SuppressWarnings("unchecked")
    public static Matcher<JType> any() {
		return Logical.any();
	}
	
	@SuppressWarnings("unchecked")
    public static Matcher<JType> none() {
		return Logical.none();
	}

	public static Matcher<JType> inPackage(final String pkgAntExpression){
		return new AbstractNotNullMatcher<JType>() {
			private final Pattern pattern = TestUtils.antExpToPattern(pkgAntExpression);

			@Override
			public boolean matchesSafely(JType found) {
				String pkgName = Strings.emptyToNull(found.getPackageName());
				if (pkgName != null) {
					return pattern.matcher(found.getPackageName()).matches();
				}
				return false;
			}
		};
	}
	
	public static Matcher<JType> assignableFrom(final Class<?> superClassOrInterface){
		return new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found) {
				return found.isSubClassOf(superClassOrInterface);
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
	
	public static Matcher<JType> isAbstract(){
		return ABSTRACT_MATCHER;
	}
	
	public static Matcher<JType> withAccess(final JAccess access){
		return new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found) {
				return found.isAccess(access);
			}
		};
	}

	public static Matcher<JType> subclassOf(final Class<?> superClassOrInterface){
		return new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found) {
				return found.isSubClassOf(superClassOrInterface);
			}
		};
	}
	
	public static Matcher<JType> withName(final Class<?> matchingClassName){
		return withFullName(convertInnerClassName(matchingClassName.getName()));
	}
	
	private static String convertInnerClassName(String classname){
		return classname.replace('$', '.');
	}
	
	public static Matcher<JType> withFullName(final String antPattern){
		return new AbstractNotNullMatcher<JType>() {
			private final Pattern pattern = TestUtils.antExpToPattern(antPattern);
			@Override
			public boolean matchesSafely(JType found) {
				return pattern.matcher(found.getFullName()).matches();
			}
			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.value("type with fullName matching ant pattern", antPattern);
			}
		};
	}
	
	public static Matcher<JType> withSimpleNameAntPattern(final String antPattern){
		return new AbstractNotNullMatcher<JType>() {
			private final Matcher<String> matcher = AString.withAntPattern(antPattern);
			@Override
			public boolean matchesSafely(JType found) {
				return matcher.matches(found.getSimpleName());
			}
			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.value("type with simpleName matching ant pattern", antPattern);
			}
		};
	}

	public static Matcher<JType> withMethod(final Matcher<JMethod> methodMatcher){
		return new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found) {
				return found.findMethodsMatching(methodMatcher).toList().size() > 0;
			}
			
			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.value("type with method", methodMatcher);
			}
		};
	}
	
	public static <A extends Annotation> Matcher<JType> withAnnotation(final Class<A> annotation){
		return new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found) {
				return found.hasAnnotationOfType(annotation, false);
			}
			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.value("type with annotation", annotation);
			}
		};
	}
}
