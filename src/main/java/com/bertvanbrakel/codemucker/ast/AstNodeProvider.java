package com.bertvanbrakel.codemucker.ast;

import org.eclipse.jdt.core.dom.ASTNode;

public interface AstNodeProvider<T extends ASTNode> {

	public T getAstNode();
}
