package org.codemucker.jmutate.ast;

import org.codemucker.jfind.ClassRoots;
import org.junit.Test;


public class JSearchEngineTest {

	@Test
	public void smokeTest(){
		JSearchEngine engine = JSearchEngine.builder()
			.setDefaults()
			.setRoots(ClassRoots.builder()
				.setIncludeMainSrcDir(true)
				.setIncludeTestSrcDir(true)
				//.setIncludeClasspath(true)
				)
			.setParser(JAstParser.builder()
				.setResolveBindings(true)
				.setCheckParse(true)
				.setResolveRoots(ClassRoots.builder()
					.setIncludeAll()))
			.build();
		
		engine.find();
		engine.close();
	}
}
