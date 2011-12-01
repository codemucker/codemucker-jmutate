package com.bertvanbrakel.codemucker.ast.finder.matcher;

public interface Matcher<T> {
	boolean matches(T found);
}
