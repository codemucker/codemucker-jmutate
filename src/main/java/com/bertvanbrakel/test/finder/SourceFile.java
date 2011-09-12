package com.bertvanbrakel.test.finder;

import java.io.File;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.test.generation.AstCreator;
import com.bertvanbrakel.test.generation.SourceFileVisitor;
import com.bertvanbrakel.test.util.SourceUtil;

public class SourceFile {

	private final File path;
	private final String pathBasedClassName;

	private transient CompilationUnit astNode;

	private final AstCreator astCreator;

	public SourceFile(AstCreator astCreator, File path, String className) {
		this.path = path;
		this.pathBasedClassName = className;
		this.astCreator = astCreator;
	}

	public void visit(SourceFileVisitor visitor) {
		if (visitor.visit(this)) {
			CompilationUnit cu = getCompilationUnit();
			if (visitor.visit(cu)) {
				cu.accept(visitor);
				visitor.endVisit(cu);
			}
			visitor.endVisit(this);
		}
	}

	public AstCreator getAstCreator() {
		return astCreator;
	}

	public String getPathBasedClassName() {
		return pathBasedClassName;
	}

	public File getPath() {
		return path;
	}

	public void setAstNode(CompilationUnit astNode) {
		this.astNode = astNode;
	}

	public CompilationUnit getCompilationUnit() {
		if (astNode == null) {
			astNode = astCreator.create(path);
		}
		return astNode;
	}

	public String readSource() {
		return SourceUtil.readSource(path);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}