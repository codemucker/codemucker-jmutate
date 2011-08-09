package com.bertvanbrakel.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
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
    
}
