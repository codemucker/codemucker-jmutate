package com.bertvanbrakel.codemucker.ast;

import org.eclipse.jdt.core.dom.ASTNode;

public class SimpleFlattener implements Flattener {

	@Override
	public String flatten(ASTNode node) {
		return JAstFlattener.asString(node);
	}

	@Override
	public void flatten(ASTNode node, StringBuilder sb) {
		JAstFlattener visitor = new JAstFlattener(sb);
		node.accept(visitor);
	}
}