package com.bertvanbrakel.test;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import com.bertvanbrakel.test.ClassFinder.ClassImplementsMatcher;
import com.bertvanbrakel.test.a.TstBeanOne;
import com.bertvanbrakel.test.b.TstBeanTwo;

public class ClassFinderTest {

	@Test
	public void test_find_class_dir() {
		ClassFinder finder = new ClassFinder();
		File dir = finder.findClassesDir();
		assertNotNull(dir);
		String path = convertToForwardSlashes(dir.getPath());
		assertTrue(path.endsWith("/target/classes"));
	}

	private String convertToForwardSlashes(String path) {
		return path.replace('\\', '/');
	}

	@Test
	public void test_find_classes() {
		ClassFinder finder = new ClassFinder();
		
		Collection<Class<?>> found = finder.findClasses();

		assertNotNull(found);
		assertTrue(found.contains(ClassFinder.class));
		assertTrue(found.contains(ClassFinderException.class));

		assertFalse(found.contains(ClassFinderTest.class));
	}
	
	@Test
	public void test_find_test_classes() {
		ClassFinder finder = new ClassFinder();
		finder.getOptions().includeTestDir(true);
		
		Collection<Class<?>> found = finder.findClasses();

		assertTrue(found.contains(ClassFinder.class));
		assertTrue(found.contains(ClassFinderException.class));

		assertTrue(found.contains(ClassFinderTest.class));
	}
	
	@Test
	public void test_filename_exclude(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions().excludeFileName("*Exception*.class");
		
		Collection<Class<?>> found = finder.findClasses();

		assertTrue(found.contains(ClassFinder.class));
		assertFalse(found.contains(ClassFinderException.class));		
	}
	
	@Test
	public void test_filename_exclude_pkg(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.excludeFileName("*/b/*");
		
		Collection<Class<?>> found = finder.findClasses();

		assertTrue(found.contains(ClassFinder.class));
		assertTrue(found.contains(TstBeanOne.class));		
		assertFalse(found.contains(TstBeanTwo.class));
	}

	@Test
	public void test_filename_exclude_target_has_no_effect(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions().excludeFileName("*/target/*");
		
		Collection<Class<?>> found = finder.findClasses();

		assertTrue(found.contains(ClassFinder.class));
	}
	
	@Test
	public void test_filename_include(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.includeFileName("*/a/*");
		
		Collection<Class<?>> found = finder.findClasses();

		assertFalse(found.contains(TstBeanTwo.class));
	}
	
	@Test
	public void test_filename_include_multiple(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.includeFileName("*/a/*")
			.includeFileName("*/b/*");
		
		Collection<Class<?>> found = finder.findClasses();

		assertTrue(found.contains(TstBeanTwo.class));
	}
	
	@Test
	public void test_filename_exclude_trumps_include(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.includeFileName("*/a/*")
			.excludeFileName("*/a/*");
		
		Collection<Class<?>> found = finder.findClasses();

		assertFalse(found.contains(TstBeanOne.class));
	}
	
	@Test
	public void test_superClass(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.classImplements(TstInterface.class);
		
		Collection<Class<?>> found = finder.findClasses();

		assertTrue(found.contains(TstInterface.class));
		assertTrue(found.contains(TstBeanOne.class));
		assertEquals(2, found.size());
	}
	
		
	@Test
	public void test_ClassImplementsMatcher(){
		ClassImplementsMatcher matcher = new ClassImplementsMatcher(TstInterface.class);
		
		assertFalse(matcher.match(Object.class));
		assertFalse(matcher.match(ClassFinder.class));
		assertFalse(matcher.match(TstBeanTwo.class));
		
		assertTrue(matcher.match(TstInterface.class));
		assertTrue(matcher.match(TstBeanOne.class));
	}
		
		
}
