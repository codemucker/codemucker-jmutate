package org.codemucker.jmutate.ast;

import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;


public class BaseSourceVisitor extends BaseASTVisitor {

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