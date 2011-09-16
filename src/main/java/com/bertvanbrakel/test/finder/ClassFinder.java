package com.bertvanbrakel.test.finder;

import static com.bertvanbrakel.test.bean.ClassUtils.pathToClassName;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
					String className = pathToClassName(resource);
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
