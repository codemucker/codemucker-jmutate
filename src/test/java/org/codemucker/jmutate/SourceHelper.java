package org.codemucker.jmutate;

import static org.junit.Assert.assertEquals;

import org.codemucker.jfind.AResource;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceFinder;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.matcher.AJSourceFile;


public class SourceHelper {

	/**
	 * Find the source file for the given compiled class
	 * @param classToFindSourceFor the class to find the source for
	 * @return the found source file, or throw an exception if no source found
	 */
	public static JSourceFile findSourceForTestClass(Class<?> classToFindSourceFor){
		
		SourceFinder finder = newTestSourcesResolvingFinder()
			.filter(SourceFilter.with()
				.includeResource(AResource.with().path(classToFindSourceFor.getName().replace('.', '/') + ".java")))
			.build();
		FindResult<JSourceFile> sources = finder.findSources();
		assertEquals("expected a single match",1,sources.toList().size());
		return sources.getFirst();
	}
	/**
	 * Look in all source locations including tests
	 * @return
	 */
	public static SourceFinder.Builder newAllSourcesResolvingFinder(){
		return SourceFinder.with()
			.searchRoots(Roots.with()
					.mainSrcDir(true)
					.testSrcDir(true))
			.parser(newResolvingParser());
	}
	
	public static SourceFinder.Builder newTestSourcesResolvingFinder(){
		return SourceFinder.with()
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
