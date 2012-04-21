package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.test.util.ClassNameUtil;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaSourceFileTest {

	TestHelper helper = new TestHelper();
	MutationContext ctxt = new SimpleMutationContext();

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
		return template.asSourceFileWithFQN(fqn);
	}

}
