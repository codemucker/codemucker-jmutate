package com.bertvanbrakel.codemucker.ast;

import org.eclipse.jdt.core.dom.ASTNode;

public interface Flattener {

	public String flatten(ASTNode node);

	//public void flatten(ASTNode node, StringBuilder sb);
}
