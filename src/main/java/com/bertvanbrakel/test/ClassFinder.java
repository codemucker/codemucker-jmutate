package com.bertvanbrakel.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Utility class to find classes in ones project
 */
public class ClassFinder {

	final FinderOptions options;
	
	public ClassFinder(){
		options = new FinderOptions();
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
		final Collection<String> PROJECT_FILES = Arrays.asList(
				"pom.xml", // maven2
		        "project.xml", // maven1
		        "build.xml", // ant
		        ".project", // eclipse
		        ".classpath" // eclipse
		);
		FilenameFilter projectDirFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return PROJECT_FILES.contains(name);
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
			                new File("./").getCanonicalPath(), PROJECT_FILES);
			throw new ClassFinderException(msg);
		} catch (IOException e) {
			throw new ClassFinderException("Error while looking for project dir", e);
		}
	}

	public Collection<Class<?>> findClasses() {
		//todo:move this into options
		Collection<File> classPathDirs = new HashSet<File>(options.getClassPathsDir());
		if( options.isIncludeClassesDir()){
			classPathDirs.add(findClassesDir());
		}
		if( options.isIncludeTestDir()){
			classPathDirs.add(findTestClassesDir());
		}
		
		Collection<String> foundClassPaths = new HashSet<String>();
		for( File classPath:classPathDirs){
			foundClassPaths.addAll(findClassFiles(classPath));
		}

		Collection<Class<?>> foundClasses = pathsToClassesAndFilter(foundClassPaths);
		return foundClasses;
	}

	public Collection<String> findClassFilePaths() {
		return findClassFiles(findClassesDir());
	}

	public Collection<String> findClassFiles(File rootDir) {
		FileWalker walker = new FileWalker(){
			@Override
            public boolean isIncludeFile(String relPath, File file) {
				return options.isIncludeFile(relPath, file);
            }
		};
	
		Collection<String> classFiles = walker.findFiles(rootDir);
		return classFiles;
	}

	private Collection<Class<?>> pathsToClassesAndFilter(Collection<String> paths) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		Collection<Class<?>> foundClasses = new ArrayList<Class<?>>();
		for (String path : paths) {
			String classPath = stripExtension(path);
			String className = convertFilePathToClassPath(classPath);
			try {
				Class<?> klass = cl.loadClass(className);
				if (options.isIncludeClass(klass)) {
					foundClasses.add(klass);
				}
			} catch (ClassNotFoundException e) {
				throw new ClassFinderException("couldn't load class " + className, e);
			}
		}
		return foundClasses;
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

	private static class FileWalker {
		FileFilter DIR_FILTER = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() && f.getName().charAt(0) != '.';
			}
		};
		FileFilter CLASS_FILE_FILTER = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().endsWith(".class");
			}
		};

		public final Collection<String> findFiles(File dir) {
			Collection<String> foundFiles = new ArrayList<String>();
			walkDir("", dir, 0, foundFiles);
			return foundFiles;
		}

		private void walkDir(String parentPath, File dir, int depth, Collection<String> foundFiles) {
			File[] files = dir.listFiles(CLASS_FILE_FILTER);
			for (File f : files) {
				String path = parentPath + "/" + f.getName();
				if (isIncludeFile(path, f)) {
					foundFiles.add(path);
				}
			}
			File[] childDirs = dir.listFiles(DIR_FILTER);
			for (File childDir : childDirs) {
				if (isWalkDir(childDir)) {
					walkDir(parentPath + "/" + childDir.getName(), childDir, depth + 1, foundFiles);
				}
			}
		}

		public boolean isWalkDir(File dir) {
			return true;
		}

		public boolean isIncludeFile(String relativePath, File file) {
			return true;
		}
	}

	public FinderOptions getOptions() {
	    return options;
    }
	
	public static class FinderOptions {
		private final Collection<File> classPathsDir = new HashSet<File>();
		private boolean includeClassesDir = true;
		private boolean includeTestDir = false;

		private Collection<FileNameMatcher> excludeFileNameMatchers = new ArrayList<FileNameMatcher>();
		private Collection<FileNameMatcher> includeFileNameMatchers = new ArrayList<FileNameMatcher>();
		
		private Collection<ClassMatcher> includeClassMatchers = new ArrayList<ClassMatcher>();
		
		
		public FinderOptions includeClassesDir(boolean b) {
			this.includeClassesDir = b;
			return this;
		}

		public FinderOptions includeTestDir(boolean b) {
			this.includeTestDir = b;
			return this;
		}

		public Collection<File> getClassPathsDir() {
			return classPathsDir;
		}

		public boolean isIncludeClassesDir() {
			return includeClassesDir;
		}

		public boolean isIncludeTestDir() {
			return includeTestDir;
		}

		public FinderOptions addClassPath(File dir) {
			classPathsDir.add(dir);
			return this;
		}

		boolean isIncludeFile(String relPath, File f){
			boolean include = true;
			if (includeFileNameMatchers!= null && includeFileNameMatchers.size() > 0) {
				include = false;//by default if we have includes we exclude all except matches
				for (FileNameMatcher matcher : includeFileNameMatchers) {
					if (matcher.match(relPath)) {
						include = true;
						break;
					}
				}
			}
			if (include && (excludeFileNameMatchers != null && excludeFileNameMatchers.size() > 0)) {
				for (FileNameMatcher matcher : excludeFileNameMatchers) {
					if (matcher.match(relPath)) {
						include = false;
					}
				}
			}
			return include;
		}

		public FinderOptions excludeFileName(String path) {
			String regExp = antToRegExp(path);
			excludeFileName(Pattern.compile(regExp));
			return this;
		}
		
		public FinderOptions excludeFileName(Pattern pattern) {
			excludeFileName(new RegExpPatternFileNameMatcher(pattern));
			return this;
		}

		public FinderOptions excludeFileName(FileNameMatcher matcher) {
			this.excludeFileNameMatchers.add(matcher);
			return this;
		}

		public FinderOptions includeFileName(String pattern) {
			String regExp = antToRegExp(pattern);
			includeFileName(Pattern.compile(regExp));
			return this;
		}

		public FinderOptions includeFileName(Pattern pattern) {
			includeFileName(new RegExpPatternFileNameMatcher(pattern));
			return this;
		}
		
		public FinderOptions includeFileName(FileNameMatcher matcher) {
			this.includeFileNameMatchers.add(matcher);
			return this;
		}
		private String antToRegExp(String antPattern) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < antPattern.length(); i++) {
				char c = antPattern.charAt(i);
				if (c == '.') {
					sb.append("\\.");
				} else if (c == '*') {
					sb.append(".*");
				} else if (c == '?') {
					sb.append(".?");
				} else {
					sb.append(c);
				}
			}
			return sb.toString();
		}

		public FinderOptions classImplements(Class<?> superclass) {
			addIncludeClassMatcher(new ClassImplementsMatcher(superclass));
			return this;
		}
		
		public FinderOptions addIncludeClassMatcher(ClassMatcher matcher) {
			this.includeClassMatchers.add(matcher);
			return this;
		}
		
		boolean isIncludeClass(Class<?> klass) {
			boolean include = true;
			if (includeClassMatchers != null && includeClassMatchers.size() > 0) {
				include = false;
				for (ClassMatcher matcher : includeClassMatchers) {
					if (matcher.match(klass)) {
						include = true;
						break;
					}
				}
			}
			return include;
		}
	}
	
	private static interface FileNameMatcher {
		public boolean match(String path);
	}
	
	private static class RegExpPatternFileNameMatcher implements FileNameMatcher {
		private final Pattern pattern;
		
		RegExpPatternFileNameMatcher(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean match(String path) {
			return pattern.matcher(path).matches();
		}
	}
	
	private static interface ClassMatcher {
		public boolean match(Class found);
	}
	
	protected static class ClassImplementsMatcher implements ClassMatcher{
		private final Class<?> superclass;

		public ClassImplementsMatcher(Class<?> superclass) {
	        super();
	        this.superclass = superclass;
        }

		@Override
        public boolean match(Class found) {
	        return superclass.isAssignableFrom(found);
        }
	}
}
