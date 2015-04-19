package org.codemucker.jmutate;

import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.ast.DefaultJAstParser;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.MutateUtil.NodeParser;

public class TestSourceHelper {

	/**
	 * Find the source file for the given compiled class
	 * @param classToFindSourceFor the class to find the source for
	 * @return the found source file, or throw an exception if no source found
	 */
	/**
	 * Find the source file for the given compiled class
	 * @param classToFindSourceFor the class to find the source for
	 * @return the found source file, or throw an exception if no source found
	 */
	public static JType findTypeNodeForClass(Class<?> classToFindSourceFor){
		return newFailingSourceLoader().loadTypeForClass(classToFindSourceFor);
	}

	public static JSourceFile findSourceForClass(Class<?> classToFindSourceFor){
		return newFailingSourceLoader().loadSourceForClass(classToFindSourceFor);
	}
	
	/**
	 * Look in all source locations including tests
	 * @return
	 */
	public static SourceScanner.Builder newSourceScannerAllSrcs(){
		return SourceScanner.with()
			.scanRoots(Roots.with()
					.mainSrcDir(true)
					.testSrcDir(true)
					.classpath(true))
			.parser(newResolvingParser());
	}
	
	private static SourceLoader newFailingSourceLoader() {
		//TODO:parser does not setSOurceLoader! need to wrap it!
		JAstParser parser = newResolvingParser();
		ResourceLoader resourceLoader = DefaultResourceLoader.with()
				.roots(Roots.with()
					.mainSrcDir(true)
					.testSrcDir(true))
				.build();
		NodeParser wrapped = MutateUtil.wrapParser(resourceLoader, parser);            
		SourceLoader sourceLoader = new DefaultSourceLoader(resourceLoader, wrapped, /*fail on not found */ true );
		wrapped.setSourceLoader(sourceLoader);
		
		return sourceLoader;
	}
	
	public static JAstParser newResolvingParser(){
		return DefaultJAstParser.with()
			.checkParse(true)
			.resolveBindings(true)
			.resourceLoader(Roots.with().all())
			.build();
	}
}
