package org.codemucker.jmutate.ast;

import static org.junit.Assert.assertEquals;

import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.SimpleCodeMuckContext;
import org.codemucker.jmutate.transform.CodeMuckContext;
import org.codemucker.jmutate.transform.SourceTemplate;
import org.junit.Test;

import com.bertvanbrakel.test.util.ClassNameUtil;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaSourceFileTest {

	TestHelper helper = new TestHelper();
	CodeMuckContext ctxt = new SimpleCodeMuckContext();

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
