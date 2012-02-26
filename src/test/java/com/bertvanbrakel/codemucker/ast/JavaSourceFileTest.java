package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.ClasspathResource;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaSourceFileTest {

	TestHelper helper = new TestHelper();

	@Test
	public void testGetSimpleClassnameBasedOnPath() {
		JSourceFile src = newSourceFile("foo/bar/Alice.java");
		assertEquals("Alice", src.getSimpleClassnameBasedOnPath());
	}

	@Test
	public void testGetClassNameBasedOnPath() {
		JSourceFile src = newSourceFile("foo/bar/Alice.java");
		assertEquals("foo.bar.Alice", src.getClassnameBasedOnPath());
	}

	private JSourceFile newSourceFile(String path) {
		File dir = helper.createTempDir();

		JSourceFile src = new JSourceFile(new ClasspathResource(dir, path), new DefaultAstCreator());
		return src;
	}

}
