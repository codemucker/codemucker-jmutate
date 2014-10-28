package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.BaseIgnoredCallback;
import org.codemucker.jfind.BaseMatchedCallback;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.JFind;
import org.codemucker.jfind.JFindAnnotation;
import org.codemucker.jfind.JFindClass;
import org.codemucker.jfind.JFindField;
import org.codemucker.jfind.JFindFilter;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.matcher.AClass;
import org.codemucker.jfind.matcher.AField;
import org.codemucker.jfind.matcher.AMethod;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JSourceFileMutator;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.JTypeMutator;
import org.codemucker.jmutate.util.BeanNameUtils;
import org.codemucker.jpattern.IsGenerated;
import org.codemucker.jpattern.cqrs.GenerateCqrsRestServiceClient;
import org.codemucker.jpattern.cqrs.GenerateCqrsRestServiceClient.Dependency;

import com.google.inject.Inject;

public class GeneratorCqrsRestServiceClient extends AbstractGenerator<GenerateCqrsRestServiceClient> {

    private final Logger log = LogManager.getLogger(GeneratorCqrsRestServiceClient.class);
    
    private final Matcher<String> cmdPatternMatcher = AString.matchingAnyAntPattern("**Cmd|**Comand");
    private final Matcher<Annotation> queryBean = AnAnnotation.with().fullName(AString.matchingAntPattern("*.QueryBean"));
    private final Matcher<Annotation> ignoreAnon = AnAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    private final Matcher<Annotation> queryParamAnon = AnAnnotation.with().fullName(AString.matchingAntPattern("*.QueryParam"));
    private final Matcher<Annotation> headerParamAnon = AnAnnotation.with().fullName(AString.matchingAntPattern("*.HeaderParam"));
    private final Matcher<Annotation> cmdParamAnon = AnAnnotation.with().fullName(AString.matchingAntPattern("*.BodyParam"));

    private final JMutateContext ctxt;
    
