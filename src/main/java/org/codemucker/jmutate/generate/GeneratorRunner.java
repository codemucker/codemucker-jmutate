package org.codemucker.jmutate.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.matcher.AResource;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.JMutateFilter;
import org.codemucker.jmutate.JMutateScanner;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JCompilationUnit;
import org.codemucker.jmutate.ast.JFindVisitor;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.codemucker.jpattern.IsGenerated;
import org.codemucker.jpattern.cqrs.GenerateCqrsRestServiceClient;
import org.codemucker.lang.IBuilder;
import org.codemucker.lang.annotation.Optional;
import org.codemucker.lang.annotation.Required;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * I scan and filter the source code to find build annotations. I then pass this off to the appropriate generator
 */
public class GeneratorRunner {

    private static final Matcher<String> MATCH_CONTENT = AString.matchingAntPattern("**" + IsGenerated.class.getPackage().getName() + "**");
    private static final Matcher<JAnnotation> GENERATION_ANNOTATIONS = AJAnnotation.with().fullName(AString.matchingAntPattern(IsGenerated.class.getPackage().getName() + ".*Generate*"));

    private final Logger log = LogManager.getLogger(GeneratorRunner.class);

    /**
     * The roots to scan
     */
    private final Iterable<Root> scanRoots;
    /**
     * Use to limit the roots scanned
     */
    private final Matcher<Root> scanRootMatcher;
    /**
     * The packages to limit the scan to, or empty if all.
     */
    private final String scanPackagesAntPattern;
    /**
     * The ast parser to use for loading the source code
     */
    private final JAstParser parser;
    
    //TODO:support generator precedence. What if generators expose the annotations they add to code so we can detect which generator has to run first?
    /**
     * Generators registered by the annotation they process
     */
    private final Map<String, String> generators;
    
    private final boolean throwErrorOnMissingGenerators = true;
    /**
     * Used to compile the generator annotations found so as to be able to easily extract the info. These annotations are effectively the configuration of each generator
     */
    private final JAnnotationCompiler annotationCompiler;
    /**
     * Class loader used to load the generators with
     */
    private final ClassLoader generatorClassLoader;
    
    /**
     * Whether to fail generation when we can't parse a source file during a scan, or simply to skip it. If true throw an error on parsing errors, else just log it
     */
    private final boolean failOnParseError; 

    private final DefaultMutateContext ctxt;
    
    public static Builder with() {
        return new Builder();
    }

    public GeneratorRunner(Iterable<Root> sourceRoots, Matcher<Root> rootMatcher, Root generationRoot, String searchPkg, JAstParser parser,ClassLoader classLoader,Map<String, String> generators, boolean failOnParseError) {
        super();
        this.scanRoots = sourceRoots;
        this.scanRootMatcher = rootMatcher;
        this.scanPackagesAntPattern = searchPkg;
        this.parser = parser;
        this.generators = ImmutableMap.copyOf(generators);
        this.generatorClassLoader = classLoader;
        this.failOnParseError = failOnParseError;
        
        ctxt = DefaultMutateContext.with()
                .defaults()
                .parser(parser)//if not set, default used
                .root(generationRoot)//if not set, one made up
                .build();
        
        this.annotationCompiler = ctxt.obtain(JAnnotationCompiler.class);
    }

