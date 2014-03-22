package org.codemucker.jmutate.ast;

import org.codemucker.jfind.Roots;
import org.junit.Test;


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
					.setIncludeAll()))
			.build();
		
		engine.find();
		engine.close();
	}
}
