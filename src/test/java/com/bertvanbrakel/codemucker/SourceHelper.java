package com.bertvanbrakel.codemucker;

import static org.junit.Assert.assertEquals;

import com.bertvanbrakel.codemucker.ast.JAstParser;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.finder.Filter;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
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
