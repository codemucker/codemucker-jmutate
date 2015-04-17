package org.codemucker.jmutate;

import static com.google.common.collect.Maps.newHashMap;

import java.util.HashMap;
import java.util.Map;

import org.codemucker.lang.annotation.NotThreadSafe;
import org.codemucker.lang.interpolator.Interpolator;

import com.google.common.base.Objects;

@NotThreadSafe
public abstract class AbstractTemplate<TSelf extends AbstractTemplate<TSelf>> implements Template {
    
    public static final String NL = System.getProperty("line.separator");
    
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
    public TSelf setVars(Map<String, ?> vars) {
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
    public TSelf addVars(Map<String, ?> vars) {
        this.vars.putAll(vars);
        return self();
    }

    /**
     * Shorthand for {@link #setVar(String, Object)}
     */
    public TSelf var(String name, Object val) {
        setVar(name, val);
        return self();
    }

    /**
     * Set a template variable. Shorthand version is {@link #var(String, Object)}. Name is case sensitive
     * 
     * @param name
     *            variable name, case sensitive.
     * @param val
     *            variable value
     * @return self for chaining
     */
    public TSelf setVar(String name, Object val) {
        this.vars.put(name, val);
        return self();
    }
    
	protected Map<String, Object> cloneVars() {
		return new HashMap<>(this.vars);
	}

    /**
     * Replace the entire template contents with the given sequence
     * 
     * @param template
     * @return self for chaining
     */
    public TSelf setTemplate(CharSequence template) {
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
    public TSelf pl(char c) {
        println(c);
        return self();
    }

    /**
     * Append a character and insert a new line
     * 
     * @param c
     * @return self for chaining
     */
    public TSelf println(char c) {
        print(c);
        println();
        return self();
    }

    /**
     * Shorthand for {@link #println(CharSequence)}
     */
    public TSelf pl(CharSequence template) {
        println(template);
        return self();
    }
    
    /**
     * @see {@link #p(CharSequence, Object...)}
     */
    public TSelf pl(CharSequence template, Object...params) {
        println(template,params);
        return self();
    }

    /**
     * Add the given char sequence, interpolating it with the provided parameters, then post fix with newline
     * 
     * @param template
     * @param params a list of alternating name/value pairs. E.g.  "var1","val1", "var2", "val2"
     * @return self
     */
    public TSelf println(CharSequence template, Object...params) {
        println(interpolateSnippet(template, params));
        return self();
    }
    
    /**
     * Append a character sequence and insert a new line
     * 
     * @param c
     * @return self for chaining
     */
    public TSelf println(CharSequence template) {
        print(template);
        println();
        return self();
    }

    /**
     * Shorthand for {@link #println()}
     */
    public TSelf pl() {
        println();
        return self();
    }

    /**
     * Append a newline to the template
     *  
     * @return self for chaining
     */
    public TSelf println() {
        this.buffer.append(NL);
        return self();
    }

    /**
     * @see {@link #p(CharSequence, Object...)}
     */
    public TSelf p(CharSequence template, Object...params) {
        print(template,params);
        return self();
    }

    /**
     * Add the given char sequence, interpolating it with the provided parameters.
     * 
     * @param template
     * @param params a list of alternating name/value pairs. E.g.  "var1","val1", "var2", "val2"
     * @return self
     */
    public TSelf print(CharSequence template, Object...params) {
        print(interpolateSnippet(template, params));
        return self();
    }
    
    /**
     * Shorthand for {@link #print(char)}
     */
    public TSelf p(char c) {
        print(c);
        return self();
    }

    /**
     * Shorthand for {@link #print(CharSequence)}
     */
    public TSelf p(CharSequence c) {
        print(c);
        return self();
    }

    /**
     * Append the given char to the end of the template
     * 
     * @param c
     * @return self for chaining
     */
    public TSelf print(char c) {
        this.buffer.append(c);
        return self();
    }

    /**
     * Append the given char sequence to the end of the template
     * 
     * @param s
     * @return self for chaining
     */

    public TSelf print(CharSequence s) {
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
    public TSelf replace(char oldChar, char newChar) {
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
    public TSelf replace(CharSequence oldSequence, CharSequence newSequence) {
        String s = this.buffer.toString();
        s = s.replace(oldSequence, newSequence);
        this.buffer.setLength(0);
        this.buffer.append(s);

        return self();
    }

    @SuppressWarnings("unchecked")
    private TSelf self() {
        return (TSelf) this;
    }

    protected String interpolateTemplateAsString() {
        return (String)interpolateTemplate();
    }

    @Override
    public CharSequence interpolateTemplate() {
        CharSequence out = Interpolator.interpolate(buffer, vars);
        return out;
    }

    protected CharSequence interpolateSnippet(CharSequence s, Object... params) {
        Map<String, Object> vars = new HashMap<>();
        if (params != null) {
            for (int i = 0; i < params.length; i = i + 2) {
                if (i + 1 < params.length) {
                    vars.put(params[i].toString(), params[i + 1]);
                } else {
                    throw new IllegalArgumentException("expect even number of name/value pairs");
                }
            }
        }
        return Interpolator.interpolate(s, vars);
    }

    protected String interpolateSnippet(String snippetText) {
        return (String) Interpolator.interpolate(snippetText, vars);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("template", buffer).add("vars", vars).toString();
    }
}