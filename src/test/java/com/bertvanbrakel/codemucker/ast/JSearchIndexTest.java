package com.bertvanbrakel.codemucker.ast;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.SearchRoots;

public class JSearchIndexTest {

	@Test
	public void testSearch(){
		JSearchIndex index = new JSearchIndex(SearchRoots.newBuilder()
			.setIncludeClassesDir(true)
			.setIncludeTestDir(true)
			.build());
		
		index.find();
		
		
	}
}
