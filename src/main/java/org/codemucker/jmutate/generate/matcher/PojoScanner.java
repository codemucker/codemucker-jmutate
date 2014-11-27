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
import org.codemucker.jfind.Root;
import org.codemucker.jfind.matcher.AClass;
import org.codemucker.jfind.matcher.AResource;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceScanner;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jpattern.Dependency;

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
        log.info("      packages ('pojoPackages'): " + (options.pojoPackages().length==0?"<any>":Arrays.toString(options.pojoPackages())));
        log.info("      matching ('pojoBeanNames'): " + (options.pojoNames().length==0?"<any>":Arrays.toString(options.pojoNames())));
        log.info("  generating to: " + ctxt.getDefaultGenerationRoot());
        
        scanRootMatcher = createRootMatcher(options);
        scanPackageMatcher = createPackageMatcher(options);
        scanForClassNameMatcher = createClassNameMatcher(options);
        if(log.isDebugEnabled()){
            log.debug("scanning for pojos in roots:");
            for(Root root:ctxt.getResourceLoader().getAllRoots()){
                if(scanRootMatcher.matches(root)){
                    log.debug(root);
                }
            }
            log.debug("ignored roots:");
            for(Root root:ctxt.getResourceLoader().getAllRoots()){
                if(!scanRootMatcher.matches(root)){
                    log.debug(root);
                }
            }
        }
    }
    
    public FindResult<Class<?>> scanForReflectedClasses(){
        
        FindResult<Class<?>> results = ClassScanner.with()
                .scanRoots(ctxt.getResourceLoader().getAllRoots())
                .filter(ClassFilter.with()
                        .rootMatches(scanRootMatcher)
                        .resourceMatches(AResource.with()
                                .packageName(scanPackageMatcher)
                                .className(scanForClassNameMatcher))
                        .classMatches(AClass.that().isNotInterface().isNotAbstract().isNotAnonymous()))
                 .listener(new BaseMatchListener<Object>(){
                     
                     @Override
                        public void onMatched(ClassResource resource) {
                             if(log.isDebugEnabled()){
                                     //       log.debug("matched:className=" + className +   ", root=" + resource.getRoot() + ",resource=" + resource);
                             }         
                        }
                     
                     @Override
                    public void onIgnored(ClassResource resource) {
                        if(log.isDebugEnabled()){
                               // log.debug("ignored:className=" + className +   ", root=" + resource.getRoot() + ",resource=" + resource);
                         }      
                    }}
                ).build()
                .findClasses();
        
        log.debug("found:" + results.toList().size());
        return results;
    }
    
    public FindResult<JType> scanSources() {
        FindResult<JType> results = SourceScanner.with()
                .scanRoots(ctxt.getResourceLoader().getAllRoots())
                .filter(SourceFilter.that()
                        .includesRoot(scanRootMatcher)
                        .includesResource(AResource.with()
                                .packageName(scanPackageMatcher)
                                .className(scanForClassNameMatcher))
                        .includesType(AJType.that().isNotInterface().isNotAbstract().isNotAnonymous()))
                        .listener(new BaseMatchListener<Object>() {})
                .build()
                .findTypes();
        
        log.debug("found:" + results.toList().size());
        return results;
    }

    private String toString(Dependency[] dependencies){
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
    
    private Matcher<String> createClassNameMatcher(GenerateMatchers options) {
        Matcher<String> scanForClassNameMatcher = Logical.not(AString.matchingAntPattern("Abstract*"));
        String[] beanNames = options.pojoNames();
        if (beanNames.length > 0) {
            List<Matcher<String>> ors = new ArrayList<>();
            for (String beanName : beanNames) {
                ors.add(AString.matchingAntPattern(beanName));
            }
            scanForClassNameMatcher = Logical.any(ors);
        }
        return scanForClassNameMatcher;
    }
    
    private Matcher<String> createPackageMatcher(GenerateMatchers options) {
        Matcher<String> scanPackageMatcher = AString.equalToAnything();
        String[] beanPackages = options.pojoPackages();
        if(beanPackages.length > 0){
            List<Matcher<String>> ors = new ArrayList<>();
            for (String pkg: beanPackages) {
                ors.add(AString.matchingAntPattern(pkg));
            }
            scanPackageMatcher = Logical.any(ors); 
        }
        return scanPackageMatcher;
    }
    
    private ARoot createRootMatcher(GenerateMatchers options) {
        ARoot scanRootMatcher = ARoot.with();
        Dependency[] limitToDeps = options.pojoDependencies();
        for (Dependency dep : limitToDeps) {
            scanRootMatcher.dependency(dep.group(), dep.artifact());
        }
        return scanRootMatcher;
    }

    
}