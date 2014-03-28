package org.codemucker.jmutate.ast;

import org.codemucker.jfind.Roots;
import org.junit.Test;


public class JSearchEngineTest {

	@Test
	public void smokeTest(){
		JSearchEngine engine = JSearchEngine.builder()
			.defaults()
			.searchRoots(Roots.with()
				.mainSrcDir(true)
				.testSrcDir(true)
				//.setIncludeClasspath(true)
				)
			.parser(JAstParser.with()
				.resolveBindings(true)
				.checkParse(true)
				.roots(Roots.with()
					.allDirs()))
			.build();
		
		engine.find();
		engine.close();
	}
}
