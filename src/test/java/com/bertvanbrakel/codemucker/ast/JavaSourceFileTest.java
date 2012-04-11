package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.codemucker.util.SrcWriter;
import com.bertvanbrakel.test.util.ClassNameUtil;
import com.bertvanbrakel.test.util.TestHelper;

public class JavaSourceFileTest {

	TestHelper helper = new TestHelper();

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
		String name = path.replace('/', '.');
		String shortName = ClassNameUtil.extractShortClassNamePart(name);
		SrcWriter writer = new SrcWriter();
		writer.append("public class " + shortName + "  {}");
		
		return SourceUtil.writeJavaSrc(writer,name);
	}

}
