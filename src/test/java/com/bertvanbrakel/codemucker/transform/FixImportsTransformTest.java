package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.codemucker.ast.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.transform.FixImportsTransform;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class FixImportsTransformTest {

	MutationContext ctxt = new SimpleMutationContext();
	
	@Test
	public void test_add_imports(){
		CompilationUnit actual = readTemplate("add_imports.before").asCompilationUnit();
		CompilationUnit expected = readTemplate("add_imports.after").asCompilationUnit();
		
		//do the actual import clean
		ctxt.obtain(FixImportsTransform.class)
			.setNodeToClean(actual)
			.apply();
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}

	@Test
	public void test_existing_imports_only(){
		CompilationUnit actual = readTemplate("existing_imports_only.before").asCompilationUnit();
		CompilationUnit expected = readTemplate("existing_imports_only.after").asCompilationUnit();
		
		//do the actual import clean
		ctxt.obtain(FixImportsTransform.class)
			.setNodeToClean(actual)
			.setAddMissingImports(false)
			.apply();
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}
	
	private SourceTemplate readTemplate(String template){
		return ctxt.newSourceTemplate().p(readTemplateAsString(template));
	}
	
	private String readTemplateAsString(String template){
		String path = getClass().getSimpleName() + ".java." + template;
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream(path);
			checkNotNull(is,"couldn't find template:" + path);
			return IOUtils.toString(is,"UTF-8");
		} catch (IOException e){
			throw new CodemuckerException("error reading template path:" + path,e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private JType findTestType(Class<?> type) {
		return JSourceFinder.newBuilder()
			.setSearchPaths(SearchPathsBuilder.newBuilder()
				.setIncludeClassesDir(true)
				.setIncludeTestDir(true)
			)
			.setFilter(FilterBuilder.newBuilder()
				.addIncludeTypes(JTypeMatchers.withFullName(type))		
			)
			.build()
			.findTypes()
			.getFirst();
	}
	
}
