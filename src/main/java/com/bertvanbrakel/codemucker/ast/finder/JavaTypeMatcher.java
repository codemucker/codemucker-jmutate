package com.bertvanbrakel.codemucker.ast.finder;

import com.bertvanbrakel.codemucker.ast.JavaType;

public interface JavaTypeMatcher {
	public boolean matchType(JavaType found);
}