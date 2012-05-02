package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractPatternBuilder<S extends AbstractPatternBuilder<S>> {

	private boolean markedGenerated;
	private String pattern;
	private MutationContext context;
	
	protected void checkFieldsSet(){
		checkState(context != null, "expect mutation context");
		if(markedGenerated){
			checkState(!StringUtils.isBlank(getPattern()), "expect non empty/null pattern when marking as generatedx");
		}
	}
	
	public S setMarkedGenerated(boolean markedGenerated) {
		this.markedGenerated = markedGenerated;
		return self();
	}

	public S setContext(MutationContext context) {
		this.context = context;
		return self();
	}

	public String getPattern() {
    	return pattern;
    }

	public S setPattern(String pattern) {
    	this.pattern = pattern;
    	return self();
	}

	public boolean isMarkedGenerated() {
    	return markedGenerated;
    }

	public MutationContext getContext() {
    	return context;
    }
	
	@SuppressWarnings("unchecked")
	private S self() {
		return (S) this;
	}	
}