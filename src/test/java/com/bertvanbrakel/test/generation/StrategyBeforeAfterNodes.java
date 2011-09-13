package com.bertvanbrakel.test.generation;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class StrategyBeforeAfterNodes implements InsertionStrategy {

	private final Collection<Class<?>> afterNodesOfType;
	private final Collection<Class<?>> beforeNodesOfType;

	public StrategyBeforeAfterNodes(Collection<Class<?>> afterNodesOfType, Collection<Class<?>> beforeNodesOfType) {
	    super();
	    this.afterNodesOfType = afterNodesOfType;
	    this.beforeNodesOfType = beforeNodesOfType;
    }

	@Override
    public int findIndex(List<ASTNode> body) {
	    return findIndexToInsertAt(body, afterNodesOfType, beforeNodesOfType);
    }
	
	private int findIndexToInsertAt(List<ASTNode> nodes, Collection<Class<?>> afterNodesOfType,
	        Collection<Class<?>> beforeNodesOfType) {
		int index = findLastIndexOf(nodes, afterNodesOfType);
		if (index != INDEX_NOT_FOUND) {
			index++;
		} else {
			index = findFirstIndexOf(nodes, beforeNodesOfType);
		}
		if (index == INDEX_NOT_FOUND) {
			index = 0;
		}
		return index;
	}

	private int findLastIndexOf(List<ASTNode> nodes, Collection<Class<?>> nodeTypes) {
		int idx = 0;
		int last = INDEX_NOT_FOUND;
		for (ASTNode node : nodes) {
			if (nodeTypes.contains(node.getClass())) {
				last = idx;
			}
			idx++;
		}
		return last;
	}

	private int findFirstIndexOf(List<ASTNode> nodes, Collection<Class<?>> nodeTypes) {
		int idx = 0;
		for (ASTNode node : nodes) {
			if (nodeTypes.contains(node.getClass())) {
				return idx;
			}
			idx++;
		}
		return INDEX_NOT_FOUND;
	}

}
