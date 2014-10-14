package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractMatcher;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JModifier;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.base.Predicate;


public class AJMethodNode extends ObjectMatcher<JMethod> {
	
	/**
	 * synonym for with()
	 * 
	 * @return
	 */
	public static AJMethodNode that(){
		return with();
	}
	
	public static AJMethodNode with(){
		return new AJMethodNode();
	}

	public AJMethodNode(){
	    super(JMethod.class);
	}
	
	public AJMethodNode method(Predicate<JMethod> predicate){
		predicate(predicate);
		return this;
	}

	/**
	 * Return a matcher which matches using the given ant style method name expression
	 * @param antPattern ant style pattern. E.g. *foo*bar??Ho
	 * @return
	 */
	public AJMethodNode nameMatchingAntPattern(final String antPattern) {
		name(AString.matchingAntPattern(antPattern));
		return this;
	}
	
	public AJMethodNode name(final String name) {
		name(AString.equalTo(name));
		return this;
	}
	
	public AJMethodNode name(final Matcher<String> matcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return matcher.matches(found.getName());
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("with name", matcher);
            }
		});
		return this;
	}
	
	public AJMethodNode returningSomething() {
        addMatcher(Logical.not(newVoidReturnMatcher()));
        return this;
    }
	
	public AJMethodNode returningVoid() {
	    addMatcher(newVoidReturnMatcher());
	    return this;
	}
	
	private static Matcher<JMethod> newVoidReturnMatcher(){
	    final Matcher<Type> voidTypeMatcher = newVoidTypeMatcher();
	    return new AbstractNotNullMatcher<JMethod>() {
            @Override
            public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
                Type returnType = found.getAstNode().getReturnType2();
                return diag.tryMatch(this, returnType, voidTypeMatcher);
            }
            
            @Override
            public void describeTo(Description desc) {
                desc.text("returning void");
            }
        };
	}
	
	   private static Matcher<Type> newVoidTypeMatcher(){
	        return new AbstractMatcher<Type>(AllowNulls.YES) {
	            @Override
	            public boolean matchesSafely(Type found, MatchDiagnostics diag) {
	                if (found != null && !(found.isPrimitiveType() && ((PrimitiveType) found).getPrimitiveTypeCode() == PrimitiveType.VOID)) {
	                    diag.mismatched("expect void but was " + JavaNameUtil.resolveQualifiedNameElseShort(found));
	                    return false;
	                }
	                diag.matched("is void");
	                return true;
	            }
	            
	            @Override
	            public void describeTo(Description desc) {
	                desc.text("returning void");
	            }
	        };
	    }
	   
	public AJMethodNode returning(final Matcher<Type> matcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				Type t = found.getAstNode().getReturnType2();
				return diag.tryMatch(this,t, matcher);
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("returning:", matcher);
            }
		});
		return this;
	}
	
	@SafeVarargs
	public static Matcher<JMethod> all(final Matcher<JMethod>... matchers) {
    	return Logical.and(matchers);
    }
	
	public static Matcher<JMethod> any() {
		return Logical.any();
	}
	
	public static Matcher<JMethod> none() {
		return Logical.none();
	}

	public AJMethodNode isNotConstructor() {
		isConstructor(false);
		return this;
	}

	public AJMethodNode isConstructor() {
		isConstructor(true);
		return this;
	}
	
	public AJMethodNode isConstructor(final boolean isCtor) {
		Matcher<JMethod> matcher = new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				if(found.isConstructor()){
				    diag.matched("is constructor");
				    return true;
				} else {
				    diag.mismatched("is not constructor");
				    return false;
				}
			}
			
			@Override
			public void describeTo(Description desc) {
			    desc.text(isCtor?"is constructor":"is not constructor");
			}
		};
		if(!isCtor ){
			matcher = Logical.not(matcher);
		}
		addMatcher(matcher);
		return this;
	}
	
	public AJMethodNode isNotPublic() {
	    notAccess(JAccess.PUBLIC);
        return this;
    }
	
	public AJMethodNode isPublic() {
	    access(JAccess.PUBLIC);
	    return this;
	}
	
	public AJMethodNode isNotPrivate() {
	    notAccess(JAccess.PRIVATE);
        return this;
    }
    
	public AJMethodNode access(final JAccess access) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.getModifiers().isAccess(access);
			}
		});
		return this;
	}

	   public AJMethodNode notAccess(final JAccess access) {
	        addMatcher(new AbstractNotNullMatcher<JMethod>() {
                @Override
	            public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
	                return !found.getModifiers().isAccess(access);
	            }
                
                @Override
                public void describeTo(Description desc) {
                    desc.text("is not " + access.name());
                }
	        });
	        return this;
	    }

	public AJMethodNode isStatic() {
		return isStatic(true);
	}
	
	public AJMethodNode isNotStatic() {
        return isStatic(false);
    }
	
	public AJMethodNode isStatic(final boolean isStatic) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.getModifiers().isStatic(isStatic);
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.text("is " + (isStatic?"":" not ")+ " static");
            }
		});
		return this;
	}
	
	public AJMethodNode modifier(final Matcher<JModifier> matcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return diag.tryMatch(this,found.getModifiers(), matcher);
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("modifier:", matcher);
            }
		});
		return this;
	}
	
	public <A extends Annotation> AJMethodNode methodAnnotation(final Class<A> annotationClass) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.getAnnotations().contains(annotationClass);
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("with annotation:", annotationClass.getName());
            }
		});
		return this;
	}

	public <A extends Annotation> AJMethodNode parameterAnnotation(final Class<A> annotationClass) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.hasParameterAnnotationOfType(annotationClass);
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("with parameter annotation:", annotationClass.getName());
            }
		});
		return this;
	}

	public AJMethodNode numArgs(final int numArgs) {
		numArgs(AnInt.equalTo(numArgs));
		return this;
	}

	public AJMethodNode numArgs(final Matcher<Integer> numArgMatcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return numArgMatcher.matches(found.getAstNode().parameters().size());
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("num args:", numArgMatcher);
            }
		});
		return this;
	}

	public AJMethodNode nameAndArgSignature(JMethod method) {
		final String name = method.getName();
		final int numArgs = method.getAstNode().typeParameters().size();
		final String sig = method.getClashDetectionSignature();

		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				//test using the quickest and least resource intensive matches first
				return numArgs == found.getAstNode().typeParameters().size() 
					&& name.equals(found.getName()) 
					&& sig.equals(found.getClashDetectionSignature());
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("with signature:", sig);
            }
		});
		return this;
	}
}
