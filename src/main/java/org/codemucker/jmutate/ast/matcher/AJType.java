package org.codemucker.jmutate.ast.matcher;

import static org.codemucker.jmatch.Logical.not;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractMatcher;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmatch.PredicateToMatcher;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.JavaNameUtil;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
/**
 * Provides convenience static methods for creating {@link Matcher<JType>} matchers
 */
public class AJType extends ObjectMatcher<JType> { 

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
	
    public static Matcher<JType> any() {
		return Logical.any();
	}
	
    public static Matcher<JType> none() {
		return Logical.none();
	}

    /**
	 * Synonym for with()
	 * @return
	 */
	public static AJType that() {
		return with();
	}
	
	public static AJType with() {
		return new AJType();
	}

	private AJType() {
    	//prevent instantiation
	}

    public AJType type(Predicate<JType> predicate){
		predicate(predicate);
		return this;
	}
    
    public AJType packageName(final Class<?> classInPackage){
    	String pkgName = JavaNameUtil.compiledNameToSourceName(classInPackage.getPackage().getName());
    	packageName(pkgName);
    	return this;
    }
    
    public AJType packageName(final String pkgName){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			private final Matcher<String> pkgNameMatcher = AString.equalTo(pkgName);
			
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
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
		});
    	return this;
    }
    
	public AJType packageMatchesAntPattern(final String pkgAntExpression){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			private final Matcher<String> pkgNameMatcher = AString.withAntPattern(pkgAntExpression);
			
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
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
		});
		return this;
	}
	
	public AJType isASubclassOf(final Class<?> superClassOrInterface){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.isSubClassOf(superClassOrInterface);
			}
			
			@Override
			public String toString(){
				return Objects.toStringHelper(this)
					.add("subclass of", superClassOrInterface)
					.toString();
			}
		});
		return this;
	}
	
	public AJType isAnonymous(){		
		isAnonymous(true);
		return this;
	}
	
	public AJType isAnonymous(boolean b){
		addMatcher(b?ANONYMOUS_MATCHER:not(ANONYMOUS_MATCHER));
		return this;
	}
	
	public AJType isInterface(){
		isInterface(true);
		return this;
	}
	
	public AJType isInterface(boolean b){
		addMatcher(b?INTERFACE_MATCHER:not(INTERFACE_MATCHER));
		return this;
	}
	
	public AJType isAnnotation(){
		isAnnotation(true);
		return this;
	}
	
	public AJType isAnnotation(boolean b){
		addMatcher(b?ANNOTATION_MATCHER:not(ANNOTATION_MATCHER));
		return this;
	}
	
	public AJType isInnerClass(){
		isInnerClass(true);
		return this;
	}
	
	public AJType isInnerClass(boolean b){
		addMatcher(b?INNER_CLASS_MATCHER:not(INNER_CLASS_MATCHER));
		return this;
	}

	public AJType isEnum(){
		isEnum(true);
		return this;
	}
	
	public AJType isEnum(boolean b){
		addMatcher(b?ENUM_MATCHER:not(ENUM_MATCHER));
		return this;
	}
	
	public AJType isAbstract(){
		isAbstract(true);
		return this;
	}
	
	public AJType isAbstract(boolean b){
		addMatcher(b?ABSTRACT_MATCHER:not(ABSTRACT_MATCHER));
		return this;
	}
	
	public AJType access(final JAccess access){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.isAccess(access);
			}
		});
		return this;
	}

	public AJType name(final Class<?> matchingClassName){
		fullName(JavaNameUtil.compiledNameToSourceName(matchingClassName.getName()));
		return this;
	}
	
	public AJType fullName(final String antPattern){
		addMatcher(new AbstractMatcher<JType>() {
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
		});
		return this;
	}
	
	public AJType simpleName(final String name){
		simpleNameMatchesAntPattern(name);
		return this;
	}
	
	public AJType simpleNameMatchesAntPattern(final String antPattern){
		addMatcher(new AbstractNotNullMatcher<JType>() {
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
		});
		return this;
	}

	public AJType method(final Matcher<JMethod> methodMatcher){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.findMethodsMatching(methodMatcher).toList().size() > 0;
			}
			
			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.value("type with method", methodMatcher);
			}
		});
		return this;
	}
	
	public <A extends Annotation> AJType annotation(final Class<A> annotation){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.hasAnnotationOfType(annotation, false);
			}
			@Override
			public void describeTo(Description desc) {
				super.describeTo(desc);
				desc.value("type with annotation", annotation);
			}
		});
		return this;
	}
}
