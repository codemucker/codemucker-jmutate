package org.codemucker.jmutate.ast.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractMatcher;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JModifier;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.base.Predicate;

public class AJField extends ObjectMatcher<JField>{

	private static final Matcher<JField> MATCH_ANY  = new AbstractNotNullMatcher<JField>() {
		@Override
		public boolean matchesSafely(JField found, MatchDiagnostics diag) {
			return true;
		}
	};
	
	/**
	 * synonym for with()
	 * @return
	 */
	public static AJField that(){
		return with();
	}
	
	public static AJField with(){
		return new AJField();
	}
	
	public AJField(){
	    super(JField.class);
	}

	public Matcher<ASTNode> toAstNodeMatcher(){
		final AJField self = this;
		return new AbstractMatcher<ASTNode>(){
			@Override
			protected boolean matchesSafely(ASTNode actual,MatchDiagnostics diag) {
				if(JField.is(actual)){
					return self.matches(JField.from(actual),diag);	
				} else {
					return false;
				}
			}
		};
	}
	
	public AJField field(Predicate<JField> predicate){
		predicate(predicate);
		return this;
	}

	public static Matcher<JField> any(){
		return MATCH_ANY;
	}

	public AJField ofType(final Matcher<Type> typeMatcher){
		addMatcher(new AbstractNotNullMatcher<JField>() {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				return typeMatcher.matches(found.getAstNode().getType());
			}
			
			@Override
			public void describeTo(Description desc) {
			    desc.value("field type", typeMatcher);
			}
		});
		return this;
	}
	
	public AJField name(final String antPattern){
		name(AString.matchingAntPattern(antPattern));
		return this;
	}
	
	public AJField name(final Matcher<String> matcher){
		addMatcher(new AbstractMatcher<JField>(AllowNulls.NO) {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				for (String name : found.getNames()) {
					if (matcher.matches(name)) {
						return true;
					}
				}
				return false;
			}
			
			@Override
			public void describeTo(Description desc) {
				desc.value("name", matcher);
			}
		});
		return this;
	}
	
    public <A extends Annotation> AJField annotation(final Class<A> annotationClass) {
        annotation(AJAnnotation.with().fullName(annotationClass));
        return this;
    }

    public AJField annotation(final Matcher<JAnnotation> matcher) {
        addMatcher(new AbstractNotNullMatcher<JField>() {
            @Override
            public boolean matchesSafely(JField found, MatchDiagnostics diag) {
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

    public AJField isPublic(){
		access(JAccess.PUBLIC);
		return this;
	}
    
    public AJField isPrivate(){
		access(JAccess.PRIVATE);
		return this;
	}
    
	public AJField access(final JAccess access){
		addMatcher(new AbstractNotNullMatcher<JField>() {
			@Override
			public boolean matchesSafely(JField found, MatchDiagnostics diag) {
				return found.isAccess(access);
			}
			
			@Override
			public void describeTo(Description desc) {
			    desc.text("access " + access.name());
			}
		});
		return this;
	}
	
	public AJField isStatic(){
		isStatic(true);
		return this;
	}
	
	public AJField isStatic(boolean b){
		modifier(AJModifier.that().isStatic(b));
		return this;
	}
	
	public AJField isFinal(){
		isFinal(true);
		return this;
	}
	
	public AJField isFinal(boolean b){
		modifier(AJModifier.that().isFinal(b));
		return this;
	}
	
	public AJField modifier(final Matcher<JModifier> matcher){
        addMatcher(new AbstractNotNullMatcher<JField>() {
            @Override
            public boolean matchesSafely(JField found, MatchDiagnostics diag) {
                return diag.tryMatch(this, found.getModifiers(), matcher);
            }
            
            @Override
            public void describeTo(Description desc) {
                desc.value("modifer ", matcher);
            }
        });
        return this;
    }
}
