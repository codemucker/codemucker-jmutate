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
		JSourceFinder finder = JSourceFinder.builder()
			.setSearchRoots(
				Roots.builder()
					.setIncludeClassesDir(false)
					.setIncludeTestDir(true)
			)
			.setFilter(
				Filter.builder()
					.setIncludeFileName("/"+ klass.getName().replace('.', '/') + ".java")
			)
			.setParser(JAstParser.builder()
					.setCheckParse(true)
					.setResolveBindings(true)
					.setRoots(Roots.builder()
						.setIncludeClassesDir(true)
						.setIncludeTestDir(true)
						.setIncludeClasspath(true)
						.build())
					.build())
			.build();
		FindResult<JSourceFile> sources = finder.findSources();
		assertEquals("expected only a single match",1,sources.toList().size());
		return sources.getFirst();
	}
}
