package org.codemucker.jmutate;

import java.util.NoSuchElementException;

import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;

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
		JType type = newCodeFinder().findJTypeForClass(classToFindSourceFor);
		
		if(type == null){
			throw new NoSuchElementException("could not find type source for type " + classToFindSourceFor.getName());
		}
		return type;
	}

	public static JSourceFile findSourceForClass(Class<?> classToFindSourceFor){
		JSourceFile source = newCodeFinder().findJSourceForClass(classToFindSourceFor);
		if(source == null){
			throw new NoSuchElementException("could not find source for type " + classToFindSourceFor.getName());
		}
		return source;
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
	
	private static CodeFinder newCodeFinder() {
		return new CodeFinder()
			.parser(newResolvingParser())
			.failOnNotFound(true)
			.scanRoots(Roots.with()
				.mainSrcDir(false)
				.testSrcDir(true));
	}
	
	public static JAstParser newResolvingParser(){
		return JAstParser.with()
			.checkParse(true)
			.resolveBindings(true)
			.resourceLoader(Roots.with().all())
			.build();
	}
}
