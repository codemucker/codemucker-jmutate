package org.codemucker.jmutate.generate.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.BaseMatchListener;
import org.codemucker.jfind.ClassFilter;
import org.codemucker.jfind.ClassResource;
import org.codemucker.jfind.ClassScanner;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.MatchListener;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.matcher.AClass;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jfind.matcher.ARootResource;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceScanner;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jpattern.Dependency;

import com.google.common.base.Strings;

class PojoScanner {
    
    private final Logger log = LogManager.getLogger(PojoScanner.class);
    private final JMutateContext ctxt;
    private ARoot scanRootMatcher;
    private Matcher<String> scanPackageMatcher;
    private Matcher<String> scanForClassNameMatcher;
    
    public PojoScanner(JMutateContext ctxt,JType node, GenerateMatchers options) {
        super();
        this.ctxt = ctxt;
        log.info("for " + node.getFullName());
        log.info("  looking for pojo's in:");
        log.info("      dependencies ('pojoDependencies'): " + (options.pojoDependencies().length==0?"<any>" :toString(options.pojoDependencies())));
        log.info("      packages ('pojoPackages'): " + (options.pojoPackages().isEmpty()?"<any>":options.pojoPackages()));
        log.info("      matching ('pojoNames'): " + (options.pojoNames().isEmpty()?"<any>":options.pojoNames()));
        
        log.info("  generating to: " + ctxt.getDefaultGenerationRoot());
        
        scanRootMatcher = createRootMatcher(options);
        scanPackageMatcher = createPackageMatcher(options);
        scanForClassNameMatcher = createClassNameMatcher(options);
        

        log.info("      pojoPackageMatcher : " + scanPackageMatcher);
        log.info("      pojoNameMatcher : " + scanForClassNameMatcher);
        
        if(log.isDebugEnabled()){
            log.debug("scanning for pojos in roots:");
            for(Root root:ctxt.getResourceLoader().getAllRoots()){
                if(scanRootMatcher.matches(root)){
                    log.debug("INCLUDE " + root);
                }
            }
            //log.debug("ignored roots:");
            for(Root root:ctxt.getResourceLoader().getAllRoots()){
                if(!scanRootMatcher.matches(root)){
                	log.debug("EXCLUDE " + root);
                }
            }
        }
    }
    
    public FindResult<Class<?>> scanForReflectedClasses(){
        log.debug("scanning for compiled pojos");
        FindResult<Class<?>> results = ClassScanner.with()
                .scanRoots(ctxt.getResourceLoader().getAllRoots())
                .classLoader(ctxt.getResourceLoader().getClassLoader())
                .filter(ClassFilter.with()
                        .rootMatches(scanRootMatcher)
                        .resourceMatches(ARootResource.with()
                               .packageName(scanPackageMatcher)
                               .className(scanForClassNameMatcher))
                        .classMatches(AClass.that().isNotInterface().isNotAbstract().isNotAnonymous()))
                 .listener(newMatcheListener()
                ).build()
                .findClasses();
        
        log.debug("found: " + results.toList().size() + " compiled pojos");
        return results;
    }
    
    public FindResult<JType> scanSources() {
    	log.debug("scanning for source pojos");
    	FindResult<JType> results = SourceScanner.with()
                .scanRoots(ctxt.getResourceLoader().getAllRoots())
                .filter(SourceFilter.that()
                        .includesRoot(scanRootMatcher)
                        .includesResource(ARootResource.with()
                                .packageName(scanPackageMatcher)
                                .className(scanForClassNameMatcher))
                        .includesType(AJType.that().isNotInterface().isNotAbstract().isNotAnonymous()))
                        .listener(newMatcheListener())
                .build()
                .findTypes();
        
        log.debug("found: " + results.toList().size() + " source pojos");
        return results;
    }

	private MatchListener<Object> newMatcheListener() {
		return new BaseMatchListener<Object>() {

			@Override
			protected void onMatched(org.codemucker.jfind.RootResource resource) {
				if (log.isDebugEnabled()) {
					log.debug("FOUND resource " + resource.getRelPath());
					// log.debug("ignored:className=" + className + ", root=" +
					// resource.getRoot() + ",resource=" + resource);
				}
			};

			@Override
			protected void onIgnored(org.codemucker.jfind.RootResource resource) {
				if (log.isDebugEnabled()) {
					log.debug("IGNORE resource " + resource.getRelPath());
					// log.debug("ignored:className=" + className + ", root=" +
					// resource.getRoot() + ",resource=" + resource);
				}
			};

			@Override
			public void onMatched(ClassResource resource) {
				if (log.isDebugEnabled()) {
					log.debug("FOUND class resource " + resource);
				}
			}

			@Override
			public void onIgnored(ClassResource resource) {
				if (log.isDebugEnabled()) {
					log.debug("IGNORE class resource " + resource);
					// log.debug("ignored:className=" + className + ", root=" +
					// resource.getRoot() + ",resource=" + resource);
				}
			}

			@Override
			protected void onMatched(Class<?> record) {
				log.debug("FOUND class " + record.getName());
			}

			@Override
			protected void onIgnored(Class<?> record) {
				log.debug("IGNORE class " + record.getName());
			}
			
		};
	}

    private static String toString(Dependency[] dependencies){
        if(dependencies == null || dependencies.length==0){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(Dependency d:dependencies){
            if(sb.length()>0){
                sb.append(",");
            }
            sb.append("(").append(d.group().length()==0?"*":d.group()).append(":").append(d.artifact().length()==0?"*":d.artifact()).append(")");
        }
        return sb.toString();
    }
    
    private static Matcher<String> createClassNameMatcher(GenerateMatchers options) {
        Matcher<String> matcher = Logical.not(AString.matchingAntPattern("Abstract*"));
        String expression = options.pojoNames();
        if (!Strings.isNullOrEmpty(expression)) {
        	matcher = AString.matchingExpression(expression);
        }
        return matcher;
    }
    
    private static Matcher<String> createPackageMatcher(GenerateMatchers options) {
        Matcher<String> matcher = AString.equalToAnything();
        String expression = options.pojoPackages();
        if (!Strings.isNullOrEmpty(expression)) {
        	matcher = AString.matchingExpression(expression);
        }
        return matcher;
    }
    
    private static ARoot createRootMatcher(GenerateMatchers options) {
        ARoot scanRootMatcher = ARoot.with();
        Dependency[] limitToDeps = options.pojoDependencies();
        for (Dependency dep : limitToDeps) {
            scanRootMatcher.dependency(dep.group(), dep.artifact());
        }
        return scanRootMatcher;
    }

    
}