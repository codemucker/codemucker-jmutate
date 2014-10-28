package org.codemucker.jmutate.ast;

import org.eclipse.jdt.core.dom.ASTNode;

public interface ToSourceConverter {
	public String toSource(ASTNode node);
}
