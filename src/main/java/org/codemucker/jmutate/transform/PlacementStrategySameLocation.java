package org.codemucker.jmutate.transform;

import java.util.List;

import org.codemucker.jmutate.PlacementStrategy;
import org.eclipse.jdt.core.dom.ASTNode;

class PlacementStrategySameLocation implements PlacementStrategy {

	private final PlacementStrategy fallbackStrategy;
	private final ASTNode afterNode;
	
	public PlacementStrategySameLocation(PlacementStrategy fallbackStrategy,ASTNode afterNode) {
		super();
		this.fallbackStrategy = fallbackStrategy;
		this.afterNode = afterNode;
	}

	@Override
	public int findIndexToPlaceInto(ASTNode nodeToInsert, List<ASTNode> nodes) {
		if(afterNode != null){
			for(int i = 0; i < nodes.size();i++){
				if(nodes.get(i)== afterNode){
					return i+1;
				}
			}
		}
		return fallbackStrategy.findIndexToPlaceInto(nodeToInsert, nodes);
	}
}