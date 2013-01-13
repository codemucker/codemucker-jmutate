package org.codemucker.jmutate;

import static org.junit.Assert.assertEquals;

import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.finder.Filter;
import org.codemucker.jmutate.ast.finder.FindResult;
import org.codemucker.jmutate.ast.finder.JSourceFinder;

import com.bertvanbrakel.test.finder.Roots;

public class SourceHelper {

	public static JSourceFile findSourceForTestClass(Class<?> klass){
		
		JSourceFinder finder = newTestSourcesResolvingFinder()
			.setFilter(
				Filter.builder()
					.setIncludeFileName("/"+ klass.getName().replace('.', '/') + ".java")
			)
			.build();
		FindResult<JSourceFile> sources = finder.findSources();
		assertEquals("expected only a single match",1,sources.toList().size());
		return sources.getFirst();
	}
	
	public static JSourceFinder.Builder newAllSourcesResolvingFinder(){
		return JSourceFinder.builder()
			.setSearchRoots(
				Roots.builder()
					.setIncludeMainSrcDir(true)
					.setIncludeTestSrcDir(true)
			)
			.setParser(
				newResolvingParser()
			);
	}
	
	public static JSourceFinder.Builder newTestSourcesResolvingFinder(){
		return JSourceFinder.builder()
			.setSearchRoots(Roots.builder()
				.setIncludeMainSrcDir(false)
				.setIncludeTestSrcDir(true)
			)
			.setParser(
				newResolvingParser()
			);
	}
	
	public static JAstParser newResolvingParser(){
		return JAstParser.builder()
		.setCheckParse(true)
		.setResolveBindings(true)
		.setResolveRoots(Roots.builder()
			.setIncludeAll()
			.build())
		.build();
	}
}
