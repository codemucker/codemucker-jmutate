package org.codemucker.jmutate;

import static org.junit.Assert.assertEquals;

import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.finder.Filter;
import org.codemucker.jmutate.ast.finder.FindResult;
import org.codemucker.jmutate.ast.finder.JSourceFinder;


public class SourceHelper {

	public static JSourceFile findSourceForTestClass(Class<?> classToFindSourceFor){
		
		JSourceFinder finder = newTestSourcesResolvingFinder()
			.setFilter(Filter.builder()
				.setIncludeFileName(classToFindSourceFor.getName().replace('.', '/') + ".java"))
			.build();
		FindResult<JSourceFile> sources = finder.findSources();
		assertEquals("expected a single match",1,sources.toList().size());
		return sources.getFirst();
	}
	/**
	 * Look in all source locations including tests
	 * @return
	 */
	public static JSourceFinder.Builder newAllSourcesResolvingFinder(){
		return JSourceFinder.builder()
			.setSearchRoots(Roots.builder()
					.setIncludeMainSrcDir(true)
					.setIncludeTestSrcDir(true))
			.setParser(newResolvingParser());
	}
	
	public static JSourceFinder.Builder newTestSourcesResolvingFinder(){
		return JSourceFinder.builder()
			.setSearchRoots(Roots.builder()
				.setIncludeMainSrcDir(false)
				.setIncludeTestSrcDir(true))
			.setParser(
				newResolvingParser());
	}
	
	public static JAstParser newResolvingParser(){
		return JAstParser.builder()
			.setCheckParse(true)
			.setResolveBindings(true)
			.setResolveRoots(Roots.builder().setIncludeAll())
			.build();
	}
}
