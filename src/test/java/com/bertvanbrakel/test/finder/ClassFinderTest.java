/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.test.finder;

import static com.bertvanbrakel.test.util.TestUtils.list;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import com.bertvanbrakel.test.finder.ClassFinderOptions.ClassAssignableMatcher;
import com.bertvanbrakel.test.finder.a.TstBeanOne;
import com.bertvanbrakel.test.finder.b.TstBeanTwo;
import com.bertvanbrakel.test.finder.c.TstAnonymous;
import com.bertvanbrakel.test.finder.d.TstInner;
import com.bertvanbrakel.test.finder.e.TstAnnotation;
import com.bertvanbrakel.test.finder.e.TstAnnotationBean;

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
	public void test_filename_include_multiple_packages(){
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
	public void test_include_instance_of(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.assignableTo(TstInterface1.class);
		
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
			.assignableTo(TstInterface1.class)
			.assignableTo(TstInterface2.class)
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
			.assignableTo(TstInterface1.class, TstInterface2.class)
			;
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstBeanOneAndTwo.class));

		assertEquals(1, found.size());
	}
	
	
	@Test
	public void test_ClassImplementsMatcher(){
		ClassAssignableMatcher matcher = new ClassAssignableMatcher(TstInterface1.class);
		
		assertFalse(matcher.matchClass(Object.class));
		assertFalse(matcher.matchClass(ClassFinder.class));
		assertFalse(matcher.matchClass(TstInterface2.class));
		assertFalse(matcher.matchClass(TstBeanTwo.class));
		
		assertTrue(matcher.matchClass(TstInterface1.class));
		assertTrue(matcher.matchClass(TstBeanOne.class));
	}

	@Test
	public void test_find_enums(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			;
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstEnum.class));
		assertTrue(found.contains(TstBeanOneAndTwo.InstanceEnum.class));
		assertTrue(found.contains(TstBeanOneAndTwo.StaticEnum.class));
	}
	
	@Test
	public void test_filter_enum(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.excludeEnum()
			;
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertFalse(found.contains(TstEnum.class));
		assertFalse(found.contains(TstBeanOneAndTwo.InstanceEnum.class));
		assertFalse(found.contains(TstBeanOneAndTwo.StaticEnum.class));

		assertTrue(found.size() > 1);
	}
	
	@Test
	public void test_filter_anonymous(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.includeFileName("*/c/*")
			.excludeAnonymous()
			;
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstAnonymous.class),list(found));
	}
	
	@Test
	public void test_filter_inner_class(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.includeFileName("*/d/*")
			.excludeInner()
			;
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstInner.class),list(found));
	}	
	
	@Test
	public void test_filter_interfaces(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.excludeInterfaces()
			;
		
		Collection<Class<?>> found = list(finder.findClasses());

		
		assertFalse(found.contains(TstInterface.class));
		assertFalse(found.contains(TstInterface1.class));
		assertFalse(found.contains(TstInterface2.class));

		assertTrue(found.contains(TstBeanOneAndTwo.class));
	}
	
	@Test
	public void test_find_has_annotations(){
		ClassFinder finder = new ClassFinder();
		finder.getOptions()
			.includeTestDir(true)
			.withAnnotation(TstAnnotation.class)
		;

		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstAnnotationBean.class), found);
	}
}
