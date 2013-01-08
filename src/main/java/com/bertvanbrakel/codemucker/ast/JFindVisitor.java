package com.bertvanbrakel.codemucker.ast;

import com.bertvanbrakel.test.finder.RootResource;
import com.bertvanbrakel.test.finder.Root;


public class JFindVisitor extends BaseASTVisitor {

	public boolean visit(Root root) {
		return true;
	}

	public void endVisit(Root root) {
	}

	public boolean visit(RootResource resource) {
		return true;
	}

	public void endVisit(RootResource resource) {
	}

	public boolean visit(JSourceFile f) {
		return true;
	}

	public void endVisit(JSourceFile f) {
	}
}