package com.bertvanbrakel.codemucker.ast;

import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;


public class JFindVisitor extends BaseASTVisitor {

	public boolean visit(Root root) {
		return true;
	}

	public void endVisit(Root root) {
	}

	public boolean visit(ClassPathResource resource) {
		return true;
	}

	public void endVisit(ClassPathResource resource) {
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
}