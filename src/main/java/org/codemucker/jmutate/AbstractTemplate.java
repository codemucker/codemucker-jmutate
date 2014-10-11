package org.codemucker.jmutate;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.codemucker.lang.annotation.NotThreadSafe;
import org.codemucker.lang.interpolator.Interpolator;

import com.google.common.base.Objects;

@NotThreadSafe
public abstract class AbstractTemplate<S extends AbstractTemplate<S>> implements Template {
    
    /**
     * Variables which will be used to interpolate the template content
     */
    private Map<String, Object> vars = newHashMap();
    
    /**
     * Template content
     */
    private StringBuilder buffer = new StringBuilder();

    /**
     * Set all the template variables in one go. Replaces all existing variables. Names are case sensitive
     * 
     * @param vars
     * @return
     */
    public S setVars(Map<String, ?> vars) {
        this.vars.clear();
        this.vars.putAll(vars);
        return self();
    }

    /**
     * Add all the given template variables. Any existing variables with the same name will be replaced. Names are case sensitive
     * 
     * @param vars
     * @return
     */
    public S addVars(Map<String, ?> vars) {
        this.vars.putAll(vars);
        return self();
    }

    /**
     * Shorthand for {@link #setVar(String, Object)}
     */
    public S v(String name, Object val) {
        setVar(name, val);
        return self();
    }

    /**
     * Set a template variable. Shorthand version is
     * {@link #setVar(String, Object)}. Name is case sensitive
     * 
     * @param name
     *            variable name, case sensitive.
     * @param val
     *            variable value
     * @return self for chaining
     */
    public S setVar(String name, Object val) {
        this.vars.put(name, val);
        return self();
    }

    /**
     * Replace the entire template contents with the given sequence
     * 
     * @param template
     * @return self for chaining
     */
    public S setTemplate(CharSequence template) {
        // or throw NPE?
        this.buffer.setLength(0);
        if (template != null) {
            this.buffer.append(template);
        }
        return self();
    }

    /**
     * Shorthand for {@link #println(char)}
     */
    public S pl(char c) {
        println(c);
        return self();
    }

    /**
     * Append a character and insert a new line
     * 
     * @param c
     * @return self for chaining
     */
    public S println(char c) {
        print(c);
        println();
        return self();
    }

    /**
     * Shorthand for {@link #println(CharSequence)}
     */
    public S pl(CharSequence template) {
        println(template);
        return self();
    }

    /**
     * Append a character sequence and insert a new line
     * 
     * @param c
     * @return self for chaining
     */
    public S println(CharSequence template) {
        print(template);
        println();
        return self();
    }

    /**
     * Shorthand for {@link #println()}
     */
    public S pl() {
        println();
        return self();
    }

    /**
     * Append a newline to the template
     *  
     * @return self for chaining
     */
    public S println() {
        this.buffer.append("\n");
        return self();
    }

    /**
     * Shorthand for {@link #print(char)}
     */
    public S p(char c) {
        print(c);
        return self();
    }

    /**
     * Shorthand for {@link #print(CharSequence)}
     */
    public S p(CharSequence c) {
        print(c);
        return self();
    }

    /**
     * Append the given char to the end of the template
     * 
     * @param c
     * @return self for chaining
     */
    public S print(char c) {
        this.buffer.append(c);
        return self();
    }

    /**
     * Append the given char sequence to the end of the template
     * 
     * @param s
     * @return self for chaining
     */

    public S print(CharSequence s) {
        this.buffer.append(s);
        return self();
    }

    /**
     * Replace all occurrences of the given character in the template with the
     * other
     * 
     * @param oldChar
     *            the character to replace
     * @param newChar
     *            what to replace with
     * @return self for chaining
     */
    public S replace(char oldChar, char newChar) {
        String s = this.buffer.toString();
        s = s.replace(oldChar, newChar);
        this.buffer.setLength(0);
        this.buffer.append(s);

        return self();
    }

    /**
     * Replace a char sequence in the template with another one
     * 
     * @param oldSequence what to replace
     * @param newSequence what to replace with
     * @return self for chaining
     */
    public S replace(CharSequence oldSequence, CharSequence newSequence) {
        String s = this.buffer.toString();
        s = s.replace(oldSequence, newSequence);
        this.buffer.setLength(0);
        this.buffer.append(s);

        return self();
    }

    @SuppressWarnings("unchecked")
    private S self() {
        return (S) this;
    }
    
    @Override
    public CharSequence interpolateTemplate() {
        return Interpolator.interpolate(buffer, vars);
    }

    protected String interpolateSnippet(String snippetText) {
        return (String) Interpolator.interpolate(snippetText, vars);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("template", buffer).add("vars", vars).toString();
    }
}