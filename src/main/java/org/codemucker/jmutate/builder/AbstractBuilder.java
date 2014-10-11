package org.codemucker.jmutate.builder;

import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;
import org.codemucker.jmutate.MutateContext;
import org.codemucker.jmutate.ast.ContextNames;

import com.google.inject.Inject;
import com.google.inject.name.Named;
/**
 * Provides the basic properties to set for all transforms 
 *
 * @param <TSelf> the subclass type (self) so builder methods can return the subclass instead of the absract builder
 * @param <TBuild> the type of the object to build
 */
public abstract class AbstractBuilder<TSelf extends AbstractBuilder<TSelf,TBuild>,TBuild> implements Builder<TBuild> {

	@Inject
	private MutateContext context;
	
	/**
	 * Whether to mark the generated class with the fact it's been generated
	 */
	@Inject
	@Named(ContextNames.MARK_GENERATED)
	private boolean markedGenerated;
	
	/**
	 * The name of the pattern this builder generates
	 */
	private String pattern;
	
	protected void checkFieldsSet(){
		checkState(context != null, "expect mutation context");
		if(markedGenerated){
			checkState(!StringUtils.isBlank(getPattern()), "expect non empty/null pattern when marking as generated");
		}
	}
	
	public TSelf markedGenerated(boolean markedGenerated) {
		this.markedGenerated = markedGenerated;
		return self();
	}

	public TSelf context(MutateContext context) {
		this.context = context;
		return self();
	}

	public String getPattern() {
    	return pattern;
    }

	public TSelf pattern(String pattern) {
    	this.pattern = pattern;
    	return self();
	}

	public boolean isMarkedGenerated() {
    	return markedGenerated;
    }

	public MutateContext getContext() {
    	return context;
    }
	
	@SuppressWarnings("unchecked")
	private TSelf self() {
		return (TSelf) this;
	}	
}
