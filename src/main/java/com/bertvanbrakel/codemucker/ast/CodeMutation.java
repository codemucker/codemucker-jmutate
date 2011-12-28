package com.bertvanbrakel.codemucker.ast;


interface CodeMutation<T> {
	String getSnippet();
	boolean isErrorOnExisting();
	boolean isReplace();
	InsertionStrategy getInsertionStrategy();
	void apply(final T applyTo);
}