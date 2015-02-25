package org.codemucker.jmutate;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;

import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Roots;
import org.codemucker.jfind.matcher.ARootResource;
import org.codemucker.jmatch.AString;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.lang.annotation.Optional;

import com.google.inject.Inject;

/**
 * Find source code given a full class name
 */
public class CodeFinder {

	private JAstParser parser;
	private Roots.Builder scanRoots;
	private boolean failOnNotFound;
	
	/**
	 * If true then fai if no type or source could be found
	 */
	@Optional
	public CodeFinder failOnNotFound(boolean failOnNotFound) {
		this.failOnNotFound = failOnNotFound;
		return this;
	}

	/**
	 * Used by the DI container to set the default
	 */
	@Inject
    public void injectParser(JAstParser parser) {
	    parser(parser);
    }

	public CodeFinder parser(JAstParser parser) {
		this.parser = parser;
		return this;
	}

	/**
	 * Set the roots. If not set use the roots from the parser
	 * @param roots
	 * @return
	 */
	@Optional
	public CodeFinder scanRoots(Roots.Builder roots) {
		this.scanRoots = roots;
		return this;
	}
	
	/**
	 * @see {@link CodeFinder#findJSourceForClass(String)}
	 */
	public JType findJTypeForClass(Class<?> classToFindSourceFor){
		return findJTypeForClass(classToFindSourceFor.getName());
	}

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
	public JType findJTypeForClass(String fullClassName){
		checkAllSet();
		
		JSourceFile source = findJSourceForClass(fullClassName);
		if(source != null){
			int startInner = fullClassName.indexOf('$');
			if(startInner == -1){
				return source.getMainType();
			}
			String[] innerNames = fullClassName.substring(startInner + 1).split("\\.");
			JType type  = source.getMainType();
			for(int i = 1; i < innerNames.length;i++){
				type = type.getChildTypeWithNameOrNull(innerNames[i]);
				if(type == null){
					if(failOnNotFound){
						throw new NoSuchElementException("Couldn't find " + fullClassName.substring(startInner + 1) + " in source " + source.getResource().getFullPath() + " for class " + fullClassName);
					} else {
						return null;
					}
				}
			}
			return type;
		}
		return null;
	}

	public JSourceFile findJSourceForClass(Class<?> classToFindSourceFor){
		return findJSourceForClass(classToFindSourceFor.getName());
	}
	
	public JSourceFile findJSourceForClass(String  fullClassToFindSourceFor){
		checkAllSet();
		
		String baseClassName = fullClassToFindSourceFor;
		int startInner = baseClassName.indexOf('$');
		if(startInner != -1){
			baseClassName = baseClassName.substring(0,startInner);
		}
		SourceScanner finder = SourceScanner.with()
				.scanRoots(getRoots())
				.parser(parser)
				.filter(SourceFilter.with()
						.resourceMatches(ARootResource.with().className(AString.equalTo(baseClassName))))
			.build();
		FindResult<JSourceFile> sources = finder.findSources();
		
		//Expect.that(sources).is(AList.withOnly(AJSourceFile.any()));
		if(!failOnNotFound && sources.isEmpty()){
			return null;
		}
		assertEquals("expected a single source match",1,sources.toList().size());
		return sources.getFirst();
	}
	
	private Iterable<Root> getRoots(){
		if(scanRoots == null){
			return parser.getResourceLoader().getAllRoots(); 
		}
		return scanRoots.build();
	}
	
	private void checkAllSet(){
		if(parser==null){
			throw new IllegalStateException("need to set the parser");
		}
	}
}
