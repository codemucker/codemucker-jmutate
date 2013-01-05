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

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.lang.IsBuilder;
import com.bertvanbrakel.test.finder.ArchiveRoot;
import com.bertvanbrakel.test.finder.ClassFinderException;
import com.bertvanbrakel.test.finder.DirectoryRoot;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.finder.Root.RootType;
import com.bertvanbrakel.test.util.ProjectFinder;
import com.bertvanbrakel.test.util.ProjectResolver;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public final class SearchRoots  {
		
	public static Builder builder(){
		return new Builder();
	}
	
	public static class Builder implements IsBuilder<List<Root>> {

		private final Map<String,Root> classPathsRoots = newLinkedHashMap();
		
		private ProjectResolver projectResolver;

		private boolean includeClassesDir = true;
		private boolean includeTestDir = false;
		private boolean includeClasspath = false;
		private boolean includeGeneratedDir = false;
		
		private Set<String> archiveTypes = Sets.newHashSet("jar","zip","ear","war");	
		
		private Builder(){
			//prevent instantiation outside of builder method
		}
		
		/**
		 * Return a mutable list of class path roots. CHanges in the builder are not reflected in the returned
		 * list (or vice versa)
		 */
		public List<Root> build(){
			ProjectResolver resolver = toResolver();
			
			Builder copy = new Builder();
			copy.classPathsRoots.putAll(classPathsRoots);
			if (includeClassesDir) {
				copy.addClassPaths(resolver.getMainSrcDirs(),RootType.MAIN_SRC);
			}
			if (includeTestDir) {
				copy.addClassPaths(resolver.getTestSrcDirs(),RootType.TEST_SRC);
			}
			if (includeGeneratedDir) {
				copy.addClassPaths(resolver.getGeneratedSrcDirs(),RootType.GENERATED_SRC);
			}
			if (includeClasspath) {
				copy.addClassPaths(findClassPathDirs());
			}
			return newArrayList(copy.classPathsRoots.values());
		}
		
		private ProjectResolver toResolver(){
			return projectResolver != null ? projectResolver : ProjectFinder.getDefaultResolver();
		}
		
		public Builder copyOf() {
			Builder copy = new Builder();
			copy.projectResolver = projectResolver;
			copy.includeClassesDir = includeClassesDir;
			copy.includeClasspath = includeClasspath;
			copy.includeGeneratedDir = includeGeneratedDir;
			copy.includeTestDir = includeTestDir;
			copy.classPathsRoots.putAll(classPathsRoots);
			copy.archiveTypes.addAll(archiveTypes);
			
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
		
		public Builder setProjectResolver(ProjectResolver resolver){
			this.projectResolver = resolver;
			return this;
		}
		
		/**
		 * Add additional file extension types to denote an archive resources (like a jar). E.g. 'jar'
		 * 
		 * Default contains jar,zip,war,ear
		 * 
		 * @param extension
		 * @return
		 */
		public Builder addArchiveFileExtension(String extension) {
			this.archiveTypes.add(extension);
	    	return this;
	    }
		
		public Builder addClassPathDir(String path) {
	    	addClassPathDir(new File(path));
	    	return this;
	    }
	
		public Builder addClassPaths(Collection<File> paths) {
			for( File path:paths){
				addClassPathDir(path);
			}
	    	return this;
	    }
		
		public Builder addClassPaths(Collection<File> paths, RootType type) {
			for(File path:paths){
				addClassPath(new DirectoryRoot(path,type));
			}
	    	return this;
	    }
		
		public Builder addClassPathDir(File path) {
			if( path.isFile()){
				String extension = Files.getFileExtension(path.getName()).toLowerCase();
				if( archiveTypes.contains(extension)){
					addClassPath(new ArchiveRoot(path,RootType.UNKNOWN));	
				} else {
					throw new CodemuckerException("Don't currently know how to handle roots of type " + extension); 
				}
			} else {
				addClassPath(new DirectoryRoot(path,RootType.UNKNOWN));
			}
			return this;
	    }
	
		public Builder addClassPaths(Iterable<Root> roots) {
			for(Root root:roots){
				addClassPath(root);
			}
			return this;
		}
		
		public Builder addClassPath(Root root) {
			String key = root.getPathName();
			if (root.isTypeKnown() || !classPathsRoots.containsKey(key)) {
				classPathsRoots.put(key, root);
			}
			return this;
		}
		
		public Builder setIncludeClassesDir(boolean b) {
			this.includeClassesDir = b;
			return this;
		}
	
		public Builder setIncludeTestDir(boolean b) {
			this.includeTestDir = b;
			return this;
		}
	
		public Builder setIncludeGeneratedDir(boolean b) {
			this.includeGeneratedDir = b;
			return this;
		}
		
		public Builder setIncludeClasspath(boolean b) {
	    	this.includeClasspath = b;
	    	return this;
	    }
	}
}