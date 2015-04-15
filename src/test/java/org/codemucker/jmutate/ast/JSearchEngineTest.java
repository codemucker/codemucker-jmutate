package org.codemucker.jmutate.ast;

import org.codemucker.jfind.Roots;
import org.junit.Ignore;
import org.junit.Test;


public class JSearchEngineTest {

	//not implemented yet!
	@Ignore
	@Test
	public void smokeTest(){
		JIndexingEngine engine = JIndexingEngine.with()
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
