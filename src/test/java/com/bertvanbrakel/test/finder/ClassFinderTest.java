package com.bertvanbrakel.test.finder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.bertvanbrakel.test.finder.ClassFinder;
import com.bertvanbrakel.test.finder.ClassFinderException;
import com.bertvanbrakel.test.finder.ClassFinderOptions.ClassImplementsMatcher;
import com.bertvanbrakel.test.finder.a.TstBeanOne;
import com.bertvanbrakel.test.finder.b.TstBeanTwo;

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
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertNotNull(found);
		assertTrue(found.contains(ClassFinder.class));
		assertTrue(found.contains(ClassFinderException.class));

		assertFalse(found.contains(ClassFinderTest.class));
	}
	
	@Test
	public void test_find_test_classes() {
		ClassFinder finder = new ClassFinder();
		finder.getOptions().includeTestDir(true);
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(ClassFinder.class));
		assertTrue(found.contains(ClassFinderException.class));

		assertTrue(found.contains(ClassFinderTest.class));
	}
	
	@Test
	public void test_filename_exclude(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions().excludeFileName("*Exception*.class");
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(ClassFinder.class));
		assertFalse(found.contains(ClassFinderException.class));		
	}
	
	@Test
	public void test_filename_exclude_pkg(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.excludeFileName("*/b/*");
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(ClassFinder.class));
		assertTrue(found.contains(TstBeanOne.class));		
		assertFalse(found.contains(TstBeanTwo.class));
	}

	@Test
	public void test_filename_exclude_target_has_no_effect(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions().excludeFileName("*/target/*");
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(ClassFinder.class));
	}
	
	@Test
	public void test_filename_include(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.includeFileName("*/a/*");
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertFalse(found.contains(TstBeanTwo.class));
	}
	
	@Test
	public void test_filename_include_multiple(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.includeFileName("*/a/*")
			.includeFileName("*/b/*");
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstBeanTwo.class));
	}
	
	@Test
	public void test_filename_exclude_trumps_include(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.includeFileName("*/a/*")
			.excludeFileName("*/a/*");
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertFalse(found.contains(TstBeanOne.class));
	}
	
	@Test
	public void test_superClass(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.classImplements(TstInterface1.class);
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstInterface1.class));
		assertTrue(found.contains(TstBeanOne.class));
		assertTrue(found.contains(TstBeanOneAndTwo.class));
		
		assertEquals(3, found.size());
	}

	@Test
	public void test_multiple_implements(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.classImplements(TstInterface1.class)
			.classImplements(TstInterface2.class)
			;
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstInterface1.class));
		assertTrue(found.contains(TstInterface2.class));
		assertTrue(found.contains(TstBeanOne.class));
		assertTrue(found.contains(TstBeanTwo.class));
		assertTrue(found.contains(TstBeanOneAndTwo.class));
		
		assertEquals(5, found.size());
	}

	@Test
	public void test_class_must_match_multiple_matchers(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.classImplements(TstInterface1.class, TstInterface2.class)
			;
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstBeanOneAndTwo.class));

		assertEquals(1, found.size());
	}
	
	
	@Test
	public void test_ClassImplementsMatcher(){
		ClassImplementsMatcher matcher = new ClassImplementsMatcher(TstInterface1.class);
		
		assertFalse(matcher.matchClass(Object.class));
		assertFalse(matcher.matchClass(ClassFinder.class));
		assertFalse(matcher.matchClass(TstInterface2.class));
		assertFalse(matcher.matchClass(TstBeanTwo.class));
		
		assertTrue(matcher.matchClass(TstInterface1.class));
		assertTrue(matcher.matchClass(TstBeanOne.class));
	}
	
	private static <T> Collection<T> list(Iterable<T> iter){
		Collection<T> col = new ArrayList<T>();
		for( T item:iter){
			col.add(item);
		}
		return col;
	}
		
		
}
