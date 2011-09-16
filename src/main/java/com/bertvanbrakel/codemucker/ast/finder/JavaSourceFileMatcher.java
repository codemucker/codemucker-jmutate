package com.bertvanbrakel.codemucker.ast.finder;

import com.bertvanbrakel.codemucker.ast.JavaSourceFile;

public interface JavaSourceFileMatcher {
	public boolean matchSource(JavaSourceFile found);
}