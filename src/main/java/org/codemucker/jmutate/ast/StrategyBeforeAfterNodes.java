package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.codemucker.jmutate.PlacementStrategy;
import org.eclipse.jdt.core.dom.ASTNode;


public class StrategyBeforeAfterNodes implements PlacementStrategy {

	private final Collection<Class<?>> afterNodesOfType;
	private final Collection<Class<?>> beforeNodesOfType;

	private StrategyBeforeAfterNodes(Collection<Class<?>> afterNodesOfType, Collection<Class<?>> beforeNodesOfType) {
	    super();
	    this.afterNodesOfType = newArrayList(afterNodesOfType);
	    this.beforeNodesOfType = newArrayList(beforeNodesOfType);
    }

	public static Builder with(){
		return new Builder();
	}
	
	@Override
    public int findIndexToPlaceInto(ASTNode nodeToInsert, List<ASTNode> nodes) {
	    return findIndexToInsertAt(nodes, afterNodesOfType, beforeNodesOfType);
    }
	
	private int findIndexToInsertAt(List<ASTNode> nodes, Collection<Class<?>> afterNodesOfType, Collection<Class<?>> beforeNodesOfType) {
		int index = findLastIndexOfTypeIn(afterNodesOfType, nodes);
		if (index != PlacementStrategy.INDEX_NOT_FOUND) {
			index++;
		} else {
			index = findFirstIndexOfTypeIn(beforeNodesOfType, nodes);
		}
		if (index == PlacementStrategy.INDEX_NOT_FOUND) {
			index = 0;
		}
		return index;
	}

	private int findLastIndexOfTypeIn(Collection<Class<?>> nodeTypes,Collection<ASTNode> nodes) {
		int idx = 0;
		int last = PlacementStrategy.INDEX_NOT_FOUND;
		for (ASTNode node : nodes) {
			if (nodeTypes.contains(node.getClass())) {
				last = idx;
			}
			idx++;
		}
		return last;
	}

	private int findFirstIndexOfTypeIn(Collection<Class<?>> nodeTypes,Collection<ASTNode> nodes) {
		int idx = 0;
		for (ASTNode node : nodes) {
			if (nodeTypes.contains(node.getClass())) {
				return idx;
			}
			idx++;
		}
		return PlacementStrategy.INDEX_NOT_FOUND;
	}
	
	public static class Builder {
		
		private final Collection<Class<?>> afterNodesOfType = newArrayList();
		private final Collection<Class<?>> beforeNodesOfType = newArrayList();
		
		public StrategyBeforeAfterNodes build(){
			return new StrategyBeforeAfterNodes(afterNodesOfType,beforeNodesOfType);
		}
		
		public Builder beforeNodes(Class<?>... nodes){
			beforeNodesOfType.addAll(Arrays.asList(nodes));
			return this;
		}

		public Builder afterNodes(Class<?>... nodes){
			afterNodesOfType.addAll(Arrays.asList(nodes));
			return this;
		}
	}

}
