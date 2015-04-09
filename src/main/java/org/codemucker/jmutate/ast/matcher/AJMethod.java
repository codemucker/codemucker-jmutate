package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.codemucker.jfind.matcher.AMethod;
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
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JModifier;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.ClassNameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.base.Predicate;


public class AJMethod extends ObjectMatcher<JMethod> {
	
	/**
	 * synonym for with()
	 * 
	 * @return
	 */
	public static AJMethod that(){
		return with();
	}
	
	public static AJMethod with(){
		return new AJMethod();
	}

	public AJMethod(){
	    super(JMethod.class);
	}
	
	public Matcher<ASTNode> toAstNodeMatcher(){
		final AJMethod self = this;
		return new AbstractMatcher<ASTNode>(){
			@Override
			protected boolean matchesSafely(ASTNode actual,MatchDiagnostics diag) {
				if(JMethod.is(actual)){
					return self.matches(JMethod.from(actual),diag);	
				} else {
					return false;
				}
			}
		};
	}
	
	public AJMethod method(Predicate<JMethod> predicate){
		predicate(predicate);
		return this;
	}

	/**
	 * Return a matcher which matches using the given ant style method name expression
	 * @param antPattern ant style pattern. E.g. *foo*bar??Ho
	 * @return
	 */
	public AJMethod nameMatchingAntPattern(final String antPattern) {
		name(AString.matchingAntPattern(antPattern));
		return this;
	}
	
	public AJMethod name(final String name) {
		name(AString.equalTo(name));
		return this;
	}
	
	public AJMethod name(final Matcher<String> matcher) {
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
	
	public AJMethod isNotVoidReturn() {
		isVoidReturn(false);
		return this;
    }
	
	public AJMethod isVoidReturn() {
		isVoidReturn(true);
	    return this;
	}
	
	public AJMethod isVoidReturn(boolean b) {
		if(b){
			addMatcher(newVoidReturnMatcher());	
		} else {
			addMatcher(Logical.not(newVoidReturnMatcher()));
		}
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
                desc.text("void return");
            }
        };
	}
	
	public AJMethod returnType(final Class<?> klass){
		returnType(NameUtil.compiledNameToSourceName(klass.getName()));
		return this;
	}
	
	public AJMethod returnType(final String fullName){
		returnType(AString.equalTo(fullName));
		return this;
	}
	
	public AJMethod returnType(final Matcher<String> fullNameMatcher){
		addMatcher(new AbstractMatcher<JMethod>() {
            @Override
            public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
            	return diag.tryMatch(this, found.getReturnTypeFullName(), fullNameMatcher);
            }
            
            @Override
            public void describeTo(Description desc) {
                desc.value("returning ",fullNameMatcher);
            }
        });
		return this;
	}
	
	private static Matcher<Type> newVoidTypeMatcher(){
        return new AbstractMatcher<Type>(AllowNulls.YES) {
            @Override
            public boolean matchesSafely(Type found, MatchDiagnostics diag) {
                if (found != null && !(found.isPrimitiveType() && ((PrimitiveType) found).getPrimitiveTypeCode() == PrimitiveType.VOID)) {
                    diag.mismatched("expect void but was " + NameUtil.resolveQualifiedNameElseShort(found));
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
   
	public AJMethod returning(final Matcher<Type> matcher) {
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

	public AJMethod isNotConstructor() {
		isConstructor(false);
		return this;
	}

	public AJMethod isConstructor() {
		isConstructor(true);
		return this;
	}
	
	public AJMethod isConstructor(final boolean isCtor) {
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
	
	public AJMethod isNotPublic() {
	    notAccess(JAccess.PUBLIC);
        return this;
    }
	
	public AJMethod isPublic() {
	    access(JAccess.PUBLIC);
	    return this;
	}
	
	public AJMethod isNotPrivate() {
	    notAccess(JAccess.PRIVATE);
        return this;
    }
    
	public AJMethod access(final JAccess access) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.getModifiers().isAccess(access);
			}
		});
		return this;
	}

	   public AJMethod notAccess(final JAccess access) {
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

	public AJMethod isStatic() {
		return isStatic(true);
	}
	
	public AJMethod isNotStatic() {
        return isStatic(false);
    }
	
	public AJMethod isStatic(final boolean isStatic) {
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
	
	public AJMethod modifier(final Matcher<JModifier> matcher) {
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

    public <A extends Annotation> AJMethod annotation(final Class<A> annotationClass) {
        annotation(AJAnnotation.with().fullName(annotationClass));
        return this;
    }

    public AJMethod annotation(final Matcher<JAnnotation> matcher) {
        addMatcher(new AbstractNotNullMatcher<JMethod>() {
            @Override
            public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
                return found.getAnnotations().contains(matcher);
            }

            @Override
            public void describeTo(Description desc) {
                // super.describeTo(desc);
                desc.value("with annotation", matcher);
            }
        });
        return this;
    }

    public <A extends Annotation> AJMethod parameterAnnotation(final Class<A> annotationClass) {
        parameterAnnotation(AJAnnotation.with().fullName(annotationClass));
        return this;
    }
    
	public AJMethod parameterAnnotation(final Matcher<JAnnotation> matcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return found.hasParameterAnnotation(matcher);
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("with parameter annotation:", matcher);
            }
		});
		return this;
	}

	public AJMethod numArgs(final int numArgs) {
		numArgs(AnInt.equalTo(numArgs));
		return this;
	}

	public AJMethod numArgs(final Matcher<Integer> numArgMatcher) {
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

	public AJMethod nameAndArgSignature(JMethod method) {
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
	
	public AJMethod nameAndArgSignature(final Matcher<String> matcher) {
		addMatcher(new AbstractNotNullMatcher<JMethod>() {
			@Override
			public boolean matchesSafely(JMethod found, MatchDiagnostics diag) {
				return diag.tryMatch(this, found.getClashDetectionSignature(), matcher);
			}
			
			@Override
            public void describeTo(Description desc) {
                desc.value("with signature:", matcher);
            }
		});
		return this;
	}
}
