package com.bertvanbrakel.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.bertvanbrakel.test.bean.BeanException;

/**
 * Utility class to find classes in ones project
 */
public class ClassFinder {

	public File findClassesDir() {
		File projectDir = findMavenTargetDir();
		String[] options = { "target/classes" };
		for (String option : options) {
			File dir = new File(projectDir, option);
			if (dir.exists() && dir.isDirectory()) {
				return dir;
			}
		}
		throw new BeanException("Can't find classes build dir");
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
			throw new BeanException(msg);
		} catch (IOException e) {
			throw new BeanException("Error while looking for project dir", e);
		}
	}

	public Collection<Class<?>> findClasses() {
		File rootDir = findClassesDir();

		Collection<String> paths = findClassFilePaths(rootDir);
		Collection<Class<?>> foundClasses = pathsToClasses(paths);

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
		DirWalker walker = new DirWalker();
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
			throw new RuntimeException("error converting to relative paths", e);
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
				throw new RuntimeException("couldn't load class " + className, e);
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

	private static class DirWalker {
		FileFilter DIR_FILTER = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
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

}
