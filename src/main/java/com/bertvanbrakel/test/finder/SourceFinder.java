package com.bertvanbrakel.test.finder;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.test.generation.AstCreator;
import com.bertvanbrakel.test.generation.SourceFileVisitor;
import com.bertvanbrakel.test.util.ProjectFinder;
import com.bertvanbrakel.test.util.SourceUtil;

/**
 * Utility class to find source classes in ones project
 */
public class SourceFinder {

	final ClassFinderOptions options;
	
	public SourceFinder(){
		this(new ClassFinderOptions());
	}

	public SourceFinder(ClassFinderOptions options){
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
	
	public Collection<String> findSourceFilePaths() {
		return findResourcesIn(findSourceDir());
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
		return new AstCreator() {
			private ASTParser parser = SourceUtil.newParser();
			
			@Override
			public synchronized CompilationUnit create(File srcFile) {
				String src = SourceUtil.readSource(srcFile);
				CompilationUnit cu = parseCompilationUnit(src);
				cu.recordModifications();
				return cu;
			}

			@Override
            public ASTNode parseAstSnippet(CharSequence src) {
				return createNode(src,ASTParser.K_EXPRESSION);
            }

			@Override
            public CompilationUnit parseCompilationUnit(CharSequence src) {
				CompilationUnit cu = (CompilationUnit) createNode(src,ASTParser.K_COMPILATION_UNIT);
				return cu;
			}
			
			private ASTNode createNode(CharSequence src, int kind){
				parser.setSource(src.toString().toCharArray());
				parser.setKind(kind);
				ASTNode node = parser.createAST(null);
				return node;
			}
		};
	}
	
	public Iterable<JavaSourceFile> findSourceFiles(final AstCreator astCreator) {
		final Collection<JavaSourceFile> foundSources = new HashSet<JavaSourceFile>();
		for (File classPath : getSourceDirsToSearch()) {
			for (String resource : findResourcesIn(classPath)) {
				if (resource.endsWith(".java")) {
					foundSources.add(new JavaSourceFile(astCreator, new ClasspathLocation(classPath, resource)));
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

	public Collection<String> findResourcesIn(File rootDir) {
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
