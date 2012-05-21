package com.bertvanbrakel.codemucker.pattern;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class ImportCleanerPatternTest {

	MutationContext ctxt = new SimpleMutationContext();
	
	@Test
	public void test(){
		
		JType actual = findTestType(BeanBefore.class);
		JType expected = findTestType(BeanAfter.class);
		
		//do the actual import clean
		ctxt.obtain(ImportCleanerPattern.class)
			.setTarget(actual)
			.apply();
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}

	private JType findTestType(Class<?> type) {
		return JSourceFinder.newBuilder()
			.setSearchPaths(SearchPathsBuilder.newBuilder()
				.setIncludeClassesDir(true)
				.setIncludeTestDir(true)
			)
			.setFilter(FilterBuilder.newBuilder()
				.addIncludeTypes(JTypeMatchers.withFQN(type))		
			)
			.build()
			.findTypes()
			.getFirst();
	}
	
}
