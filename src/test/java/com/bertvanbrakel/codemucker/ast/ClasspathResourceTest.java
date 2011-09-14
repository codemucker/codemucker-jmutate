package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.bertvanbrakel.codemucker.ast.finder.ClasspathResource;
import com.bertvanbrakel.test.util.TestHelper;

public class ClasspathResourceTest {

	TestHelper helper = new TestHelper();
	
	@Test
	public void testPackagePart(){
		File dir = tmpDir();
		ClasspathResource resource = new ClasspathResource(dir, "foo/bar/Alice.java");
		
		assertEquals("foo.bar", resource.getPackagePart());
	}
	@Test
	public void testFileName(){
		File dir = tmpDir();
		ClasspathResource resource = new ClasspathResource(dir, "foo/bar/Alice.java");
		
		assertEquals("Alice", resource.getFilenamePart());
	}
	
	@Test
	public void testExtension(){
		File dir = tmpDir();
		ClasspathResource resource = new ClasspathResource(dir, "foo/bar/Alice.java");
		
		assertEquals("java", resource.getExtension());
	}
	
	private File tmpDir(){
		return helper.createTempDir();
	}
	
}
