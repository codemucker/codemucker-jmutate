package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.bertvanbrakel.codemucker.ast.AstCreator;
import com.bertvanbrakel.codemucker.ast.DefaultAstCreator;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JSourceFileVisitor;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.ClassPathRoot;
import com.bertvanbrakel.test.finder.LoggingMatchedCallback;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.google.common.collect.ImmutableList;

/**
 * Utility class to find source files, java types, and methods
 */
public class JSourceFinder {

	private static final String JAVA_EXTENSION = "java";
	private static FileFilter DIR_FILTER = new FileFilter() {
		private static final char HIDDEN_DIR_PREFIX = '.';//like .git, .svn,....
		
		@Override
		public boolean accept(File dir) {
			return dir.isDirectory() && dir.getName().charAt(0) != HIDDEN_DIR_PREFIX && !dir.getName().equals("CVS");
		}
	};
	
	private static FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File f) {
			return f.isFile();
		}
	};
	
//	private final JContext context;
	private final Collection<ClassPathRoot> classPathRoots;
	private final JSourceFinderFilterCallback filter;
	private final JSourceFinderMatchedCallback matchedCallback;
	private final JSourceFinderIgnoredCallback ignoredCallback;
	private final JSourceFinderErrorCallback errorCallback;
	private final AstCreator astCreator;

	private final Matcher<JType> TYPE_MATCHER = new Matcher<JType>(){
		@Override
        public boolean matches(JType found) {
            return filter.matches(found);
        }
	};

	private final Matcher<JMethod> METHOD_MATCHER = new Matcher<JMethod>(){
		@Override
        public boolean matches(JMethod found) {
            return filter.matches(found);
        }
	};
	
	public static interface JSourceFinderMatchedCallback {
		public void onMatched(Object obj);
		public void onMatched(ClassPathRoot classPathRoot);
		public void onMatched(ClassPathResource child);
		public void onMatched(JSourceFile file);
		public void onMatched(JType type);
		public void onMatched(JMethod method);
	}

	public static interface JSourceFinderIgnoredCallback {
		public void onIgnored(Object obj);
		public void onIgnored(ClassPathRoot classPathRoot);
		public void onIgnored(ClassPathResource dirResource);
		public void onIgnored(JSourceFile file);
		public void onIgnored(JType type);
		public void onIgnored(JMethod method);
	}

	public static interface JSourceFinderFilterCallback {
		public boolean matches(Object obj);
		public boolean matches(ClassPathRoot root);
		public boolean matches(ClassPathResource resource);
		public boolean matches(JSourceFile file);
		public boolean matches(JType type);
		public boolean matches(JMethod method);
	}

	public static interface JSourceFinderErrorCallback {
		public void onError(Exception e);
	}
	
	public static Builder newBuilder(){
		return new Builder();
	}

	public JSourceFinder(
			AstCreator astCreator
			, Iterable<ClassPathRoot> classPathRoots
			, JSourceFinderFilterCallback filter
			, JSourceFinderMatchedCallback matchedCallback
			, JSourceFinderIgnoredCallback ignoredCallback
			, JSourceFinderErrorCallback errorCallback
			) {
		checkNotNull(classPathRoots, "expect class path roots");
		
		this.astCreator = checkNotNull(astCreator, "expect astCreator");
		this.filter = checkNotNull(filter, "expect filter");
		this.matchedCallback = checkNotNull(matchedCallback, "expect match callback");
		this.ignoredCallback = checkNotNull(ignoredCallback, "expect ignored callback");
		this.errorCallback = checkNotNull(errorCallback, "expect error callback");
		this.classPathRoots = ImmutableList.<ClassPathRoot>builder().addAll(classPathRoots).build();
	}


	public void visit(JSourceFileVisitor visitor) {
		for (JSourceFile srcFile : findSources()) {
			srcFile.visit(visitor);
		}
	}
	
	public Iterable<JMethod> findMethods() {
		final Iterable<JType> foundTypes = findTypes();
		return new Iterable<JMethod>() {
    		@Override
    		public Iterator<JMethod> iterator() {
    			return new FilteringIterator<JMethod>(new JMethodIterator(foundTypes.iterator()), toMethodMatcher());
    		}
    	};
	}

	public FindResult<JType> findTypes() {
		FindResult<JSourceFile> sources = findSources();
		return convertSourcesIntoTypes(sources).filter(toTypeMatcher());
    }
	
	private Matcher<JType> toTypeMatcher(){
		return TYPE_MATCHER;
	}
	
	private Matcher<JMethod> toMethodMatcher(){
		return METHOD_MATCHER;
	}
	
	private FindResult<JType> convertSourcesIntoTypes(FindResult<JSourceFile> sources){
		return FindResultIterableBacked.from(new JavaTypeIterator(sources.iterator()));
	}

	public FindResult<JSourceFile> findSources() {
		final Set<JSourceFile> sources = newHashSet();
		FindResult<ClassPathResource> resources = findResources();
		for (ClassPathResource resource :  resources) {
			if (resource.hasExtension(JAVA_EXTENSION)) {
				JSourceFile source = new JSourceFile(resource, astCreator);
				if( filter.matches((Object)source) && filter.matches(source) ){
					matchedCallback.onMatched(source);
					sources.add(source);
				} else {
					ignoredCallback.onIgnored(source);
				}
			}
		}
		return FindResultIterableBacked.from(sources);
	}
	
	public FindResult<ClassPathResource> findResources(){
		Collection<ClassPathResource> resources = newHashSet();
		Collection<ClassPathRoot> sourceDirs = getSourceDirsToSearch();
		for (ClassPathRoot root: sourceDirs) {
			if( filter.matches((Object)root) && filter.matches(root)){
    			if( root.isDirectory()){
    				matchedCallback.onMatched(root);
    				walkResourceDir(resources, root);
        		} else {
        			ignoredCallback.onIgnored(root);
        			//throw new CodemuckerException("can't currently walk non directory class path roots. Path: " + root.getPathName());
        		}
    		} else {
    			ignoredCallback.onIgnored(root);
    		}
		}
		return FindResultIterableBacked.from(resources);
	}
	
	private Collection<ClassPathRoot> getSourceDirsToSearch() {
		return classPathRoots;
	}
	
	private void walkResourceDir(Collection<ClassPathResource> found, ClassPathRoot root) {
		walkDir(found, root, "", root.getPath());
	}
	
	private void walkDir(Collection<ClassPathResource> found, ClassPathRoot rootDir, String parentPath, File dir) {
//		ClassPathResource dirResource = new ClassPathResource(rootDir, dir, parentPath + "/", false);
//		if (!filter.matches((Object)dirResource) || !filter.matches(dirResource)) {
//			ignoredCallback.onIgnored(dirResource);
//			return;
//		}
		File[] files = dir.listFiles(FILE_FILTER);
		for (File f : files) {
			String relPath = parentPath + "/" + f.getName();
			ClassPathResource child = new ClassPathResource(rootDir, f, relPath, false);
			if (filter.matches((Object)child) && filter.matches(child)) {
				matchedCallback.onMatched(child);
				found.add(child);
			} else {
				ignoredCallback.onIgnored(child);
			}
		}
		File[] childDirs = dir.listFiles(DIR_FILTER);
		for (File childDir : childDirs) {
			walkDir(found, rootDir, parentPath + "/" + childDir.getName(), childDir);
		}
	}
	
	private static class JMethodIterator implements Iterator<JMethod> {

		private final Iterator<JType> types;
		
		private Iterator<JMethod> methods;
		private JMethod nextMethod;

		public JMethodIterator(Iterator<JType> types) {
			checkNotNull("types", types);
			this.types = types;
			this.nextMethod = nextMethod();
		}
		
		@Override
		public boolean hasNext() {
			return nextMethod != null;
		}

		@Override
		public JMethod next() {
			if (nextMethod == null) {
				throw new NoSuchElementException();
			}
			JMethod ret = nextMethod;
			nextMethod = nextMethod();
			return ret;
		}

		private JMethod nextMethod() {
			//does current iterator have any more?
			if (methods != null && methods.hasNext()) {
				return methods.next();
			}
			//move on to the next type
			while (types.hasNext()) {
				methods = types.next().findAllJavaMethods().iterator();
				if( methods.hasNext()){
					return methods.next();
				}
			}
			//no more methods
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}	
	}
	
	private static class JavaTypeIterator implements Iterator<JType> {

		private final Iterator<JSourceFile> sources;
		
		private Iterator<JType> types;
		private JType nextType;

		JavaTypeIterator(Iterator<JSourceFile> sources) {
			checkNotNull("sources", sources);
			this.sources = sources;
			this.nextType = nextType();
		}
		
		@Override
		public boolean hasNext() {
			return nextType != null;
		}

		@Override
		public JType next() {
			if (nextType == null) {
				throw new NoSuchElementException();
			}
			JType ret = nextType;
			nextType = nextType();
			return ret;
		}

		private JType nextType() {
			//does current iterator have any more?
			if (types != null && types.hasNext()) {
				return types.next();
			}
			//move on to the next source file
			while (sources.hasNext()) {
				types = sources.next().findAllTypes().iterator();
				if( types.hasNext()){
					return types.next();
				}
			}
			//no more types
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}	
	}
	
	public static class Builder {
		private AstCreator astCreator;
		private List<ClassPathRoot> classPathRoots = newArrayList();
		private JSourceFinderMatchedCallback findMatchedCallback;
		private JSourceFinderIgnoredCallback findIgnoredCallback;
		private JSourceFinderErrorCallback findErrorCallback;
		private JSourceFinderFilterCallback finderFilter;
		
		public JSourceFinder build(){			
			return new JSourceFinder(
				toAstCreator()
				, classPathRoots
				, toFilter()
				, toMatchedCallback()
				, toIgnoredCallback()
				, toErrorCallback()
			);
		}
		
		public Builder copyOf() {
			Builder copy = new Builder();
			copy.astCreator = astCreator;
			copy.classPathRoots.addAll(classPathRoots);
			copy.finderFilter = finderFilter;
			copy.findMatchedCallback = findMatchedCallback;
			copy.findIgnoredCallback = findIgnoredCallback;
			copy.findErrorCallback = findErrorCallback;
			return copy;
		}

		private AstCreator toAstCreator(){
			return astCreator != null?astCreator:new DefaultAstCreator();
		}
	
		private JSourceFinderIgnoredCallback toIgnoredCallback() {
			return findIgnoredCallback != null ? findIgnoredCallback : new BaseIgnoredCallback();
		}

		private JSourceFinderMatchedCallback toMatchedCallback() {
			return findMatchedCallback != null ? findMatchedCallback : new BaseMatchedCallback();
		}
		
		private JSourceFinderErrorCallback toErrorCallback() {
			return findErrorCallback != null ? findErrorCallback : new LoggingErrorCallback();
		}

		private JSourceFinderFilterCallback toFilter(){
			return finderFilter != null? finderFilter:new BaseAllowAllFilter();
		}

	 	public Builder setSearchPaths(SearchPathsBuilder builder) {
        	setSearchPaths(builder.build());
        	return this;
        }
	 	
	 	public Builder setSearchPaths(Iterable<ClassPathRoot> classPathRoots) {
        	this.classPathRoots = nullSafeList(classPathRoots);
        	return this;
        }
	 	
	 	private static <T> List<T> nullSafeList(Iterable<T> iter){
	 		if( iter == null){
	 			return newArrayList();
	 		}
	 		return newArrayList(iter);
	 	}

		public Builder setFilter(FilterBuilder builder) {
        	setFilter(builder.build());
        	return this;
		}
		
		public Builder setFilter(JSourceFinderFilterCallback filter) {
        	this.finderFilter = filter;
        	return this;
		}

		public Builder setMatchedCallback(JSourceFinderMatchedCallback callback) {
        	this.findMatchedCallback = callback;
        	return this;
        }

		public Builder setIgnoredCallback(JSourceFinderIgnoredCallback callback) {
        	this.findIgnoredCallback = callback;
        	return this;
        }

		public Builder setErrorCallback(JSourceFinderErrorCallback callback) {
        	this.findErrorCallback = callback;
        	return this;
        }

		public Builder setAstCreator(AstCreator astCreator) {
        	this.astCreator = astCreator;
        	return this;
        }
	}
	
	public static class BaseAllowAllFilter implements JSourceFinderFilterCallback {

		@Override
        public boolean matches(Object obj) {
	        return true;
        }

		@Override
        public boolean matches(ClassPathRoot root) {
	        return true;
        }

		@Override
        public boolean matches(ClassPathResource resource) {
	        return true;
        }

		@Override
        public boolean matches(JSourceFile file) {
	        return true;
        }

		@Override
        public boolean matches(JType type) {
	        return true;
        }

		@Override
        public boolean matches(JMethod method) {
	        return true;
        }
	}
	
	public static class BaseIgnoredCallback implements JSourceFinderIgnoredCallback {

		@Override
        public void onIgnored(Object object) {
        }
		
		@Override
        public void onIgnored(ClassPathRoot root) {
        }

		@Override
        public void onIgnored(JSourceFile source) {
        }

		@Override
        public void onIgnored(JType type) {
        }

		@Override
        public void onIgnored(JMethod method) {
        }

		@Override
        public void onIgnored(ClassPathResource resource) {
        }

	}
	
	public static class BaseMatchedCallback implements JSourceFinderMatchedCallback {

		@Override
        public void onMatched(Object obj) {
        }
		
		@Override
        public void onMatched(ClassPathRoot root) {
        }

		@Override
        public void onMatched(JSourceFile source) {
        }

		@Override
        public void onMatched(JType type) {
        }

		@Override
        public void onMatched(JMethod method) {
        }

		@Override
        public void onMatched(ClassPathResource resource) {
        }
	}
	
	public static class LoggingErrorCallback implements JSourceFinderErrorCallback {
		private final Logger logger;
		
		public LoggingErrorCallback(){
			this(Logger.getLogger(LoggingMatchedCallback.class));
		}
		
		public LoggingErrorCallback(Logger logger){
			this.logger = checkNotNull(logger, "expect logger");
		}

		@Override
        public void onError(Exception e) {
			logger.warn("error", e);
		}
	}
	
}
