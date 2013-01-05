package com.bertvanbrakel.codemucker.ast.finder;

import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;

public interface JSearchScopeVisitor {
	public boolean visit(Root root);
	public void endVisit(Root root);
	public boolean visit(ClassPathResource resource);
	public void endVisit(ClassPathResource resource);
}