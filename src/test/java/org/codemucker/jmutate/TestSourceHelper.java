package org.codemucker.jmutate;

import static org.junit.Assert.assertEquals;

import org.codemucker.jfind.AResource;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;

public class TestSourceHelper {

	/**
	 * Find the source file for the given compiled class
	 * @param classToFindSourceFor the class to find the source for
	 * @return the found source file, or throw an exception if no source found
	 */
	public static JSourceFile findSourceForClass(Class<?> classToFindSourceFor){
		String filePath = classToFindSourceFor.getName().replace('.', '/') + ".java";
		JSourceFinder finder = newAllSourcesResolvingFinder()
			.filter(JSourceFilter.with()
				.includeResource(AResource.with().path(filePath)))
			.build();
		FindResult<JSourceFile> sources = finder.findSources();
		
		//Expect.that(sources).is(AList.withOnly(AJSourceFile.any()));
		
		assertEquals("expected a single match",1,sources.toList().size());
		return sources.getFirst();
	}
	/**
	 * Look in all source locations including tests
	 * @return
	 */
	public static JSourceFinder.Builder newAllSourcesResolvingFinder(){
		return JSourceFinder.with()
			.searchRoots(Roots.with()
					.mainSrcDir(true)
					.testSrcDir(true))
			.parser(newResolvingParser());
	}
	
	public static JSourceFinder.Builder newTestSourcesResolvingFinder(){
		return JSourceFinder.with()
			.searchRoots(Roots.with()
				.mainSrcDir(false)
				.testSrcDir(true))
			.parser(
				newResolvingParser());
	}
	
	public static JAstParser newResolvingParser(){
		return JAstParser.with()
			.checkParse(true)
			.resolveBindings(true)
			.roots(Roots.with().allDirs())
			.build();
	}
}
