package com.bertvanbrakel.test;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import com.bertvanbrakel.test.bean.BeanException;
import com.bertvanbrakel.test.bean.RandomDataProvider;

import static junit.framework.Assert.*;

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
		assertTrue(found.contains(BeanException.class));
		assertTrue(found.contains(RandomDataProvider.class));
	}
}
