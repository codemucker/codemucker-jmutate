package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JType;

/**
 * Inserts generic nodes using an insertion strategy
 */
public class NodeInserter {

	private JType target;
	private ASTNode nodeToInsert;
	private PlacementStrategy strategy;
	
	public void insert() {
		checkNotNull(target, "expect target");
		checkNotNull(nodeToInsert, "expect node to insert");
		checkNotNull(strategy,"expect strategy");
		
		ASTNode copy = ASTNode.copySubtree(target.getAst(), nodeToInsert);
		List<ASTNode> body = target.getBodyDeclarations();
		int index = strategy.findIndexToPlace(body);
		if( index < 0){
			throw new CodemuckerException("Insertion strategy %s couldn't find an index to insert %s into", strategy, nodeToInsert);
		}
		body.add(index, copy);
	}

	public NodeInserter setTarget(JType javaType) {
    	this.target = javaType;
    	return this;
	}

	public NodeInserter setNodeToInsert(ASTNode child) {
    	this.nodeToInsert = child;
    	return this;
	}

	public NodeInserter setStrategy(PlacementStrategy strategy) {
    	this.strategy = strategy;
    	return this;
    }
}
