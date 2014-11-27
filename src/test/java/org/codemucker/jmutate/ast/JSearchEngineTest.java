package org.codemucker.jmutate.ast;

import org.codemucker.jfind.Roots;
import org.junit.Test;


public class JSearchEngineTest {

	@Test
	public void smokeTest(){
		JSearchEngine engine = JSearchEngine.with()
			.defaults()
			.scanRoots(Roots.with()
				.mainSrcDir(true)
				.testSrcDir(true))
			.parser(JAstParser.with()
				.resolveBindings(true)
				.checkParse(true)
				.resourceLoader(Roots.with().all()))
			.build();
		
		engine.find();
		engine.close();
	}
}
