package com.bertvanbrakel.codemucker.ast;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.SearchRoots;

public class JSearchEngineTest {

	@Test
	public void smokeTest(){
		JSearchEngine engine = new JSearchEngine(SearchRoots.builder()
			.setIncludeClassesDir(true)
			.setIncludeTestDir(true)
			.setIncludeClasspath(true)
			.build());
		
		engine.find();
		engine.close();
	}
}
