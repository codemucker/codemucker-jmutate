package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codemucker.jmutate.ast.MutateException;
import org.codemucker.jmutate.ast.SimpleMutateContext;
import org.codemucker.jmutate.util.SourceAsserts;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;


public class FixImportsTransformTest {

	MutateContext ctxt = new SimpleMutateContext();
	
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
			throw new MutateException("error reading template path:" + path,e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
