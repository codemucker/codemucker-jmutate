package org.codemucker.jmutate.ast.matcher;

import static org.codemucker.jmatch.Logical.not;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.codemucker.jfind.matcher.AClass;
import org.codemucker.jfind.matcher.AMethod;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractMatcher;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmatch.expression.AbstractMatchBuilderCallback;
import org.codemucker.jmatch.expression.ExpressionParser;
import org.codemucker.jmutate.ast.Depth;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;

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
	public static AJType that() {
		return with();
	}
	
	public static AJType with() {
		return new AJType();
	}

	//prevent instantiation
	private AJType() {
    	super(JType.class);
	}

	  /**
     * Converts a logical expression using {@link ExpressionParser}} into a matcher. The names in the expression are converted into no arg 
     * method calls on this matcher as in X,isX ==&gt; isX()  (case insensitive) 
     * 
     *  <p>
     * E.g.
     *  <ul>
     *  <li> 
     *  <li>Abstract==&gt; isAbstract()
     *  <li>IsAstract ==&gt; isAbstract()
     *  <li>Anonymous,isAnonymous==&gt; isAnonymous()
     *  
     *  </ul>
     *  </p>
     * @return
     */
    public AJType expression(String expression){
		if (!isBlank(expression)) {
			Matcher<JType> matcher = ExpressionParser.parse(expression,new JTypeMatchBuilderCallback());
	    	if( matcher instanceof AJType){ //make the matching are bit faster by directly running the matchers directly
	    		for(Matcher<JType> m:((AJType)matcher).getMatchers()){
	    			addMatcher(m);
	    		}
	    	} else {
	    		addMatcher(matcher);
	    	}
		}
    	return this;
    }
    
    private boolean isBlank(String s){
    	return s==null || s.trim().length() == 0;
    }
    
    public AJType type(Predicate<JType> predicate){
		predicate(predicate);
		return this;
	}
    
    public AJType packageName(final Class<?> classInPackage){
    	String pkgName = NameUtil.compiledNameToSourceName(classInPackage.getPackage().getName());
    	packageName(pkgName);
    	return this;
    }
    
    public AJType packageName(final String pkgName){
		packageName(AString.equalTo(pkgName));
    	return this;
    }
    
    public AJType packageName(final Matcher<String> matcher){
		addMatcher(new AbstractNotNullMatcher<JType>() {
			@Override
			public boolean matchesSafely(JType found, MatchDiagnostics diag) {
				return diag.tryMatch(this,found.getPackageName(), matcher);
			}

			@Override
			public void describeTo(Description desc) {
				desc.value("package name", matcher);
			}
		});
    	return this;
    }
    
	public AJType packageMatchingAntPattern(final String pkgAntExpression){
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
	
	public AJType isASubclassOf(final Class<?> superClassOrInterface){
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
	
	public AJType isPublicConcreteClass() {
	    isNotAnonymous();
	    isNotInterface();
	    isNotInnerClass();
	    return this;
    }
	
	public AJType isAnonymous(){		
		isAnonymous(true);
		return this;
	}
	
	public AJType isNotAnonymous(){       
        isAnonymous(false);
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
	
	public AJType isNotInterface(){
        isInterface(false);
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
	
	public AJType isNotInnerClass(){
		isInnerClass(false);
		return this;
	}
	
	public AJType isInnerClass(){
		isInnerClass(true);
		return this;
	}
	
	public AJType isInnerClass(Boolean b){
	    if( b != null){
	        addMatcher(b?INNER_CLASS_MATCHER:not(INNER_CLASS_MATCHER));
	    }
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
	
	public AJType isNotAbstract(){
		isAbstract(false);
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
		String fullName = NameUtil.compiledNameToSourceName(matchingClassName.getName());
		fullName(fullName);
		return this;
	}
	
    public AJType simpleName(final String simpleNameAntPattern){
        simpleName(AString.matchingAntPattern(simpleNameAntPattern));
        return this;
    }
    
    public AJType simpleName(final Matcher<String> matcher){
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
    
    public AJType fullName(final String antPattern){
        fullName(AString.matchingAntPattern(antPattern));
        return this;
    }
    
    public AJType fullName(final Matcher<String> matcher) {
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

	public AJType method(final Matcher<JMethod> methodMatcher){
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
	
	public <A extends Annotation> AJType annotation(final Class<A> annotation){
	    annotation(AJAnnotation.with().fullName(annotation));
		return this;
	}
	
	public AJType annotation(final Matcher<JAnnotation> matcher){
        addMatcher(new AbstractNotNullMatcher<JType>() {
            @Override
            public boolean matchesSafely(JType found, MatchDiagnostics diag) {
                return found.getAnnotations().contains(matcher);
            }
            @Override
            public void describeTo(Description desc) {
                //super.describeTo(desc);
                desc.value("with annotation", matcher);
            }
        });
        return this;
    }
	
	public <A extends Annotation> AJType nestedAnnotation(final Class<A> annotation){
        nestedAnnotation(AJAnnotation.with().fullName(annotation));
        return this;
    }
	
    public AJType nestedAnnotation(final Matcher<JAnnotation> matcher) {
        addMatcher(new AbstractNotNullMatcher<JType>() {
            @Override
            public boolean matchesSafely(JType found, MatchDiagnostics diag) {
                return found.getAnnotations().contains(matcher, Depth.ANY);
            }

            @Override
            public void describeTo(Description desc) {
                // super.describeTo(desc);
                desc.value("with nested annotation", matcher);
            }
        });
        return this;
    }
    
    private static class JTypeMatchBuilderCallback extends AbstractMatchBuilderCallback<JType>{

    	private static final Object[] NO_ARGS = new Object[]{};
    	
    	private static Map<String, Method> methodMap = new TreeMap<>();
    	
    	private static Matcher<Method> methodMatcher  = AMethod.that().isPublic().isNotAbstract().numArgs(0).isNotVoidReturn().name("is*");
    	
    	static {
    		for(Method m : AJType.class.getDeclaredMethods()){
    			if(methodMatcher.matches(m)){
    				methodMap.put(m.getName().toLowerCase(),m);
    				if(m.getName().startsWith("is")){
    					methodMap.put(m.getName().substring(2).toLowerCase(),m);
    				}
    			}
    		}
    	}

		@Override
		protected Matcher<JType> newMatcher(String expression) {
			// TODO Auto-generated method stub
			AJType matcher = AJType.with();
			String key = expression.trim().toLowerCase();
			Method m = methodMap.get(key);
			if( m==null){
				throw new NoSuchElementException("Could not find method '" + expression.trim() + " on " + AJType.class.getName() + ",options are " + Arrays.toString(methodMap.keySet().toArray()) + "");
			}
			try {
				m.invoke(matcher, NO_ARGS);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				//should never be thrown
				throw new ExpressionParser.ParseException("Error calling " + AJType.class.getName() + "." + m.getName() + "() from expression '" + expression + "'",e);
			}catch (InvocationTargetException e) {
				//should never be thrown
				throw new ExpressionParser.ParseException("Error calling " + AJType.class.getName() + "." + m.getName() + "() from expression '" + expression + "'",e.getTargetException());
			}
			return matcher;
		}
    }
}
