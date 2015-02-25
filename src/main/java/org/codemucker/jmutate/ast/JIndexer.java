package org.codemucker.jmutate.ast;

import org.codemucker.jfind.FindResult;

public interface JIndexer {

	public FindResult<Object> find(String expression);
}
