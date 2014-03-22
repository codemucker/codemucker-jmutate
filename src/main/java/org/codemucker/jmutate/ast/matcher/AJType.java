package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractMatcher;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.JavaNameUtil;

import com.google.common.base.Strings;
/**
 * Provides convenience static methods for creating {@link Matcher<JType>} matchers
 */
public class AJType { //extends Logical {

	private static final Matcher<JType> ANONYMOUS_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isAnonymousClass();
		}
	};

	private static Matcher<JType> ANNOTATION_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isAnnotation();
		}
	};
	
	private static Matcher<JType> INTERFACE_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isInterface();
		}
	};
	
	private static Matcher<JType> INNER_CLASS_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isInnerClass();
		}
	};
	
	private static Matcher<JType> ENUM_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isEnum();
		}
	};
	
	private static Matcher<JType> ABSTRACT_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
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
		return new AbstractMatcher<JType>() {
			private final Matcher<String> pkgNameMatcher = AString.withAntPattern(pkgAntExpression);
			
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				if( found == null){
					return false;
				}
				String pkgName = Strings.emptyToNull(found.getPackageName());
				if (pkgName != null) {
					return diag.TryMatch(found.getPackageName(), pkgNameMatcher);
				}
				return false;
			}

			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.value("type with packageName matching", pkgNameMatcher);
			}
		};
	}
	
	public static Matcher<JType> assignableFrom(final Class<?> superClassOrInterface){
		return new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
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
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.isAccess(access);
			}
		};
	}

	public static Matcher<JType> subclassOf(final Class<?> superClassOrInterface){
		return new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.isSubClassOf(superClassOrInterface);
			}
		};
	}
	
	public static Matcher<JType> withName(final Class<?> matchingClassName){
		return withFullName(JavaNameUtil.compiledNameToSourceName(matchingClassName.getName()));
	}
	
	public static Matcher<JType> withFullName(final String antPattern){
		return new AbstractMatcher<JType>() {
			private final Matcher<String> nameMatcher = AString.withAntPattern(antPattern);
			
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found != null
						&& diag.TryMatch(found.getFullName(), nameMatcher);
			}

			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.value("type with fullName matching", nameMatcher);
			}
		};
	}
	
	public static Matcher<JType> withSimpleName(final String name){
		return withSimpleNameAntPattern(name);
	}
	
	public static Matcher<JType> withSimpleNameAntPattern(final String antPattern){
		return new AbstractNotNullMatcher<JType>() {
			private final Matcher<String> matcher = AString.withAntPattern(antPattern);
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
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
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
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
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
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
