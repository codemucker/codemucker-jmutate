package org.codemucker.jmutate.ast;

import org.codemucker.jfind.RootResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public interface JAstParser {

	/**
	 * Parse the given source as a compilation unit
	 * 
	 * @param src
	 * @param resource optional resource which points to this source. Can be null, in which case there will be no source file associated with the node 
	 * @return
	 */
	CompilationUnit parseCompilationUnit(CharSequence src, RootResource resource);

	ASTNode parseNode(CharSequence src, int kind, RootResource resource);
}