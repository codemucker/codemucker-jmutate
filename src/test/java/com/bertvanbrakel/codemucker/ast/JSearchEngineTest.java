package com.bertvanbrakel.codemucker.ast;

import org.junit.Test;

import com.bertvanbrakel.test.finder.Roots;

public class JSearchEngineTest {

	@Test
	public void smokeTest(){
		JSearchEngine engine = JSearchEngine.builder()
			.setDefaults()
			.setRoots(Roots.builder()
				.setIncludeClassesDir(true)
				.setIncludeTestDir(true)
				//.setIncludeClasspath(true)
			)
			.build();
		
		engine.find();
		engine.close();
	}
}
