package com.bertvanbrakel.test.generation;

import java.io.File;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public interface AstCreator {

	public CompilationUnit create(File srcFile);

	public CompilationUnit parseCompilationUnit(CharSequence src);

	public ASTNode parseAstSnippet(CharSequence src);

}
