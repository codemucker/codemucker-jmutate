package com.bertvanbrakel.test.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.bertvanbrakel.test.finder.ClassFinderException;

public class ProjectFinder {

	public static final Collection<String> DEF_PROJECT_FILES = Collections.unmodifiableCollection(Arrays.asList(
			"pom.xml", // maven2
	        "project.xml", // maven1
	        "build.xml", // ant
	        ".project", // eclipse
	        ".classpath" // eclipse	
	));
	
	
	public static File findInProjectDir(String[] relativeDirs){
		return findInProjectDir(DEF_PROJECT_FILES, relativeDirs);
	}
		
	public static File findInProjectDir(Collection<String> projectFiles, String[] relativeDirs){
		File projectDir = findProjectDir(projectFiles);
		for (String option : relativeDirs) {
			File dir = new File(projectDir, option);
			if (dir.exists() && dir.isDirectory()) {
				return dir;
			}
		}
		throw new ClassFinderException("Can't find any of %s directories in %s", Arrays.asList(relativeDirs), projectDir.getAbsolutePath());	
	}

	public static File findProjectDir() {
		return findProjectDir(DEF_PROJECT_FILES);
	}
	
	public static File findProjectDir(final Collection<String> projectFiles) {
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
			throw new ClassFinderException("Can't find project dir. Started looking in %s, looking for any parent directory containing one of %s",
			                new File("./").getCanonicalPath(), projectFiles);
		} catch (IOException e) {
			throw new ClassFinderException("Error while looking for project dir", e);
		}
	}

}
