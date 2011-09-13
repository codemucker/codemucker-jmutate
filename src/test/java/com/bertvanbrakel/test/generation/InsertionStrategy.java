package com.bertvanbrakel.test.generation;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public interface InsertionStrategy {
	
	public static final int INDEX_NOT_FOUND = -1;

	
	/**
	 * Find the indx to insert at. Anything less than zero indicates no position could be found
	 * @param body
	 * @return
	 */
	public int findIndex(List<ASTNode> body);
}
