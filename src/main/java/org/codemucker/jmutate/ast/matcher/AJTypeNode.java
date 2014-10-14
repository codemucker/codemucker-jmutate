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
import org.codemucker.jmutate.ast.Depth;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.JavaNameUtil;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
/**
 * Provides convenience static methods for creating {@link Matcher<JType>} matchers
 */
public class AJTypeNode extends ObjectMatcher<JType> { 

	private static final Matcher<JType> ANONYMOUS_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isAnonymousClass();
		}
		
		@Override
        public void describeTo(Description desc) {
            desc.text("is anonymous class");
        };
	};

	private static Matcher<JType> ANNOTATION_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isAnnotation();
		}
		
		@Override
		public void describeTo(Description desc) {
		    desc.text("is annotation");
		};
	};
	
	private static Matcher<JType> INTERFACE_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isInterface();
		}
		@Override
        public void describeTo(Description desc) {
            desc.text("is interface");
        };
	};
	
	private static Matcher<JType> INNER_CLASS_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isInnerClass();
		}
		@Override
        public void describeTo(Description desc) {
            desc.text("is inner class");
        };
	};
	
	private static Matcher<JType> ENUM_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isEnum();
		}
		@Override
        public void describeTo(Description desc) {
            desc.text("is enum");
        };
	};
	
	private static Matcher<JType> ABSTRACT_MATCHER = new AbstractNotNullMatcher<JType>() {
		@Override
		public boolean matchesSafely(JType found, MatchDiagnostics diag) {
			return found.isAbstract();
		}
		@Override
        public void describeTo(Description desc) {
            desc.text("is abstract class");
        };
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
	public static AJTypeNode that() {
		return with();
	}
	
	public static AJTypeNode with() {
		return new AJTypeNode();
	}

	//prevent instantiation
	private AJTypeNode() {
    	super(JType.class);
	}

    public AJTypeNode type(Predicate<JType> predicate){
		predicate(predicate);
		return this;
	}
    
    public AJTypeNode packageName(final Class<?> classInPackage){
    	String pkgName = JavaNameUtil.compiledNameToSourceName(classInPackage.getPackage().getName());
    	packageName(pkgName);
    	return this;
    }
    
    public AJTypeNode packageName(final String pkgName){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			private final Matcher<String> pkgNameMatcher = AString.equalTo(pkgName);
			
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				String pkgName = found.getPackageName();
				if (pkgName != null) {
					return diag.tryMatch(this,found.getPackageName(), pkgNameMatcher);
				}
				return false;
			}

			@Override
			public void describeTo(Description desc) {
				//super.describeTo(desc);
				desc.value("package name matching", pkgNameMatcher);
			}
		});
    	return this;
    }
    
	public AJTypeNode packageMatchingAntPattern(final String pkgAntExpression){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			private final Matcher<String> pkgNameMatcher = AString.matchingAntPattern(pkgAntExpression);
			
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				String pkgName = Strings.emptyToNull(found.getPackageName());
				if (pkgName != null) {
					return diag.tryMatch(this,found.getPackageName(), pkgNameMatcher);
				}
				return false;
			}

			@Override
			public void describeTo(Description desc) {
				//super.describeTo(desc);
				desc.value("package name matching", pkgNameMatcher);
			}
		});
		return this;
	}
	
	public AJTypeNode isASubclassOf(final Class<?> superClassOrInterface){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.isSubClassOf(superClassOrInterface);
			}
			
			@Override
			public void describeTo(Description desc) {
			    desc.value("subclass of", superClassOrInterface.getName());
			};
		});
		return this;
	}
	
	public AJTypeNode isAnonymous(){		
		isAnonymous(true);
		return this;
	}
	
	public AJTypeNode isAnonymous(boolean b){
		addMatcher(b?ANONYMOUS_MATCHER:not(ANONYMOUS_MATCHER));
		return this;
	}
	
	public AJTypeNode isInterface(){
		isInterface(true);
		return this;
	}
	
	public AJTypeNode isInterface(boolean b){
		addMatcher(b?INTERFACE_MATCHER:not(INTERFACE_MATCHER));
		return this;
	}
	
	public AJTypeNode isAnnotation(){
		isAnnotation(true);
		return this;
	}
	
	public AJTypeNode isAnnotation(boolean b){
		addMatcher(b?ANNOTATION_MATCHER:not(ANNOTATION_MATCHER));
		return this;
	}
	
	public AJTypeNode isInnerClass(){
		isInnerClass(true);
		return this;
	}
	
	public AJTypeNode isInnerClass(boolean b){
		addMatcher(b?INNER_CLASS_MATCHER:not(INNER_CLASS_MATCHER));
		return this;
	}

	public AJTypeNode isEnum(){
		isEnum(true);
		return this;
	}
	
	public AJTypeNode isEnum(boolean b){
		addMatcher(b?ENUM_MATCHER:not(ENUM_MATCHER));
		return this;
	}
	
	public AJTypeNode isAbstract(){
		isAbstract(true);
		return this;
	}
	
	public AJTypeNode isNotAbstract(){
		isAbstract(false);
		return this;
	}
	
	public AJTypeNode isAbstract(boolean b){
		addMatcher(b?ABSTRACT_MATCHER:not(ABSTRACT_MATCHER));
		return this;
	}
	
	public AJTypeNode access(final JAccess access){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.isAccess(access);
			}
		});
		return this;
	}

	public AJTypeNode name(final Class<?> matchingClassName){
		String fullName = JavaNameUtil.compiledNameToSourceName(matchingClassName.getName());
		fullName(fullName);
		return this;
	}
	
    public AJTypeNode simpleName(final String simpleNameAntPattern){
        simpleName(AString.matchingAntPattern(simpleNameAntPattern));
        return this;
    }
    
    public AJTypeNode simpleName(final Matcher<String> matcher){
        addMatcher(new AbstractMatcher<JType>() {
            @Override
            public boolean matchesSafely(JType found, MatchDiagnostics diag) {
                return found != null
                        && diag.tryMatch(this,found.getSimpleName(), matcher);
            }

            @Override
            public void describeTo(Description desc) {
                super.describeTo(desc);
                desc.value("simple name matching", matcher);
            }
        });
        return this;
    }
    
    public AJTypeNode fullName(final String antPattern){
        fullName(AString.matchingAntPattern(antPattern));
        return this;
    }
    
    public AJTypeNode fullName(final Matcher<String> matcher) {
        addMatcher(new AbstractMatcher<JType>() {
            @Override
            public boolean matchesSafely(JType found, MatchDiagnostics diag) {
                return found != null && diag.tryMatch(this, found.getFullName(), matcher);
            }

            @Override
            public void describeTo(Description desc) {
                //super.describeTo(desc);
                desc.value("fullname matching", matcher);
            }
        });
        return this;
    }

	public AJTypeNode method(final Matcher<JMethod> methodMatcher){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return found.findMethodsMatching(methodMatcher).toList().size() > 0;
			}
			
			@Override
			public void describeTo(Description desc) {
				//super.describeTo(desc);
				desc.value("with method", methodMatcher);
			}
		});
		return this;
	}
	
	public <A extends Annotation> AJTypeNode annotation(final Class<A> annotation){
	    annotation(AJAnnotationNode.with().fullName(annotation));
		return this;
	}
	
	public AJTypeNode annotation(final Matcher<JAnnotation> matcher){
  
        addMatcher(new AbstractNotNullMatcher<JType>() {
            @Override
            public boolean matchesSafely(JType found, MatchDiagnostics diag) {
                return found.getAnnotations().contains(matcher);
            }
            @Override
            public void describeTo(Description desc) {
                //super.describeTo(desc);
                desc.value("marked with annotation", matcher);
            }
        });
        return this;
    }
	
	public <A extends Annotation> AJTypeNode nestedAnnotation(final Class<A> annotation){
        nestedAnnotation(AJAnnotationNode.with().fullName(annotation));
        return this;
    }
	
	   public AJTypeNode nestedAnnotation(final Matcher<JAnnotation> matcher){
	       
	        addMatcher(new AbstractNotNullMatcher<JType>() {
	            @Override
	            public boolean matchesSafely(JType found, MatchDiagnostics diag) {
	                return found.getAnnotations().contains(matcher, Depth.ANY);
	            }
	            @Override
	            public void describeTo(Description desc) {
	                //super.describeTo(desc);
	                desc.value("with nested annotation", matcher);
	            }
	        });
	        return this;
	    }
    
    
}
