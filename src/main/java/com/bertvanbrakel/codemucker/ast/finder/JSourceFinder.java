package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.bertvanbrakel.codemucker.ast.AstCreator;
import com.bertvanbrakel.codemucker.ast.DefaultJContext;
import com.bertvanbrakel.codemucker.ast.JContext;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.codemucker.ast.JSourceFileVisitor;
import com.bertvanbrakel.codemucker.ast.finder.matcher.Matcher;
import com.bertvanbrakel.test.finder.FileMatcher;
import com.bertvanbrakel.test.finder.FileWalkerFilter;
import com.bertvanbrakel.test.util.ProjectFinder;

/**
 * Utility class to find source files, java types, and methods
 */
public class JSourceFinder {

	private static final String JAVA_EXTENSION = "java";
	private final JSourceFinderOptions options;
	private final JContext context;

	public JSourceFinder() {
		this(new JSourceFinderOptions());
	}

	public JSourceFinder(JContext context) {
		this(context, new JSourceFinderOptions());
	}

	public JSourceFinder(JSourceFinderOptions options) {
		this(new DefaultJContext(), options);
	}

	public JSourceFinder(JContext context, JSourceFinderOptions options) {
		checkNotNull(context, "expect a context");
		checkNotNull(options, "expect options");
		
		this.context = context;
		this.options = options;
	}

	public JSourceFinderOptions getOptions() {
		return options;
	}

	public void visit(JSourceFileVisitor visitor) {
		visit(visitor, options.toSourceMatcher());
	}

	public void visit(JSourceFileVisitor visitor, Matcher<JSourceFile> matcher) {
		visit(visitor, matcher, getDefaultASTCreator());
	}

	public void visit(JSourceFileVisitor visitor, Matcher<JSourceFile> matcher, AstCreator astCreator) {
		for (JSourceFile srcFile : findSources(astCreator, matcher)) {
			srcFile.visit(visitor);
		}
	}

	public File findSourceDir() {
		return ProjectFinder.findDefaultMavenSrcDir();
	}

	public File findTestSourceDir() {
		return ProjectFinder.findDefaultMavenTestDir();
	}

	public Iterable<JMethod> findMethods() {
		return findMethods( options.toMethodMatcher());
	}
	
	public Iterable<JMethod> findMethods(Matcher<JMethod> matcher) {
		return findMethods( options.toSourceMatcher(),options.toTypeMatcher(),matcher);
	}
	
	public Iterable<JMethod> findMethods(final  Matcher<JSourceFile> sourceMatcher, final Matcher<JType> typeMatcher, final Matcher<JMethod> methodMatcher) {
		final Iterable<JType> foundTypes = findTypes(sourceMatcher, typeMatcher);
		return new Iterable<JMethod>() {
    		@Override
    		public Iterator<JMethod> iterator() {
    			return new FilteringIterator<JMethod>(new JMethodIterator(foundTypes.iterator()), methodMatcher);
    		}
    	};
	}

	public FindResult<JType> findTypes() {
		return findTypes(options.toTypeMatcher());
	}
	
	public FindResult<JType> findTypes(final Matcher<JType> typeMatcher) {
    	return findTypes(options.toSourceMatcher(),typeMatcher);
    }

	public FindResult<JType> findTypes(Matcher<JSourceFile> sourceMatcher, final Matcher<JType> typeMatcher) {
		FindResult<JSourceFile> sources = findSources(sourceMatcher);
		return convertSourcesIntoTypes(sources).filter(typeMatcher);
    }
	
	private FindResult<JType> convertSourcesIntoTypes(FindResult<JSourceFile> sourceResults){
		JavaTypeIterator typeIterator = new JavaTypeIterator(sourceResults.iterator());
		return FindResultImpl.from(typeIterator);
	}
	
	public FindResult<JSourceFile> findSources() {
		return findSources(options.toSourceMatcher());
	}
	
	public FindResult<JSourceFile> findSources(Matcher<JSourceFile> matcher) {
		return findSources(getDefaultASTCreator(), matcher);
	}

	private AstCreator getDefaultASTCreator() {
		return context.getAstCreator();
	}

	public FindResult<JSourceFile> findSources(final AstCreator astCreator, final Matcher<JSourceFile> matcher) {
		final Collection<JSourceFile> sources = newHashSet();
		FindResult<ClasspathResource> resources = findResources();
		for (ClasspathResource resource :  resources) {
			if (resource.isExtension(JAVA_EXTENSION)) {
				sources.add(new JSourceFile(resource, astCreator));
			}
		}
		return FindResultImpl.from(sources).filter(matcher);
	}
	
	public FindResult<ClasspathResource> findResources(){
		return findResources(options.toFileMatcher());
	}

	public FindResult<ClasspathResource> findResources(FileMatcher fileMatcher){
		Collection<File> sourceDirs = getSourceDirsToSearch();
		Collection<ClasspathResource> resources = newArrayList();
		for (File classDir : sourceDirs) {
			//TODO:expose classpath resources as a public finder method
			FindResult<ClasspathResource> foundInClassDir = findClassResourcesIn(classDir, fileMatcher);
			resources.addAll(foundInClassDir.asList());
		}
		return FindResultImpl.from(resources);
	}
	
	private Collection<File> getSourceDirsToSearch() {
		Collection<File> classPathDirs = new HashSet<File>(options.getClassPathsDir());
		if (options.isIncludeClassesDir()) {
			classPathDirs.add(findSourceDir());
		}
		if (options.isIncludeTestDir()) {
			classPathDirs.add(findTestSourceDir());
		}
		return classPathDirs;
	}

	public FindResult<ClasspathResource> findClassResourcesIn(File rootDir, FileMatcher fileMatcher) {
		final Collection<ClasspathResource> found = newHashSet();
		for (String relativePath : findRelativePathsIn(rootDir, fileMatcher)) {
			found.add(new ClasspathResource(rootDir, relativePath));
		}
		return FindResultImpl.from(found);
	}

	public Collection<String> findRelativePathsIn(File rootDir, FileMatcher fileMatcher) {
		FileWalkerFilter walker = new FileWalkerFilter();
		walker.setIncludes(fileMatcher);

		Collection<String> resources = walker.findFiles(rootDir);
		return resources;
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

}
