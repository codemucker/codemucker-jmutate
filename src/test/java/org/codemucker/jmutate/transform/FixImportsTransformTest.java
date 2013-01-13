package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codemucker.jmutate.ast.CodemuckerException;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.SimpleCodeMuckContext;
import org.codemucker.jmutate.ast.finder.Filter;
import org.codemucker.jmutate.ast.finder.JSourceFinder;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.transform.CodeMuckContext;
import org.codemucker.jmutate.transform.FixImportsTransform;
import org.codemucker.jmutate.transform.SourceTemplate;
import org.codemucker.jmutate.util.SourceAsserts;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import com.bertvanbrakel.test.finder.Roots;

public class FixImportsTransformTest {

	CodeMuckContext ctxt = new SimpleCodeMuckContext();
	
	@Test
	public void test_add_imports(){
		CompilationUnit actual = readTemplate("add_imports.before").asResolvedCompilationUnitNamed(null);
		CompilationUnit expected = readTemplate("add_imports.after").asResolvedCompilationUnitNamed(null);
		
		//do the actual import clean
		ctxt.obtain(FixImportsTransform.class)
			.setNodeToClean(actual)
			.apply();
		
		SourceAsserts.assertAstsMatch(expected, actual);
	}

	@Test
	public void test_existing_imports_only(){
		CompilationUnit actual = readTemplate("existing_imports_only.before").asResolvedCompilationUnitNamed(null);
		CompilationUnit expected = readTemplate("existing_imports_only.after").asResolvedCompilationUnitNamed(null);
		
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
		return JSourceFinder.builder()
			.setSearchRoots(Roots.builder()
				.setIncludeMainSrcDir(true)
				.setIncludeTestSrcDir(true)
			)
			.setFilter(Filter.builder()
				.addIncludeTypes(AJType.withName(type))		
			)
			.build()
			.findTypes()
			.getFirst();
	}
	
}
