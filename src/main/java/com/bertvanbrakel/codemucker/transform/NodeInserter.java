package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JType;
import com.google.common.base.Preconditions;

/**
 * Inserts generic nodes using an insertion placement strategy
 */
public class NodeInserter {

	private List<ASTNode> nodesToInsertInto;
	private AST ast;
	private ASTNode nodeToInsert;
	private PlacementStrategy strategy;
	
	/**
	 * Insert the node.
	 * 
	 * @return the node which was inserted
	 */
	public ASTNode insert() {
		checkNotNull(nodesToInsertInto, "expect nodes to insert into");
		checkNotNull(nodeToInsert, "expect node to insert");
		checkNotNull(strategy,"expect strategy");
		
		ASTNode clonedNode = ASTNode.copySubtree(ast, nodeToInsert);

		int index = strategy.findIndexToPlaceInto(clonedNode, nodesToInsertInto);
		if (index < 0) {
			throw new CodemuckerException("Insertion strategy %s couldn't find an index to insert %s into", strategy, nodeToInsert);
		}
		nodesToInsertInto.add(index, clonedNode);
		return clonedNode;
	}

	public NodeInserter setTargetToInsertInto(JType javaType) {
    	this.nodesToInsertInto = javaType.getBodyDeclarations();
    	this.ast = javaType.getAst();
    	return this;
	}

	public NodeInserter setNodesToInsertInto(AST ast, List<ASTNode> nodesToInsertInto) {
    	this.nodesToInsertInto = Preconditions.checkNotNull(nodesToInsertInto, "expect non null list of nodes to insert into");
    	this.ast = Preconditions.checkNotNull(ast,"expect non null ast");
    	return this;
	}

	public NodeInserter setNodeToInsert(ASTNode childNode) {
    	this.nodeToInsert = Preconditions.checkNotNull(childNode,"expect non null node to insert");
    	return this;
	}

	public NodeInserter setStrategy(PlacementStrategy strategy) {
    	this.strategy = Preconditions.checkNotNull(strategy, "expect non null strategy");
    	return this;
    }	
}
