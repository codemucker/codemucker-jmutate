package com.bertvanbrakel.codemucker.ast.finder;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.codemucker.ast.AstCreator;
import com.bertvanbrakel.codemucker.ast.DefaultAstCreator;
import com.bertvanbrakel.codemucker.ast.JavaSourceFile;
import com.bertvanbrakel.codemucker.ast.SourceFileVisitor;
import com.bertvanbrakel.test.finder.ClassFinderOptions;
import com.bertvanbrakel.test.finder.FileWalkerFilter;
import com.bertvanbrakel.test.util.ProjectFinder;

/**
 * Utility class to find source classes in ones project
 */
public class JavaSourceFinder {

	private final ClassFinderOptions options;
	
	public JavaSourceFinder(){
		this(new ClassFinderOptions());
	}

	public JavaSourceFinder(ClassFinderOptions options){
		this.options = options;
	}	

	public ClassFinderOptions getOptions() {
	    return options;
    }
	
	public void visit(SourceFileVisitor visitor) {
		visit(visitor, createDefaultASTCreator());
	}

	public void visit(SourceFileVisitor visitor, AstCreator astCreator) {
		for( JavaSourceFile srcFile:findSourceFiles(astCreator)){
			srcFile.visit(visitor);
		}
	}
	
	public File findSourceDir() {
		return ProjectFinder.findDefaultMavenSrcDir();
	}
	
	public File findTestSourceDir() {
		return ProjectFinder.findDefaultMavenTestDir();
	}

	public Iterable<CompilationUnit> findCompilationUnits() {
		return findCompilationUnits(createDefaultASTCreator());
	}

	public Iterable<CompilationUnit> findCompilationUnits(AstCreator astCreator) {
		final Iterable<JavaSourceFile> sources = findSourceFiles(astCreator);
		return new Iterable<CompilationUnit>(){
			@Override
			public Iterator<CompilationUnit> iterator() {
				return new CompilationUnitIterator(sources.iterator());
			}
		};
	}
	
	public Iterable<JavaSourceFile> findSourceFiles() {
		return findSourceFiles(createDefaultASTCreator());
	}

	private AstCreator createDefaultASTCreator(){
		return new DefaultAstCreator();
	}
	
	public Iterable<JavaSourceFile> findSourceFiles(final AstCreator astCreator) {
		final Collection<JavaSourceFile> foundSources = new HashSet<JavaSourceFile>();
		for (File classDir: getSourceDirsToSearch()) {
			for (ClasspathResource resource : findClassResourcesIn(classDir)) {
				if (resource.isExtension("java")) {
					foundSources.add(new JavaSourceFile(astCreator, resource));
				}
			}
		}
		return foundSources;
	}

	private Collection<File> getSourceDirsToSearch(){
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
	
	
	private static class CompilationUnitIterator implements Iterator<CompilationUnit>{

		private final Iterator<JavaSourceFile> sources;
		
		private CompilationUnitIterator(Iterator<JavaSourceFile> sources){
			this.sources = sources;
		}
		
		@Override
        public boolean hasNext() {
	        return sources.hasNext();
        }

		@Override
		public CompilationUnit next() {
			return sources.next().getCompilationUnit();
		}

		@Override
        public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
