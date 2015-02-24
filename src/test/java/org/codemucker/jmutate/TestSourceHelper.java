package org.codemucker.jmutate;

import static org.junit.Assert.assertEquals;

import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Roots;
import org.codemucker.jfind.matcher.ARootResource;
import org.codemucker.jmatch.AString;
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
		JSourceFile source = findSourceForClass(classToFindSourceFor);
		
		int startInner = classToFindSourceFor.getName().indexOf('$');
		if(startInner == -1){
			return source.getMainType();
		}
		String[] innerNames = classToFindSourceFor.getName().substring(startInner + 1).split("\\.");
		JType type  = source.getMainType();
		for(int i = 1; i < innerNames.length;i++){
			type = type.getChildTypeWithName(innerNames[i]);	
		}
		return type;
	}
	
	public static JSourceFile findSourceForClass(Class<?> classToFindSourceFor){
		String baseClassName = classToFindSourceFor.getName();
		int startInner = baseClassName.indexOf('$');
		if(startInner != -1){
			baseClassName = baseClassName.substring(0,startInner);
		}
		SourceScanner finder = newAllSourcesResolvingFinder()
			.filter(SourceFilter.with()
				.resourceMatches(ARootResource.with().className(AString.equalTo(baseClassName))))
			.build();
		FindResult<JSourceFile> sources = finder.findSources();
		
		//Expect.that(sources).is(AList.withOnly(AJSourceFile.any()));
		
		assertEquals("expected a single match",1,sources.toList().size());
		return sources.getFirst();
	}
	
	
	/**
	 * Look in all source locations including tests
	 * @return
	 */
	public static SourceScanner.Builder newAllSourcesResolvingFinder(){
		return SourceScanner.with()
			.scanRoots(Roots.with()
					.mainSrcDir(true)
					.testSrcDir(true)
					.classpath(true))
			.parser(newResolvingParser());
	}
	
	public static SourceScanner.Builder newTestSourcesResolvingFinder(){
		return SourceScanner.with()
			.scanRoots(Roots.with()
				.mainSrcDir(false)
				.testSrcDir(true))
			.parser(
				newResolvingParser());
	}
	
	public static JAstParser newResolvingParser(){
		return JAstParser.with()
			.checkParse(true)
			.resolveBindings(true)
			.resourceLoader(Roots.with().all())
			.build();
	}
}
