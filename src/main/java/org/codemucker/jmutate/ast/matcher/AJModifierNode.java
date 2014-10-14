package org.codemucker.jmutate.ast.matcher;

import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JModifier;

public class AJModifierNode extends ObjectMatcher<JModifier> {

    private static final Matcher<JModifier> MATCH_ANY = new AbstractNotNullMatcher<JModifier>() {
        @Override
        public boolean matchesSafely(JModifier found, MatchDiagnostics diag) {
            return true;
        }
    };

    /**
     * synonym for with()
     * 
     * @return
     */
    public static AJModifierNode that() {
        return with();
    }

    public static AJModifierNode with() {
        return new AJModifierNode();
    }

    public AJModifierNode() {
        super(JField.class);
    }

    public static Matcher<JModifier> any() {
        return MATCH_ANY;
    }

    /**
     * Shorthand for 
     *  isNotNative(),
     *  isNotStatic(),
     *  isNotFinal(),
     *  isNotTransient()
     * 
     * @return
     */
    public AJModifierNode isMutableInstance() {
        isNotNative();
        isNotStatic();
        isNotTransient();
        isNotFinal();
        return this;
    }

    public AJModifierNode isFinal() {
        isFinal(true);
        return this;
    }

    public AJModifierNode isNotFinal() {
        isFinal(false);
        return this;
    }
    
    public AJModifierNode isFinal(final boolean b) {
        addMatcher(new AbstractNotNullMatcher<JModifier>() {
            @Override
            public boolean matchesSafely(JModifier found, MatchDiagnostics diag) {
                return found.isFinal(b);
            }

            @Override
            public void describeTo(Description desc) {
                desc.text("is" + (b ? "" : " not") + " final");
            }
        });
        return this;
    }

    
    public AJModifierNode isStatic() {
        isStatic(true);
        return this;
    }

    public AJModifierNode isNotStatic() {
        isStatic(false);
        return this;
    }
    
    public AJModifierNode isStatic(final boolean b) {
        addMatcher(new AbstractNotNullMatcher<JModifier>() {
            @Override
            public boolean matchesSafely(JModifier found, MatchDiagnostics diag) {
                return found.isStatic(b);
            }

            @Override
            public void describeTo(Description desc) {
                desc.text("is" + (b ? "" : " not") + " static");
            }
        });
        return this;
    }

    public AJModifierNode isTransient() {
        isTransient(true);
        return this;
    }

    public AJModifierNode isNotTransient() {
        isTransient(false);
        return this;
    }

    public AJModifierNode isTransient(final boolean b) {
        addMatcher(new AbstractNotNullMatcher<JModifier>() {
            @Override
            public boolean matchesSafely(JModifier found, MatchDiagnostics diag) {
                return found.isTransient(b);
            }

            @Override
            public void describeTo(Description desc) {
                desc.text("is" + (b ? "" : " not") + "  transient");
            }
        });
        return this;
    }

    public AJModifierNode isNative() {
        isNative(true);
        return this;
    }

    public AJModifierNode isNotNative() {
        isNative(false);
        return this;
    }

    public AJModifierNode isNative(final boolean b) {
        addMatcher(new AbstractNotNullMatcher<JModifier>() {
            @Override
            public boolean matchesSafely(JModifier found, MatchDiagnostics diag) {
                return found.isNative(b);
            }

            @Override
            public void describeTo(Description desc) {
                desc.text("is" + (b ? "" : " not") + "  native");
            }
        });
        return this;
    }

    public AJModifierNode isVolatile() {
        isVolatile(true);
        return this;
    }

    public AJModifierNode isNotVolatile() {
        isVolatile(false);
        return this;
    }

    public AJModifierNode isVolatile(final boolean b) {
        addMatcher(new AbstractNotNullMatcher<JModifier>() {
            @Override
            public boolean matchesSafely(JModifier found, MatchDiagnostics diag) {
                return found.isVolatile(b);
            }

            @Override
            public void describeTo(Description desc) {
                desc.text("is" + (b ? "" : " not") + "  volatile");
            }
        });
        return this;
    }

    public AJModifierNode isSynchronized() {
        isSynchronized(true);
        return this;
    }

    public AJModifierNode isNotSynchronized() {
        isSynchronized(false);
        return this;
    }

    public AJModifierNode isSynchronized(final boolean b) {
        addMatcher(new AbstractNotNullMatcher<JModifier>() {
            @Override
            public boolean matchesSafely(JModifier found, MatchDiagnostics diag) {
                return found.isSynchronized(b);
            }

            @Override
            public void describeTo(Description desc) {
                desc.text("is" + (b ? "" : " not") + "  isSynchronized");
            }
        });
        return this;
    }

}
