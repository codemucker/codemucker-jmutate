package com.bertvanbrakel.codemucker.ast;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.codemucker.ast.finder.ClasspathResource;
import com.bertvanbrakel.codemucker.util.SourceUtil;

public class JavaSourceFile {
	private final ClasspathResource location;
	private final AstCreator astCreator;
	private transient CompilationUnit astNode;

	public JavaSourceFile(AstCreator astCreator, ClasspathResource location) {
		this.location = location;
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

	public ClasspathResource getLocation(){
		return location;
	}
	
	public void setAstNode(CompilationUnit astNode) {
		this.astNode = astNode;
	}

	public CompilationUnit getCompilationUnit() {
		if (astNode == null) {
			astNode = astCreator.create(location.getFile());
		}
		return astNode;
	}

	public String readSource() {
		return SourceUtil.readSource(location.getFile());
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}