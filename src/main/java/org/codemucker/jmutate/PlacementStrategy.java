package org.codemucker.jmutate;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public interface PlacementStrategy {
	
	public static final int INDEX_NOT_FOUND = -1;

	
	/**
	 * Find the index to place a node into. Anything less than zero indicates no position could be found. This could indicate to callers
	 * to use an alternative strategy.
	 * @param nodeToInsert TODO
	 * @param body
	 * 
	 * @return
	 */
	public int findIndexToPlaceInto(ASTNode nodeToInsert, List<ASTNode> nodes);
}
