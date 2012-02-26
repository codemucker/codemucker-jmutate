package com.bertvanbrakel.codemucker.ast;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;


public class JSourceFileVisitor extends BaseASTVisitor {

	public boolean visit(File rootDir, String relFilePath, File srcFile) {
		return true;
	}

	public void endVisit(File rootDir, String relFilePath, File srcFile) {
	}

	public boolean visitClass(String className) {
		return true;
	}

	public void endVisitClass(String className) {
	}

	public boolean visit(JSourceFile f) {
		return true;
	}

	public void endVisit(JSourceFile f) {
	}

	public boolean visit(CompilationUnit cu) {
		return true;
	}

	public void endVisit(CompilationUnit cu) {
	}
}