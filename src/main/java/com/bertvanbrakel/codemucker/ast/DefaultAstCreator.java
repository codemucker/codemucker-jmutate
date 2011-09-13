package com.bertvanbrakel.codemucker.ast;

import java.io.File;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.codemucker.util.SourceUtil;

public final class DefaultAstCreator implements AstCreator {
    private ASTParser parser = SourceUtil.newParser();

    @Override
    public synchronized CompilationUnit create(File srcFile) {
    	String src = SourceUtil.readSource(srcFile);
    	CompilationUnit cu = parseCompilationUnit(src);
    	cu.recordModifications();
    	return cu;
    }

    @Override
    public ASTNode parseAstSnippet(CharSequence src) {
    	return createNode(src,ASTParser.K_EXPRESSION);
    }

    @Override
    public CompilationUnit parseCompilationUnit(CharSequence src) {
    	CompilationUnit cu = (CompilationUnit) createNode(src,ASTParser.K_COMPILATION_UNIT);
    	return cu;
    }

    private ASTNode createNode(CharSequence src, int kind){
    	parser.setSource(src.toString().toCharArray());
    	parser.setKind(kind);
    	ASTNode node = parser.createAST(null);
    	return node;
    }
}