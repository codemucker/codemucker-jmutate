package org.codemucker.jmutate.ast.matcher;

import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JModifier;

public class AJModifier extends ObjectMatcher<JModifier> {

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
    public static AJModifier that() {
        return with();
    }

    public static AJModifier with() {
        return new AJModifier();
    }

    public AJModifier() {
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
    public AJModifier isMutableInstance() {
        isNotNative();
        isNotStatic();
        isNotTransient();
        isNotFinal();
        return this;
    }

    public AJModifier isFinal() {
        isFinal(true);
        return this;
    }

    public AJModifier isNotFinal() {
        isFinal(false);
        return this;
    }
    
    public AJModifier isFinal(final boolean b) {
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

    
    public AJModifier isStatic() {
        isStatic(true);
        return this;
    }

    public AJModifier isNotStatic() {
        isStatic(false);
        return this;
    }
    
    public AJModifier isStatic(final boolean b) {
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

    public AJModifier isTransient() {
        isTransient(true);
        return this;
    }

    public AJModifier isNotTransient() {
        isTransient(false);
        return this;
    }

    public AJModifier isTransient(final boolean b) {
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

    public AJModifier isNative() {
        isNative(true);
        return this;
    }

    public AJModifier isNotNative() {
        isNative(false);
        return this;
    }

    public AJModifier isNative(final boolean b) {
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

    public AJModifier isVolatile() {
        isVolatile(true);
        return this;
    }

    public AJModifier isNotVolatile() {
        isVolatile(false);
        return this;
    }

    public AJModifier isVolatile(final boolean b) {
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

    public AJModifier isSynchronized() {
        isSynchronized(true);
        return this;
    }

    public AJModifier isNotSynchronized() {
        isSynchronized(false);
        return this;
    }

    public AJModifier isSynchronized(final boolean b) {
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
