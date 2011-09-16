package com.bertvanbrakel.codemucker.ast;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;


public class JavaSourceFileVisitor extends BaseASTVisitor {

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

	public boolean visit(JavaSourceFile f) {
		return true;
	}

	public void endVisit(JavaSourceFile f) {
	}

	public boolean visit(CompilationUnit cu) {
		return true;
	}

	public void endVisit(CompilationUnit cu) {
	}
}