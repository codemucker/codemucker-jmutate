package com.bertvanbrakel.test.generation;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.test.bean.builder.BaseASTVisitor;
import com.bertvanbrakel.test.finder.JavaSourceFile;

public class SourceFileVisitor extends BaseASTVisitor {

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