package org.codemucker.jmutate.ast;

import static org.junit.Assert.assertEquals;

import org.codemucker.jmutate.transform.MutateContext;
import org.codemucker.jmutate.transform.SourceTemplate;
import org.codemucker.jtest.ClassNameUtil;
import org.codemucker.jtest.TestHelper;
import org.junit.Test;


public class JavaSourceFileTest {

	TestHelper helper = new TestHelper();
	MutateContext ctxt = new SimpleMutateContext();

	@Test
	public void testGetSimpleClassnameBasedOnPath() {
		JSourceFile src = newSourceFile("foo/bar/Alice");
		assertEquals("Alice", src.getSimpleClassnameBasedOnPath());
	}

	@Test
	public void testGetClassNameBasedOnPath() {
		JSourceFile src = newSourceFile("foo/bar/Alice");
		assertEquals("foo.bar.Alice", src.getClassnameBasedOnPath());
	}

	private JSourceFile newSourceFile(String path) {
		String fqn = path.replace('/', '.');
		String shortName = ClassNameUtil.extractShortClassNamePart(fqn);
		SourceTemplate template = ctxt.newSourceTemplate();
		template.println("public class " + shortName + "  {}");
		return template.asResolvedSourceFileNamed(fqn);
	}
}
