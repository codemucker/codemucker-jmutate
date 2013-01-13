package com.bertvanbrakel.codemucker.ast;

import org.junit.Test;

import com.bertvanbrakel.test.finder.Roots;

public class JSearchEngineTest {

	@Test
	public void smokeTest(){
		JSearchEngine engine = JSearchEngine.builder()
			.setDefaults()
			.setRoots(Roots.builder()
				.setIncludeMainSrcDir(true)
				.setIncludeTestSrcDir(true)
				//.setIncludeClasspath(true)
			)
			.setParser(JAstParser.builder()
				.setResolveBindings(true)
				.setCheckParse(true)
				.setResolveRoots(Roots.builder()
					.setIncludeAll()
					.build()
				)
				.build()
			)
			.build();
		
		engine.find();
		engine.close();
	}
}
