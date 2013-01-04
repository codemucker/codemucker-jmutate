package com.bertvanbrakel.codemucker.ast;

import org.eclipse.jdt.core.dom.ASTNode;

public interface AstNodeFlattener {
	public String flatten(ASTNode node);
}
