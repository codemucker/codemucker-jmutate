package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JAccess;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.test.util.TestUtils;
import com.google.common.base.Strings;
/**
 * Provides convenience static methods for creating {@link JTypeMatcher} matchers
 */
public class JTypeMatchers {

	private static final JTypeMatcher ANONYMOUS_MATCHER = new JTypeMatcher() {
		@Override
		public boolean matches(JType found) {
			return found.isAnonymousClass();
		}
	};

	private static JTypeMatcher ANNOTATION_MATCHER = new JTypeMatcher() {
		@Override
		public boolean matches(JType found) {
			return found.isAnnotation();
		}
	};
	
	private static JTypeMatcher INTERFACE_MATCHER = new JTypeMatcher() {
		@Override
		public boolean matches(JType found) {
			return found.isInterface();
		}
	};
	
	private static JTypeMatcher INNER_CLASS_MATCHER = new JTypeMatcher() {
		@Override
		public boolean matches(JType found) {
			return found.isInnerClass();
		}
	};
	
	private static JTypeMatcher ENUM_MATCHER = new JTypeMatcher() {
		@Override
		public boolean matches(JType found) {
			return found.isEnum();
		}
	};
	
	
	
	
	
	private JTypeMatchers(){
		//prevent instantiation
	}
	
	/**
	 * Synonym for {@link #and(JTypeMatcher...)}
	 */
	public static JTypeMatcher all(final JTypeMatcher... matchers){
		return and(matchers);
	}
	
	public static JTypeMatcher and(final JTypeMatcher... matchers){
		return new JTypeMatcher() {
			@Override
			public boolean matches(JType found) {
				for(JTypeMatcher matcher:matchers){
					if( !matcher.matches(found)){
						return false;
					}
				}
				return true;
			}
		};
	}

	/**
	 * Synonym for {@link #or(JTypeMatcher...)}
	 */
	public static JTypeMatcher either(final JTypeMatcher... matchers){
		return or(matchers);
	}
	
	/**
	 * Synonym for {@link #or(JTypeMatcher...)}
	 */
	public static JTypeMatcher any(final JTypeMatcher... matchers){
		return or(matchers);
	}
	
	public static JTypeMatcher or(final JTypeMatcher... matchers){
		return new JTypeMatcher() {
			@Override
			public boolean matches(JType found) {
				for(JTypeMatcher matcher:matchers){
					if( matcher.matches(found)){
						return true;
					}
				}
				return false;
			}
		};
	}
	
	public static JTypeMatcher not(final JTypeMatcher matcher){
		return new JTypeMatcher() {
			@Override
			public boolean matches(JType found) {
				return !matcher.matches(found);
			}
		};
	}
	
	public static JTypeMatcher inPackage(final String pkgAntExpression){
		return new JTypeMatcher() {
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
	
	public static JTypeMatcher assignableFrom(final Class<?> superClassOrInterface){
		return new JTypeMatcher() {
			@Override
			public boolean matches(JType found) {
				return found.isImplementing(superClassOrInterface);
			}
		};
	}
	
	public static JTypeMatcher isAnonymous(){		
		return ANONYMOUS_MATCHER;
	}
	
	public static JTypeMatcher isInterface(){
		return INTERFACE_MATCHER;
	}
	
	public static JTypeMatcher isAnnotation(){
		return ANNOTATION_MATCHER;
	}
	
	public static JTypeMatcher isInnerClass(){
		return INNER_CLASS_MATCHER;
	}

	public static JTypeMatcher isEnum(){
		return ENUM_MATCHER;
	}
	
	public static JTypeMatcher withAccess(final JAccess access){
		return new JTypeMatcher() {
			@Override
			public boolean matches(JType found) {
				return found.getJavaModifiers().isAccess(access);
			}
		};
	}
	
	public static JTypeMatcher withName(final String antPattern){
		return new JTypeMatcher() {
			private final Pattern pattern = TestUtils.antExpToPattern(antPattern);
			@Override
			public boolean matches(JType found) {
				return pattern.matcher(found.getSimpleName()).matches();
			}
		};
	}
	
	public static <A extends Annotation> JTypeMatcher withAnnotation(final Class<A> annotation){
		return new JTypeMatcher() {
			@Override
			public boolean matches(JType found) {
				return found.hasAnnotationOfType(annotation, false);
			}
		};
	}
}
