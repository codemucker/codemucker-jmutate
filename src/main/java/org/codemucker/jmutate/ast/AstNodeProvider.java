package org.codemucker.jmutate.ast;

import org.eclipse.jdt.core.dom.ASTNode;

public interface AstNodeProvider<T extends ASTNode> {

	public T getAstNode();
}
