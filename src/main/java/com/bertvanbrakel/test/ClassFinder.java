package com.bertvanbrakel.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Utility class to find classes in ones project
 */
public class ClassFinder {

	final FinderOptions options;
	
	public ClassFinder(){
		options = new FinderOptions();
	}

	public File findTestClassesDir() {
		return findMavenDirOneOf(new String[] { "target/test-classes" });
	}

	public File findClassesDir() {
		return findMavenDirOneOf(new String[] { "target/classes" });
	}
	
	private File findMavenDirOneOf(String[] options){
		File projectDir = findMavenTargetDir();
		for (String option : options) {
			File dir = new File(projectDir, option);
			if (dir.exists() && dir.isDirectory()) {
				return dir;
			}
		}
		throw new ClassFinderException("Can't find dir");	
	}

	private File findMavenTargetDir() {
		final Collection<String> PROJECT_FILES = Arrays.asList(
				"pom.xml", // maven22
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
		Collection<File> classPathDirs = new HashSet<File>(options.getClassPathsDir());
		if( options.isIncludeClassesDir()){
			classPathDirs.add(findClassesDir());
		}
		if( options.isIncludeTestDir()){
			classPathDirs.add(findTestClassesDir());
		}
		
		Collection<String> foundClassPaths = new HashSet<String>();
		for( File classPath:classPathDirs){
			Collection<String> paths = findClassFilePaths(classPath);
			foundClassPaths.addAll(paths);
		}

		Collection<Class<?>> foundClasses = pathsToClasses(foundClassPaths);

		return foundClasses;
	}

	public Collection<String> findClassFilePaths() {
		return findClassFilePaths(findClassesDir());
	}

	public Collection<String> findClassFilePaths(File rootDir) {
		Collection<File> files = findClassFiles(rootDir);
		Collection<String> paths = filesToRelativePaths(rootDir, files);
		return paths;
	}

	public Collection<File> findClassFiles(File rootDir) {
		FileWalker walker = new FileWalker();
		Collection<File> classFiles = walker.findFiles(rootDir);
		return classFiles;
	}

	private Collection<String> filesToRelativePaths(File dir, Collection<File> files) {
		try {
			String rootPath = convertToForwardSlashes(dir.getCanonicalPath());
			int len = rootPath.length();
			Collection<String> paths = new ArrayList<String>();
			for (File f : files) {
				String path = convertToForwardSlashes(f.getCanonicalPath());
				String relPath = path.substring(len);
				if ('/' == relPath.charAt(0)) {
					relPath = relPath.substring(1, relPath.length());
				}
				paths.add(relPath);
			}
			return paths;
		} catch (IOException e) {
			throw new ClassFinderException("error converting to relative paths", e);
		}
	}

	private Collection<Class<?>> pathsToClasses(Collection<String> paths) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		Collection<Class<?>> foundClasses = new ArrayList<Class<?>>();
		for (String path : paths) {
			String classPath = stripExtension(path);
			String className = convertFilePathToClassPath(classPath);
			try {
				Class<?> klass = cl.loadClass(className);
				foundClasses.add(klass);
			} catch (ClassNotFoundException e) {
				throw new ClassFinderException("couldn't load class " + className, e);
				// todo:should never happen...
			}
		}
		return foundClasses;
	}

	private String stripExtension(String path) {
		int dot = path.lastIndexOf('.');
		if (dot != -1) {
			return path.substring(0, dot);
		}
		return path;
	}

	private String convertToForwardSlashes(String path) {
		return path.replace('\\', '/');
	}

	private String convertFilePathToClassPath(String path) {
		return path.replace('/', '.');
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

		public Collection<File> findFiles(File dir) {
			Collection<File> foundFiles = new ArrayList<File>();
			walkDir(dir, 0, foundFiles);
			return foundFiles;
		}

		private void walkDir(File dir, int depth, Collection<File> foundFiles) {
			File[] files = dir.listFiles(CLASS_FILE_FILTER);
			for (File f : files) {
				if (isIncludeFile(f)) {
					foundFiles.add(f);
				}
			}
			File[] childDirs = dir.listFiles(DIR_FILTER);
			for (File childDir : childDirs) {
				if (isWalkDir(childDir)) {
					walkDir(childDir, depth + 1, foundFiles);
				}
			}
		}

		public boolean isWalkDir(File dir) {
			return true;
		}

		public boolean isIncludeFile(File file) {
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
	}

}
