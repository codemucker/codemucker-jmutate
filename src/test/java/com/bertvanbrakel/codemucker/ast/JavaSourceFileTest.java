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
		JavaSourceFile src = newSourceFile("foo/bar/Alice.java");
		assertEquals("Alice", src.getSimpleClassnameBasedOnPath());
	}

	@Test
	public void testGetClassNameBasedOnPath() {
		JavaSourceFile src = newSourceFile("foo/bar/Alice.java");
		assertEquals("foo.bar.Alice", src.getClassnameBasedOnPath());
	}

	private JavaSourceFile newSourceFile(String path) {
		File dir = helper.createTempDir();

		JavaSourceFile src = new JavaSourceFile(new ClasspathResource(dir, path), new DefaultAstCreator());
		return src;
	}

}