    @Inject
    public GeneratorCqrsRestServiceClient(JMutateContext ctxt){
        this.ctxt = ctxt;

    }
    @Override
    public void generate(JType node,GenerateCqrsRestServiceClient options) {
   
        log.info("for " + node.getFullName());
        log.info("  looking for CQRS request beans in:");
        log.info("      dependencies ('requestBeanDependencies'): " + (options.requestBeanDependencies().length==0?"<any>" :toString(options.requestBeanDependencies())));
        log.info("      packages ('requestBeanPackages'): " + (options.requestBeanPackages().length==0?"<any>":Arrays.toString(options.requestBeanPackages())));
        log.info("      matching ('requestBeanNames'): " + (options.requestBeanNames().length==0?"<any>":Arrays.toString(options.requestBeanNames())));
        log.info("  generating to: " + ctxt.getDefaultGenerationRoot());
        
        ARoot scanRootMatcher = createRootMatcher(options);
        Matcher<String> scanPackageMatcher = createPackageMatcher(options);
        Matcher<String> scanForClassNameMatcher = createClassNameMatcher(options);

        if(log.isDebugEnabled()){
            log.debug("scanning for request beans in roots:");
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
        FindResult<Class<?>> results = JFind
                .with()
                .roots(ctxt.getResourceLoader().getAllRoots())
                .filter(JFindFilter.with()
                        .rootMatches(scanRootMatcher)
                        .packageNameMatches(scanPackageMatcher)
                        .classNameMatches(scanForClassNameMatcher)
                        .classMatches(AClass.that().isNotInterface().isNotAbstract().isNotAnonymous()))
                 .matchedCallback(new BaseMatchedCallback(){
                // @Override
                // public void onRootMatched(Root root) {
                // if(root.getPathName().contains("dareyou")){
                // System.out.println("matched:root=" + root);
                // }
                //
                // }
                //
                @Override
                public void onClassNameMatched(RootResource resource, String className) {
                    log.debug("matched:className=" + className +   ", root=" + resource.getRoot() + ",resource=" + resource);
                    }
                })
                .ignoredCallback(new BaseIgnoredCallback() {
                    @Override
                    public void onRootIgnored(Root root) {
                        if (root.getPathName().contains("dareyou")) {
                            log.debug("ignored:root=" + root);
                        }
                    }

                   @Override
                public void onClassNameIgnored(RootResource resource, String className) {
                       log.debug("ignored:className=" + className +   ", root=" + resource.getRoot() + ",resource=" + resource);
                }
                }).build()
                .findClasses();
        log("found:" + results.toList().size());
        
        generate(results);
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
    
    private Matcher<String> createClassNameMatcher(GenerateCqrsRestServiceClient options) {
        Matcher<String> scanForClassNameMatcher = AString.matchingAntPattern("*Cmd|*Query");
        String[] beanNames = options.requestBeanNames();
        if (beanNames.length > 0) {
            List<Matcher<String>> ors = new ArrayList<>();
            for (String beanName : beanNames) {
                ors.add(AString.matchingAntPattern(beanName));
            }
            scanForClassNameMatcher = Logical.any(ors);
        }
        return scanForClassNameMatcher;
    }
    
    private Matcher<String> createPackageMatcher(GenerateCqrsRestServiceClient options) {
        Matcher<String> scanPackageMatcher = AString.equalToAnything();
        String[] beanPackages = options.requestBeanPackages();
        if(beanPackages.length > 0){
            List<Matcher<String>> ors = new ArrayList<>();
            for (String pkg: beanPackages) {
                ors.add(AString.matchingAntPattern(pkg));
            }
            scanPackageMatcher = Logical.any(ors); 
        }
        return scanPackageMatcher;
    }
    
    private ARoot createRootMatcher(GenerateCqrsRestServiceClient options) {
        ARoot scanRootMatcher = ARoot.with();
        Dependency[] limitToDeps = options.requestBeanDependencies();
        for (GenerateCqrsRestServiceClient.Dependency dep : limitToDeps) {
            scanRootMatcher.dependency(dep.group(), dep.artifact());
        }
        return scanRootMatcher;
    }
    
    private void generate(Iterable<Class<?>> requestBeans){
        
        SourceTemplate cls = ctxt.newSourceTemplate();
        cls.pl("package com.myproject.resty;");
        cls.pl("");
        cls.pl("import org.codemucker.cqrs.client.gwt.AbstractClientRestService;");
        cls.pl("import org.codemucker.cqrs.client.gwt.AbstractClientRestService.Builder;");
        cls.pl("");

        cls.pl("public class GeneratedRestService extends AbstractClientRestService{}");

        JSourceFile source = cls.asSourceFileSnippet();
        JSourceFileMutator sourceMutator = source.asMutator(ctxt);

        JTypeMutator classMutator = sourceMutator.getMainTypeAsMutable();

        for (Class<?> cmdType : requestBeans) {
            generateEncodeMethod(ctxt, classMutator, cmdType);
        }

        source = source.asMutator(ctxt).writeModificationsToDisk();
        log("wrote " + source.getResource().getFullPathInfo());
    }

    private void log(String msg) {
        log.debug(msg);
    }

    private void generateEncodeMethod(JMutateContext ctxt, JTypeMutator serviceClass, Class<?> cmdType) {

        log("writing for type:" + cmdType.getName());

        SourceTemplate method = ctxt.newSourceTemplate();
        method.var("cmdType", cmdType.getName());
        method.pl("/** Generated method to build an http request */");
        method.pl("@" + javax.annotation.Generated.class.getName() + "(\"" + GeneratorRunner.class.getName() + "\")");
        method.pl("@" + IsGenerated.class.getName() + "(generator=\"" + GeneratorRunner.class.getName() + "\")");

        // method.pl( "@" + Generat.class);

        method.pl("public Builder buildRequest(${cmdType} cmd){");
        method.pl("  Builder req = newRequestBuilder();");
        boolean isCmd = cmdPatternMatcher.matches(cmdType.getName());
        if (isCmd) {
            method.pl("  req.methodPUT();");
        }

        JFindClass type = JFindClass.from(cmdType);
        Matcher<Method> validateMethodMatcher = AMethod.with().name("validate").numArgs(0).isPublic();
        if (type.hasMethodMatching(validateMethodMatcher)) {
            Method m = type.findMethodsMatching(validateMethodMatcher).getFirst();
            method.pl(" cmd." + m.getName() + "()");
        }

        FindResult<Field> fields = type.findFieldsMatching(AField.that().isNotStatic().isNotTransient().isNotNative());
        log("found " + fields.toList().size() + " fields");

        for (Field f : fields) {
            JFindField field = JFindField.from(f);
            if (field.hasAnnotation(ignoreAnon)) {
                log("ignoring field:" + f.getName());
                continue;
            }
            String getterName = BeanNameUtils.toGetterName(field.getName(), field.getType());
            String getter = getterName + "()";
            if (!type.hasMethodMatching(AMethod.with().name(getterName).numArgs(0))) {
                log("no method " + getter);
                if (!field.isPublic()) {
                    log("ignoring field as not public:" + f.getName());
                    continue;
                }
                getter = field.getName();// direct field access
            }
            String paramName;
            String setter;
            if (field.hasAnnotation(headerParamAnon)) {
                paramName = JFindAnnotation.from(field.getAnnotation(headerParamAnon)).getValueForAttribute("value", field.getName());
                setter = "setHeaderIfNotNull";
            }
            if (field.hasAnnotation(cmdParamAnon)) {
                paramName = JFindAnnotation.from(field.getAnnotation(cmdParamAnon)).getValueForAttribute("value", field.getName());
                setter = "setBodyParamIfNotNull";
            } else if (field.hasAnnotation(queryParamAnon)) {
                paramName = JFindAnnotation.from(field.getAnnotation(queryParamAnon)).getValueForAttribute("value", field.getName());
                setter = "setQueryParamIfNotNull";
            } else {
                paramName = field.getName();
                setter = "setQueryParamIfNotNull";
            }
            method.pl("req.${setter}(\"${param}\",cmd.${getter});", "param", paramName, "setter", setter, "getter", getter);

        }
        method.pl("  return req;");
        method.pl("}");

        serviceClass.addMethod(method.asMethodNodeSnippet());

    }
}