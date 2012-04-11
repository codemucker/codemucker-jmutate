package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bertvanbrakel.test.finder.ClassFinderException;
import com.bertvanbrakel.test.finder.ClassPathRoot;
import com.bertvanbrakel.test.finder.ClassPathRoot.TYPE;
import com.bertvanbrakel.test.util.ProjectFinder;
import com.bertvanbrakel.test.util.ProjectResolver;

public class SearchPathsBuilder {
	
	private final Map<String,ClassPathRoot> classPathsRoots = newLinkedHashMap();
	
	private ProjectResolver projectResolver;

	private boolean includeClassesDir = true;
	private boolean includeTestDir = false;
	private boolean includeClasspath = false;
	private boolean includeGeneratedDir = false;
	
	public static SearchPathsBuilder newBuilder(){
		return new SearchPathsBuilder();
	}
	
	private SearchPathsBuilder(){
		//prevent instantiation outside of builder method
	}
	
	/**
	 * Return a mutable list of class path roots
	 */
	public List<ClassPathRoot> build(){
		ProjectResolver resolver = toResolver();
		
		SearchPathsBuilder copy = new SearchPathsBuilder();
		copy.classPathsRoots.putAll(classPathsRoots);
		if (includeClassesDir) {
			copy.addClassPaths(resolver.getMainSrcDirs(),TYPE.MAIN_SRC);
		}
		if (includeTestDir) {
			copy.addClassPaths(resolver.getTestSrcDirs(),TYPE.TEST_SRC);
		}
		if (includeGeneratedDir) {
			copy.addClassPaths(resolver.getGeneratedSrcDirs(),TYPE.GENERATED_SRC);
		}
		if (includeClasspath) {
			copy.addClassPaths(findClassPathDirs());
		}
		return newArrayList(copy.classPathsRoots.values());
	}
	
	private ProjectResolver toResolver(){
		return projectResolver!=null?projectResolver:ProjectFinder.getDefaultResolver();
	}
	
	public SearchPathsBuilder copyOf(){
		SearchPathsBuilder copy = new SearchPathsBuilder();
		copy.projectResolver = projectResolver;
		copy.includeClassesDir = includeClassesDir;
		copy.includeClasspath = includeClasspath;
		copy.includeGeneratedDir = includeGeneratedDir;
		copy.includeTestDir = includeTestDir;
		copy.classPathsRoots.putAll(classPathsRoots);
		return copy;
	}
	
	private Set<File> findClassPathDirs() {
		Set<File> files = newLinkedHashSet();

		String classpath = System.getProperty("java.class.path");
		String sep = System.getProperty("path.separator");
		String[] paths = classpath.split(sep);

		Collection<String> fullPathNames = newArrayList();
		for (String path : paths) {
			try {
				File f = new File(path);
				if (f.exists() & f.canRead()) {
					String fullPath = f.getCanonicalPath();
					if (!fullPathNames.contains(fullPath)) {
						files.add(f);
						fullPathNames.add(fullPath);
					}
				}
			} catch (IOException e) {
				throw new ClassFinderException("Error trying to resolve pathname " + path);
			}
		}
		return files;
	}	
	
	public SearchPathsBuilder setProjectResolver(ProjectResolver resolver){
		this.projectResolver = resolver;
		return this;
	}
	
	public SearchPathsBuilder addClassPathDir(String path) {
    	addClassPathDir(new File(path));
    	return this;
    }

	public SearchPathsBuilder addClassPaths(Collection<File> paths) {
		for( File path:paths){
			addClassPathDir(path);
		}
    	return this;
    }
	
	public SearchPathsBuilder addClassPaths(Collection<File> paths, TYPE type) {
		for( File path:paths){
			addClassPath(new ClassPathRoot(path,type));
		}
    	return this;
    }
	
	public SearchPathsBuilder addClassPathDir(File path) {
		addClassPath(new ClassPathRoot(path,TYPE.UNKNOWN));
    	return this;
    }

	public SearchPathsBuilder addClassPaths(Iterable<ClassPathRoot> roots) {
		for(ClassPathRoot root:roots){
			addClassPath(root);
		}
		return this;
	}
	
	public SearchPathsBuilder addClassPath(ClassPathRoot root) {
		String key = root.getPathName();
		if (root.isTypeKnown() || !classPathsRoots.containsKey(key)) {
			classPathsRoots.put(key, root);
		}
		return this;
	}
	
	public SearchPathsBuilder setIncludeClassesDir(boolean b) {
		this.includeClassesDir = b;
		return this;
	}

	public SearchPathsBuilder setIncludeTestDir(boolean b) {
		this.includeTestDir = b;
		return this;
	}

	public SearchPathsBuilder setIncludeGeneratedDir(boolean b) {
		this.includeGeneratedDir = b;
		return this;
	}
	
	public SearchPathsBuilder setIncludeClasspath(boolean b) {
    	this.includeClasspath = b;
    	return this;
    }
}