    /**
     * Run the scan and generator and invocation
     */
    public void run() {
        if (log.isDebugEnabled()) {

            //log.debug("filtering roots to " + scanRootMatcher);
            log.debug("filtering packages " + scanPackagesAntPattern);
            log.debug("scanning roots: ");
            for (Root root : scanRoots) {
                if(scanRootMatcher.matches(root)){
                    log.debug(root);
                }
            }
            log.debug("ignored roots: ");
            for (Root root : scanRoots) {
                if(!scanRootMatcher.matches(root)){
                    log.debug(root);
                }
            }
            log.debug("generators for annotation:");
            for (String key : generators.keySet()) {
                log.debug("@" + key + "-- handled by --> " + generators.get(key));
            }
        }
        List<Annotation> generateAnnotations = findGenerationAnnotations();
        //compile all the annotations at once instead of calling the compiler once for each annotation. The compiledgi
        //version will be cached on the ast node of each annotation
        annotationCompiler.compileAnnotations(generateAnnotations);
        Collection<GroupedAnnotations> groups = groupByAnnotationType(generateAnnotations);
        //run the generators in order
        for (GroupedAnnotations group : groups) {
            Generator<?> generator = getGeneratorFor(group.getAnnotationName());
            if (generator != null) {
                invokeGenerator(generator,group);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void invokeGenerator(Generator generator, GroupedAnnotations nodesForThisGenerator) {
        for (Annotation optionAnnotation : nodesForThisGenerator.getCollectedNodes()) {
            java.lang.annotation.Annotation compiledOptionAnnotation = annotationCompiler.toCompiledAnnotation(optionAnnotation);
            ASTNode attachedTo = getOwningNodeFor(optionAnnotation);
            JType type= findClosetTypeFor(attachedTo);
            log.info("processing annotation '" + compiledOptionAnnotation.getClass().getName() + "' in '" + type.getFullName());
            
            generator.generate(attachedTo, compiledOptionAnnotation);
        }
    }

    private static JType findClosetTypeFor(ASTNode node) {
        while (node != null) {
            if (JType.is(node)) {
                return JType.from(node);
            }
            node = node.getParent();
        }
        return null;
    }
    
    /**
     * FInd all the annotations which mark a request to generate some code
     * 
     * @return
     */
    private List<Annotation> findGenerationAnnotations() {
        final List<Annotation> found = new ArrayList<>();
        
        final AResource resourceMatcher = AResource.with()
                .packageName(AString
                        .matchingAntPattern(scanPackagesAntPattern))//limit search
                   //     .stringContent(MATCH_CONTENT)//prevent parsing nodes we don't need to
                        ;
        if(log.isInfoEnabled()){
             log.info("match resource " + resourceMatcher);
            
        }
        //find all the code with generation annotations
        JMutateScanner.with()
                    .parser(parser)
                    .failOnParseError(failOnParseError)
                    .scanRoots(scanRoots)
                    .filter(JMutateFilter.with()
                        //    .includeRoot(rootMatcher)
                            .includeResource(resourceMatcher)
                            .includeType(AJType.with().annotation(GENERATION_ANNOTATIONS)))
                    .build()
                    .visit(new JFindVisitor() {
                            @Override
                            public boolean visit(Root node) {
                                log.debug("visit root:" + node.getPathName());
                                return true;
                            }
            
                            @Override
                            public boolean visit(SingleMemberAnnotation node) {
                                return found(node);
                            }
        
                            @Override
                            public boolean visit(MarkerAnnotation node) {
                                return found(node);
                            }
        
                            @Override
                            public boolean visit(NormalAnnotation node) {
                                return found(node);
                            }
        
                            private boolean found(Annotation node) {
                                JAnnotation a = JAnnotation.from(node);
                                // TODO:check instead if annotation is marked with it's
                                // own 'generator' annotation?
                              
                                if (GENERATION_ANNOTATIONS.matches(a)) {
                                    log("found generation annotation:" + a.getQualifiedName());
                                    found.add(node);
                                }
                                return false;
                            } 
                        });
        log("found " + found.size() +" code generation annotations");
        return found;
    }
    
    /**
     * Given a list of annotations, group them by type (fullname). This is so we can run a generator once for a given annotation type
     * 
     * @param generatorAnnotations
     * @return
     */
    private Collection<GroupedAnnotations> groupByAnnotationType(List<Annotation> generatorAnnotations) {
        //collect all the annotations of a given type to be passed to a generator at once (and so we can apply generator ordering)        
        Map<String, GroupedAnnotations> nodesByAnnotationName = new HashMap<>();
       
         for(Annotation option:generatorAnnotations){
             //find the appropriate builder
             //TODO:collect all the types per builder, and send once, using preferred builder order?
             String fullName = JAnnotation.from(option).getQualifiedName();
             
             GroupedAnnotations group = nodesByAnnotationName.get(fullName);
             if(group == null){
                 group = new GroupedAnnotations(fullName);
                 nodesByAnnotationName.put(fullName, group);
             }
             group.addNode(option);
         }
        return nodesByAnnotationName.values();
    }
    
    private Generator<?> getGeneratorFor(String annotationType){
        String generatorClassName = generators.get(annotationType);
        if (generatorClassName == null) {
            StringBuilder msg = new StringBuilder("No code generator registered for annotation '" + annotationType + "', have:[");
            for (String key : generators.keySet()) {
                msg.append("\n").append(generators.get(key)).append(" for annotation ").append(key);
            }
            msg.append("]");
            log(msg.toString());
            if (throwErrorOnMissingGenerators) {
                throw new JMutateException(msg.toString());
            }
        }
        
        Class<?> generatorClass;
        try {
            generatorClass = generatorClassLoader.loadClass(generatorClassName);
        } catch (ClassNotFoundException e) {
            throw new JMutateException("Registered generator class %s for annotation %s does not exist", generatorClassName, annotationType);
        }
        if (!Generator.class.isAssignableFrom(generatorClass)) {
            throw new JMutateException("Registered generator class %s for annotation %s does not implement %s", generatorClassName, annotationType,
                    Generator.class.getName());
        }
        Generator<?> gen = (Generator<?>) ctxt.obtain(generatorClass);

        return gen;
    }
    
    private ASTNode getOwningNodeFor(Annotation annotation) {
        ASTNode parent = annotation.getParent();
        while (parent != null) {
            if (parent instanceof FieldDeclaration || parent instanceof MethodDeclaration || parent instanceof TypeDeclaration
                    || parent instanceof AnnotationTypeDeclaration || parent instanceof SingleVariableDeclaration) {
                return parent;
            }
            parent = parent.getParent();
        }
        throw new JMutateException("Currently can't figure out correct parent for annotation:" + annotation);
    }

    private void log(String msg) {
        log.debug(msg);
    }
    
    private static class GroupedAnnotations {
        private final String annotationName;
        private final Set<Annotation> collectedNodes = new HashSet<>();
        
        public GroupedAnnotations(String annotationName){
            this.annotationName = annotationName;
        }
        
        public void addNode(Annotation type){
            collectedNodes.add(type);
        }

        public Set<Annotation> getCollectedNodes() {
            return collectedNodes;
        }

        public String getAnnotationName() {
            return annotationName;
        }
    }
    
    public static class Builder implements IBuilder<GeneratorRunner> {

        @Required
        private Iterable<Root> rootsToScan;
        @Required
        private Root defaultGenerateTo;
        @Optional
        private Matcher<Root> rootsMatcher;
        @Optional
        private String scanPackages;
        @Optional
        private JAstParser parser;
        @Optional
        private ClassLoader classLoader;
        @Optional
        private boolean failOnParseError;
        
        private final Map<String, String> generators = new HashMap<>();
        
        @Override
        public GeneratorRunner build() {
            Preconditions.checkNotNull(rootsToScan, "expect the sources to be scanned to be set");
            Preconditions.checkNotNull(defaultGenerateTo, "expect a default output (generation) root to be set");

            ClassLoader loader = this.classLoader != null ? this.classLoader : Thread.currentThread().getContextClassLoader();
            String pkg = scanPackages == null ? "" : scanPackages;
            Matcher<Root> rootMatcher = this.rootsMatcher != null ? this.rootsMatcher : ARoot.that().isDirectory().containsSrc().isNotType(RootType.GENERATED);
            return new GeneratorRunner(rootsToScan, rootMatcher, defaultGenerateTo, pkg, parser, loader, generators, failOnParseError);
        }

        public Builder defaults(){
            registerGenerator(GenerateCqrsRestServiceClient.class, GeneratorCqrsRestServiceClient.class);
            return this;
        }

        @Required
        public Builder sources(Iterable<Root> roots) {
            this.rootsToScan = roots;
            return this;
        }

        @Optional
        public Builder scanRootMatching(Matcher<Root> matcher) {
            this.rootsMatcher = matcher;
            return this;
        }

        @Required
        public Builder defaultGenerateTo(File dir) {
            defaultGenerateTo(new DirectoryRoot(dir,RootType.GENERATED,RootContentType.SRC));
            return this;
        }
        
        @Required
        public Builder defaultGenerateTo(Root generateTo) {
            this.defaultGenerateTo = generateTo;
            return this;
        }

        @Optional
        public Builder scanPackages(String searchPkg) {
            this.scanPackages = searchPkg;
            return this;
        }

        @Optional
        public Builder parser(JAstParser parser) {
            this.parser = parser;
            return this;
        }

        @Optional
        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        @Optional
        @SuppressWarnings("rawtypes")
        public Builder registerGenerator(Class<? extends java.lang.annotation.Annotation> forAnnotation, Class<? extends Generator> generator) {
            registerGenerator(JavaNameUtil.compiledNameToSourceName(forAnnotation), generator);
            return this;
        }
        
        @Optional
        @SuppressWarnings("rawtypes")
        public Builder registerGenerator(String forAnnotationClass,Class<? extends Generator> generator){
            generators.put(forAnnotationClass, generator.getName());
            return this;
        }
        
        @Optional
        @SuppressWarnings("rawtypes")
        public Builder registerGenerator(String forAnnotation,String generatorClass){
            generators.put(forAnnotation, generatorClass);
            return this;
        }

        @Optional
        public Builder failOnParseError(boolean failOnError) {
            this.failOnParseError = failOnError;
            return this;
        }
    }
}
