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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.bertvanbrakel.test.util.ClassNameUtil;
import com.bertvanbrakel.test.util.ProjectFinder;

/**
 * Utility class to find classes in ones project
 */
public class ClassFinder {

	final ClassFinderOptions options;
	
	public ClassFinder(){
		this(new ClassFinderOptions());
	}

	public ClassFinder(ClassFinderOptions options){
		this.options = options;
	}	
	
	public ClassFinderOptions getOptions() {
	    return options;
    }
	
	public File findClassesDir() {
		return ProjectFinder.findDefaultMavenCompileDir();
	}
	
	public File findTestClassesDir() {
		return ProjectFinder.findDefaultMavenCompileTestDir();
	}
	
	public Iterable<Class<?>> findClasses() {
		final Collection<String> foundClassNames = findClassNames();
		return new Iterable<Class<?>>() {
			@Override
			public Iterator<Class<?>> iterator() {
				return new ClassLoadingIterator(foundClassNames.iterator(), options.toClassMatcher(), options.getClassLoader());
			}
		};
	}
	
	public Collection<String> findClassNames() {
		final Collection<String> foundClassNames = new HashSet<String>();
		for (File classPath : getClassPathDirsToSearch()) {
			for (String resource : findClassResourcePaths(classPath)) {
				if (resource.endsWith(".class")) {
					String className = ClassNameUtil.pathToClassName(resource);
					foundClassNames.add(className);
				}
			}
		}
		return foundClassNames;
	}
	
	private Collection<File> getClassPathDirsToSearch(){
		Collection<File> classPathDirs = new HashSet<File>(options.getClassPathsDir());
		if (options.isIncludeClassesDir()) {
			classPathDirs.add(findClassesDir());
		}
		if (options.isIncludeTestDir()) {
			classPathDirs.add(findTestClassesDir());
		}
		return classPathDirs;
	}

	public Collection<String> findClassResourcePaths() {
		return findClassResourcePaths(findClassesDir());
	}

	public Collection<String> findClassResourcePaths(File rootDir) {
		FileWalkerFilter walker = new FileWalkerFilter();
		walker.setIncludes(options.toFileMatcher());
		
		Collection<String> resources = walker.findFiles(rootDir);
		return resources;
	}

	private static final class ClassLoadingIterator implements Iterator<Class<?>>{
		private final ClassMatcher classMatcher;
		private final Iterator<String> classNames;
		private final ClassLoader classLoader;
		private Class<?> nextClass;

		public ClassLoadingIterator(Iterator<String> classNames, ClassMatcher classMatcher, ClassLoader classLoader) {
			super();
			this.classNames = classNames;
			this.classMatcher = classMatcher;
			this.classLoader = classLoader;
			nextClass = nextClass();
		}

		@Override
		public boolean hasNext() {
			return nextClass != null;
		}

		@Override
		public Class<?> next() {
			if (nextClass == null) {
				throw new NoSuchElementException();
			}
			Class<?> ret = nextClass;
			nextClass = nextClass();
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		private Class<?> nextClass() {
			while (classNames.hasNext()) {
				String className = classNames.next();
				Class<?> klass = loadClass(className);
				if (classMatcher.matchClass(klass)) {
					return klass;
				}
			}
			return null;
		}
		
		private Class<?> loadClass(String className) {
			try {
				return classLoader.loadClass(className);
			} catch (ClassNotFoundException e) {
				throw new ClassFinderException("couldn't load class " + className, e);
			}
		}
	}
}
