package org.codemucker.jmutate.ast;

import org.eclipse.jdt.core.dom.ASTNode;

public interface AstNodeFlattener {
	public String flatten(ASTNode node);
}
