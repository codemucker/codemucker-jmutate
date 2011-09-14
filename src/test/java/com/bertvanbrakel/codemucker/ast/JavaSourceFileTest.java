package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.ClasspathResource;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaSourceFileTest {

	@Test
	public void testClassNameBasedOnPath() {
		TestHelper helper = new TestHelper();

		File dir = helper.createTempDir();

		JavaSourceFile src = new JavaSourceFile(new DefaultAstCreator(), new ClasspathResource(dir, "foo/bar/Alice.java"));
		assertEquals("foo.bar.Alice", src.getClassnameBasedOnPath());

	}
}
