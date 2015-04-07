package org.codemucker.jmutate.generate.matcher;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.ClassFilter;
import org.codemucker.jfind.ClassScanner;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.matcher.AClass;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jfind.matcher.ARootResource;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ResourceLoader;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceScanner;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;

import com.google.common.base.Strings;

/**
 * Scans for sources and compiled classes. Allows for scanning of current project sources and third party compiled libs
 */
public class PojoSourceAndClassScanner {
    
    private static final Logger log = LogManager.getLogger(PojoSourceAndClassScanner.class);
    
    private final ResourceLoader resourceLoader;
    private final Matcher<Root> scanRootMatcher;
    private final Matcher<String> scanForClassNameMatcher;
    private final Matcher<Class<?>> scanForClasses;
    private final Matcher<JType> scanForTypes;
    
    public PojoSourceAndClassScanner(ResourceLoader resourceLoader,String dependenciesExpression,String pojoNameExpression, String pojoClassExpression) {
        this.resourceLoader = resourceLoader;
        this.scanRootMatcher = ARoot.with().dependenciesExpression(dependenciesExpression);
        this.scanForClassNameMatcher = AString.matchingExpression(pojoNameExpression);
        this.scanForClasses = AClass.with().fullName(scanForClassNameMatcher).expression(pojoClassExpression);
        this.scanForTypes= AJType.with().fullName(scanForClassNameMatcher).expression(pojoClassExpression);
        
        if(log.isDebugEnabled()){
	        log.debug("  looking for classes in:");
	        log.debug("      pojoDependencies: " + (Strings.isNullOrEmpty(dependenciesExpression)?"<any>":dependenciesExpression));
	        log.debug("      pojoNames: " + (Strings.isNullOrEmpty(pojoNameExpression)?"<any>":pojoNameExpression));
	        log.debug("      pojoClassExpression: " + (Strings.isNullOrEmpty(pojoClassExpression)?"<any>":pojoClassExpression));
	        
	        log.debug("      pojoNameMatcher : " + scanForClassNameMatcher);
	        log.debug("      pojoClassMatcher : " + scanForClasses);
	        log.debug("      pojoTypeMatcher : " + scanForTypes);

            log.debug("scanning for pojos in roots:");
            for(Root root:resourceLoader.getAllRoots()){
                if(scanRootMatcher.matches(root)){
                    log.debug("INCLUDE " + root);
                }
            }
            //log.debug("ignored roots:");
            for(Root root:resourceLoader.getAllRoots()){
                if(!scanRootMatcher.matches(root)){
                	log.debug("EXCLUDE " + root);
                }
            }
        }
    }
    
    public FindResult<Class<?>> scanForReflectedClasses(){
        log.debug("scanning for compiled pojos");
        FindResult<Class<?>> results = ClassScanner.with()
                .scanRoots(resourceLoader.getAllRoots())
                .classLoader(resourceLoader.getClassLoader())
                .filter(ClassFilter.where()
                        .rootMatches(scanRootMatcher)
                        .resourceMatches(ARootResource.with().className(scanForClassNameMatcher))
                        .classMatches(scanForClasses))
                .build()
                .findClasses();
        
        log.debug("found: " + results.toList().size() + " compiled pojos");
        return results;
    }
    
    public FindResult<JType> scanSources() {
    	log.debug("scanning for source pojos");
    	FindResult<JType> results = SourceScanner.with()
                .scanRoots(resourceLoader.getAllRoots())
                .filter(SourceFilter.where()
                        .rootMatches(scanRootMatcher)
                        .resourceMatches(ARootResource.with().className(scanForClassNameMatcher))
                        .typeMatches(scanForTypes))
                .build()
                .findTypes();
        
        log.debug("found: " + results.toList().size() + " source pojos");
        return results;
    }
}