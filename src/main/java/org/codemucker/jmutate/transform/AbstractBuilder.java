package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;
import org.codemucker.jmutate.ast.ContextNames;

import com.google.inject.Inject;
import com.google.inject.name.Named;
/**
 * Provides the basic properties to set for all transforms 
 *
 * @param <S>
 */
public abstract class AbstractBuilder<S extends AbstractBuilder<S>> {

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
	
	public S markedGenerated(boolean markedGenerated) {
		this.markedGenerated = markedGenerated;
		return self();
	}

	public S context(MutateContext context) {
		this.context = context;
		return self();
	}

	public String getPattern() {
    	return pattern;
    }

	public S pattern(String pattern) {
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
	private S self() {
		return (S) this;
	}	
}
