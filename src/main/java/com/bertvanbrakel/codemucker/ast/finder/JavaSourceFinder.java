package com.bertvanbrakel.codemucker.ast.finder;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.bertvanbrakel.codemucker.ast.AstCreator;
import com.bertvanbrakel.codemucker.ast.DefaultAstCreator;
import com.bertvanbrakel.codemucker.ast.JavaSourceFile;
import com.bertvanbrakel.codemucker.ast.JavaSourceFileVisitor;
import com.bertvanbrakel.codemucker.ast.JavaType;
import com.bertvanbrakel.test.finder.FileWalkerFilter;
import com.bertvanbrakel.test.util.ProjectFinder;

import static com.bertvanbrakel.lang.Check.*;

/**
 * Utility class to find source classes in ones project
 */
public class JavaSourceFinder {

	private final SourceFinderOptions options;

	private static final JavaSourceFileMatcher ALL_MATCHER = new JavaSourceFileMatcher() {
		@Override
		public boolean matchSource(JavaSourceFile found) {
			return true;
		}
	};

	public JavaSourceFinder() {
		this(new SourceFinderOptions());
	}

	public JavaSourceFinder(SourceFinderOptions options) {
		this.options = options;
	}

	public SourceFinderOptions getOptions() {
		return options;
	}

	public void visit(JavaSourceFileVisitor visitor) {
		visit(visitor, ALL_MATCHER);
	}

	public void visit(JavaSourceFileVisitor visitor, JavaSourceFileMatcher matcher) {
		visit(visitor, matcher, createDefaultASTCreator());
	}

	public void visit(JavaSourceFileVisitor visitor, JavaSourceFileMatcher matcher, AstCreator astCreator) {
		for (JavaSourceFile srcFile : findSourceFiles(astCreator, matcher)) {
			srcFile.visit(visitor);
		}
	}

	public File findSourceDir() {
		return ProjectFinder.findDefaultMavenSrcDir();
	}

	public File findTestSourceDir() {
		return ProjectFinder.findDefaultMavenTestDir();
	}

	// public Iterable<CompilationUnit> findCompilationUnits() {
	// return findCompilationUnits(createDefaultASTCreator());
	// }
	//
	// public Iterable<CompilationUnit> findCompilationUnits(AstCreator
	// astCreator) {
	// final Iterable<JavaSourceFile> sources = findSourceFiles(astCreator);
	// return new Iterable<CompilationUnit>(){
	// @Override
	// public Iterator<CompilationUnit> iterator() {
	// return new CompilationUnitIterator(sources.iterator());
	// }
	// };
	// }

	public Iterable<JavaType> findTypes(final  JavaSourceFileMatcher sourceMatcher, final JavaTypeMatcher typeMatcher) {
		final Iterable<JavaSourceFile> sources = findSourceFiles(sourceMatcher);
		return new Iterable<JavaType>() {
			@Override
			public Iterator<JavaType> iterator() {
				return new JavaTypeFilteringIterator(new JavaTypeIterator(sources.iterator()), typeMatcher);
			}
		};
	}
	
	public Iterable<JavaSourceFile> findSourceFiles() {
		return findSourceFiles(options.toJavaSourceMatcher());
	}
	
	public Iterable<JavaSourceFile> findSourceFiles(JavaSourceFileMatcher matcher) {
		return findSourceFiles(createDefaultASTCreator(), matcher);
	}

	private AstCreator createDefaultASTCreator() {
		return new DefaultAstCreator();
	}

	public Iterable<JavaSourceFile> findSourceFiles(final AstCreator astCreator, final JavaSourceFileMatcher matcher) {
		final Collection<JavaSourceFile> foundSources = new HashSet<JavaSourceFile>();
		for (File classDir : getSourceDirsToSearch()) {
			for (ClasspathResource resource : findClassResourcesIn(classDir)) {
				if (resource.isExtension("java")) {
					foundSources.add(new JavaSourceFile(resource, astCreator));
				}
			}
		}
		return new Iterable<JavaSourceFile>() {
			@Override
			public Iterator<JavaSourceFile> iterator() {
				return new JavaSourceFileIterator(foundSources.iterator(), matcher);
			}
		};
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

	public Iterable<ClasspathResource> findClassResourcesIn(File rootDir) {
		final Collection<ClasspathResource> found = new HashSet<ClasspathResource>();
		for (String relativePath : findRelativePathsIn(rootDir)) {
			found.add(new ClasspathResource(rootDir, relativePath));
		}
		return found;
	}

	public Collection<String> findRelativePathsIn(File rootDir) {
		FileWalkerFilter walker = new FileWalkerFilter();
		walker.setIncludes(options.toFileMatcher());

		Collection<String> resources = walker.findFiles(rootDir);
		return resources;
	}

	private static class JavaSourceFileIterator implements Iterator<JavaSourceFile> {

		private final Iterator<JavaSourceFile> sources;
		private final JavaSourceFileMatcher matcher;
		private JavaSourceFile nextFile;

		private JavaSourceFileIterator(Iterator<JavaSourceFile> sources, JavaSourceFileMatcher matcher) {
			checkNotNull("sources", sources);
			checkNotNull("matcher", matcher);
			this.sources = sources;
			this.matcher = matcher;

			nextFile = nextFile();
		}

		@Override
		public boolean hasNext() {
			return nextFile != null;
		}

		@Override
		public JavaSourceFile next() {
			if (nextFile == null) {
				throw new NoSuchElementException();
			}
			JavaSourceFile ret = nextFile;
			nextFile = nextFile();
			return ret;
		}

		private JavaSourceFile nextFile() {
			while (sources.hasNext()) {
				JavaSourceFile file = sources.next();
				if (matcher.matchSource(file)) {
					return file;
				}
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private static class JavaTypeIterator implements Iterator<JavaType> {

		private final Iterator<JavaSourceFile> sources;
		
		private Iterator<JavaType> types;
		private JavaType nextType;

		JavaTypeIterator(Iterator<JavaSourceFile> sources) {
			checkNotNull("sources", sources);
			this.sources = sources;
			nextType = nextType();
		}
		
		@Override
		public boolean hasNext() {
			return nextType != null;
		}

		@Override
		public JavaType next() {
			if (nextType == null) {
				throw new NoSuchElementException();
			}
			JavaType ret = nextType;
			nextType = nextType();
			return ret;
		}

		private JavaType nextType() {
			if (types != null && types.hasNext()) {
				return types.next();
			}
			while (sources.hasNext()) {
				types = sources.next().getJavaTypes().iterator();
				if( types.hasNext()){
					return types.next();
				}
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}	
	}
	
	private static class JavaTypeFilteringIterator implements Iterator<JavaType> {

		private final Iterator<JavaType> types;
		private final JavaTypeMatcher matcher;
		private JavaType nextType;

		private JavaTypeFilteringIterator(Iterator<JavaType> types, JavaTypeMatcher matcher) {
			checkNotNull("types", types);
			checkNotNull("matcher", matcher);
			this.types = types;
			this.matcher = matcher;

			nextType = nextType();
		}

		@Override
		public boolean hasNext() {
			return nextType != null;
		}

		@Override
		public JavaType next() {
			if (nextType == null) {
				throw new NoSuchElementException();
			}
			JavaType ret = nextType;
			nextType = nextType();
			return ret;
		}

		private JavaType nextType() {
			while (types.hasNext()) {
				JavaType type = types.next();
				if (matcher.matchType(type)) {
					return type;
				}
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
