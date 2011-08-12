package com.bertvanbrakel.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Utility class to find classes in ones project
 */
public class ClassFinder {

	final ClassFinderOptions options;
	
	public ClassFinder(){
		options = new ClassFinderOptions();
	}

	public File findTestClassesDir() {
		return findInProjectDir(new String[] { "target/test-classes" });
	}

	public File findClassesDir() {
		return findInProjectDir(new String[] { "target/classes" });
	}
	
	private File findInProjectDir(String[] options){
		File projectDir = findProjectDir();
		for (String option : options) {
			File dir = new File(projectDir, option);
			if (dir.exists() && dir.isDirectory()) {
				return dir;
			}
		}
		throw new ClassFinderException("Can't find dir");	
	}

	private File findProjectDir() {
		final Collection<String> projectFiles = options.getProjectFiles();
		FilenameFilter projectDirFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return projectFiles.contains(name);
			}
		};

		try {
			File dir = new File("./");
			while (dir != null) {
				if (dir.listFiles(projectDirFilter).length > 0) {
					return dir.getCanonicalFile();
				}
				dir = dir.getParentFile();
			}
			String msg = String
			        .format("Can't find project dir. Started looking in %s, looking for any parent directory containing one of %s",
			                new File("./").getCanonicalPath(), projectFiles);
			throw new ClassFinderException(msg);
		} catch (IOException e) {
			throw new ClassFinderException("Error while looking for project dir", e);
		}
	}

	public Iterable<Class<?>> findClasses() {
		final Collection<String> foundClassNames = findClassNames();
		return new Iterable<Class<?>>() {
			@Override
			public Iterator<Class<?>> iterator() {
				return new ClassLoadingIterator(foundClassNames.iterator(), options, options.getClassLoader());
			}
		};
	}
	
	public Collection<String> findClassNames() {
		Collection<File> classPathDirs = new HashSet<File>(options.getClassPathsDir());
		if (options.isIncludeClassesDir()) {
			classPathDirs.add(findClassesDir());
		}
		if (options.isIncludeTestDir()) {
			classPathDirs.add(findTestClassesDir());
		}
		final Collection<String> foundClassNames = new HashSet<String>();
		for (File classPath : classPathDirs) {
			for (String resource : findClassResourcePaths(classPath)) {
				if (resource.endsWith(".class")) {
					String className = pathToClassName(resource);
					foundClassNames.add(className);
				}
			}
		}
		return foundClassNames;
	}

	public Collection<String> findClassResourcePaths() {
		return findClassResourcePaths(findClassesDir());
	}

	public Collection<String> findClassResourcePaths(File rootDir) {
		FileWalker walker = new FileWalker(){
			@Override
            public boolean isIncludeFile(String relPath, File file) {
				return options.matchFile(file, relPath);
            }
		};
		Collection<String> resources = walker.findFiles(rootDir);
		return resources;
	}
	
	private String pathToClassName(String relFilePath) {
		String classPath = stripExtension(relFilePath);
		String className = convertFilePathToClassPath(classPath);
		return className;
	}

	private static String stripExtension(String path) {
		int dot = path.lastIndexOf('.');
		if (dot != -1) {
			return path.substring(0, dot);
		}
		return path;
	}

	private static String convertFilePathToClassPath(String path) {
		if (path.charAt(0) == '/') {
			return path.substring(1).replace('/', '.');
		} else {
			return path.replace('/', '.');
		}
	}

	private static class ClassLoadingIterator implements Iterator<Class<?>>{
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
	
	public ClassFinderOptions getOptions() {
	    return options;
    }
}
