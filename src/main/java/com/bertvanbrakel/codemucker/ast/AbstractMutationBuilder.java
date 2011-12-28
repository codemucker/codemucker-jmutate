package com.bertvanbrakel.codemucker.ast;

import org.eclipse.jdt.core.dom.ASTNode;


public abstract class AbstractMutationBuilder<T> implements CodeMutation<T> {

	private String snippet;
	private boolean errorOnExisting;
	private boolean isReplace;
	private InsertionStrategy insertionStrategy;

	private static final DefaultStrategyProvider PROVIDER = new DefaultStrategyProvider();
	
	public AbstractMutationBuilder<T> strategyCtor(){
		return insertionStrategy(PROVIDER.getCtorStrategy());
	}
	
	public AbstractMutationBuilder<T> strategyFeild(){
		return insertionStrategy(PROVIDER.getFieldStrategy());
	}
	
	public AbstractMutationBuilder<T> strategyMethod(){
		return insertionStrategy(PROVIDER.getMethodStrategy());
	}
	
	public AbstractMutationBuilder<T> strategyClass(){
		return insertionStrategy(PROVIDER.getClassStrategy());
	}
	
	public AbstractMutationBuilder<T> errorOnExisting() {
    	return errorOnExisting(true);
    }
	
	public AbstractMutationBuilder<T> errorOnExisting(boolean errorOnExiting) {
    	setErrorOnExisting(errorOnExiting);
    	return this;
    }

	public AbstractMutationBuilder<T> insertionStrategy(InsertionStrategy insertionStrategy) {
    	setInsertionStrategy(insertionStrategy);
    	return this;
	}

	public AbstractMutationBuilder<T> snippet(String snippet) {
    	setSnippet(snippet);
    	return this;
	}

	public AbstractMutationBuilder<T> replace() {
    	return replace(true);
	}
	
	public AbstractMutationBuilder<T> replace(boolean isReplace) {
    	setReplace(isReplace);
    	return this;
	}

	public void setErrorOnExisting(boolean errorOnExiting) {
    	this.errorOnExisting = errorOnExiting;
    }

	public void setInsertionStrategy(InsertionStrategy insertionStrategy) {
    	this.insertionStrategy = insertionStrategy;
    }

	public void setSnippet(String snippet) {
    	this.snippet = snippet;
    }

	public void setReplace(boolean isReplace) {
    	this.isReplace = isReplace;
    }

	@Override
	public InsertionStrategy getInsertionStrategy() {
    	return insertionStrategy;
	}
	
	@Override
    public String getSnippet() {
	    return snippet;
    }

	@Override
    public boolean isErrorOnExisting() {
	    return errorOnExisting;
    }

	@Override
    public boolean isReplace() {
	    return isReplace;
    }

}
