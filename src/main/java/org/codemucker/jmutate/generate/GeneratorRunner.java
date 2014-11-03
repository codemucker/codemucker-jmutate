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
import org.codemucker.jfind.Roots;
import org.codemucker.jfind.matcher.AResource;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.DefaultResourceLoader;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceScanner;
import org.codemucker.jmutate.ast.BaseSourceVisitor;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.DefaultGenerator;
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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * I scan and filter the source code to find build annotations. I then pass this off to the appropriate generator
 */
public class GeneratorRunner {

    private static final Matcher<String> MATCH_CONTENT = AString.matchingAntPattern("**Generate**");
    private static final Matcher<JAnnotation> GENERATION_ANNOTATIONS = AJAnnotation.with()
            .fullName(AString.matchingAntPattern("*.*Generate*"))
            .annotatedWith(AnAnnotation.with().fullName(DefaultGenerator.class));

    private final Logger log = LogManager.getLogger(GeneratorRunner.class);

    /**
     * The roots used to resolve classes and sources. The set of roots to scan
     */
    private final Iterable<Root> scanRoots;
    /**
     * Use to limit the roots scanned
     */
    private final Matcher<Root> scanRootFilter;
    /**
     * The packages to limit the scan to, or empty if all.
     */
    private final String scanPackagesAntPattern;
    
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
    
    /**
     * If true and a generator is not explicitly registered for a generation annotation, then see if the annotation contains a default generator
     */
    private final boolean autoRegisterDefaultGenerators;
    private final boolean scanSubtypes;
    private final DefaultResourceLoader resourceLoader;
    
    
    public static Builder with() {
        return new Builder();
    }

    public GeneratorRunner(Iterable<Root> roots, Iterable<Root> scanRoots, Matcher<Root> scanRootsFilter, boolean scanSubtypes,Root generationRoot, String searchPkg,Map<String, String> generators, boolean failOnParseError) {
        super();
        this.resourceLoader = DefaultResourceLoader.with()
                .parentLoader(DefaultResourceLoader.with().classLoader(Thread.currentThread().getContextClassLoader()).build())
                .roots(Roots.with()
                    .roots(roots)
                    .roots(scanRoots)
                    .root(generationRoot)
                    .build())
               .build();
        
        this.scanRoots = scanRoots;
        this.scanRootFilter = scanRootsFilter;
        this.scanPackagesAntPattern = searchPkg;
        this.scanSubtypes = scanSubtypes;
        
        this.failOnParseError = failOnParseError;
        this.generators = Maps.newLinkedHashMap(generators);
        this.generatorClassLoader = resourceLoader.getClassLoader();
        this.autoRegisterDefaultGenerators = true;
        
        ctxt = DefaultMutateContext.with()
                .defaults()
                .parser(JAstParser.with()
                        .defaults()
                        .resourceLoader(resourceLoader)
                        .build())
                .generationRoot(generationRoot)
                .build();
        
        this.annotationCompiler = ctxt.obtain(JAnnotationCompiler.class);
    }

    /**
     * Run the scan and generator and invocation
     */
    public void run() {
        MutateUtil.setClassLoader(generatorClassLoader);
        if (log.isDebugEnabled()) {

            //log.debug("filtering roots to " + scanRootMatcher);
            log.debug("filtering packages " + scanPackagesAntPattern);
            log.debug("scanning roots: ");
            for (Root root : scanRoots) {
                if(scanRootFilter.matches(root)){
                    log.debug(root);
                }
            }
            log.debug("ignored roots: ");
            for (Root root : scanRoots) {
                if(!scanRootFilter.matches(root)){
                    log.debug(root);
                }
            }
            log.debug("generators for annotation:");
            for (String key : generators.keySet()) {
                log.debug("@" + key + "-- handled by --> " + generators.get(key));
            }
        }
        List<Annotation> generateAnnotations = scanForGenerationAnnotations();
        if(generateAnnotations.size()==0){
            log.info("no generation annotations found");
            return;
        }
        //compile all the annotations at once instead of calling the compiler once for each annotation. The compiled
        //version will be cached on the ast node of each annotation
        annotationCompiler.compileAnnotations(generateAnnotations);
        //group the found annotations by the generator they invoke
        Collection<GroupedAnnotations> groups = groupByAnnotationType(generateAnnotations);
        //run the generators in order (of generator)
        for (GroupedAnnotations group : groups) {
            Generator<?> generator = getGeneratorFor(group.getAnnotationName());
            if (generator != null) {
                invokeGenerator(generator,group);
            }
        }
        MutateUtil.setClassLoader(null);
    }

    @SuppressWarnings("unchecked")
    private void invokeGenerator(Generator generator, GroupedAnnotations nodesForThisGenerator) {
        for (Annotation optionAnnotation : nodesForThisGenerator.getCollectedNodes()) {
            java.lang.annotation.Annotation compiledOptionAnnotation = annotationCompiler.toCompiledAnnotation(optionAnnotation);
            ASTNode attachedTo = getOwningNodeFor(optionAnnotation);
            JType type= findNearestParentTypeFor(attachedTo);
            log.info("processing annotation '" + compiledOptionAnnotation.getClass().getName() + "' in '" + type.getFullName());
            
            generator.generate(attachedTo, compiledOptionAnnotation);
        }
    }
  
    /**
     * FInd all the annotations which mark a request to generate some code
     * 
     * @return
     */
    private List<Annotation> scanForGenerationAnnotations() {
        final List<Annotation> found = new ArrayList<>();
        
        final AResource resourceFilter = AResource.with()
                .packageName(AString
                        .matchingAntPattern(scanPackagesAntPattern))//limit search
                        .stringContent(MATCH_CONTENT)//prevent parsing nodes we don't need to
                        ;
        if(log.isInfoEnabled()){
             log.info("match resource " + resourceFilter);
            
        }
        //find all the code with generation annotations
        SourceFilter.Builder sourceFilter = SourceFilter.with()
            .includesRoot(scanRootFilter)
             .includesResource(resourceFilter)
             .includesType(AJType.with().annotation(GENERATION_ANNOTATIONS));
        
        if(!scanSubtypes){
            sourceFilter.excludesType(AJType.that().isInnerClass());
        }
                     
        SourceScanner scanner = ctxt.obtain(SourceScanner.Builder.class)
                .failOnParseError(failOnParseError)
                .parser(ctxt.getParser())
                .scanRoots(scanRoots)
                .filter(sourceFilter)
                .build();
        
        scanner.visit(new BaseSourceVisitor() {
                @Override
                public boolean visit(Root node) {
                    //log.debug("visit root:" + node.getPathName());
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
                    } else {
                        //log("skipped annotation:" + a.getQualifiedName());
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
        if (generatorClassName == null && autoRegisterDefaultGenerators) {
            autoRegisterGeneratorFor(annotationType);
            generatorClassName = generators.get(annotationType);
        }
        if(generatorClassName==null){
            StringBuilder msg = new StringBuilder("No code generator currently registered for annotation '" + annotationType + "', have:[");
            //print out what we do have registered
            for (String key : generators.keySet()) {
                msg.append("\n").append(generators.get(key)).append(" for annotation ").append(key);
            }
            msg.append("]");
            msg.append("\n auto register generators is " + autoRegisterDefaultGenerators);
            
            log(msg.toString());
            if (throwErrorOnMissingGenerators) {
                throw new JMutateException(msg.toString());
            }
        }
        
        Class<?> generatorClass;
        try {
            generatorClass = generatorClassLoader.loadClass(NameUtil.sourceNameToCompiledName(generatorClassName));
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

    private void autoRegisterGeneratorFor(String annotationType) {
        try {
            Class<?> annotation = ctxt.getResourceLoader().loadClass(NameUtil.sourceNameToCompiledName(annotationType));
            DefaultGenerator defGenAnon = annotation.getAnnotation(DefaultGenerator.class);
            if(defGenAnon!=null){
                log.info("auto registering generator '" + defGenAnon.value() + "' for annotation '" + annotationType + "'");
                generators.put(annotationType, defGenAnon.value());
            } else {
                log.warn("skipping auto registering generator for annotation '" + annotationType + "' as no default supplied");    
            }
        } catch (ClassNotFoundException e) {
            log.warn(String.format("couldn't load annotation class %s, using roots:%n%s",annotationType,Joiner.on("\n").join(ctxt.getResourceLoader().getAllRoots())), e);
        }
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

    private static JType findNearestParentTypeFor(ASTNode node) {
        while (node != null) {
            if (JType.is(node)) {
                return JType.from(node);
            }
            node = node.getParent();
        }
        return null;
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

        @Optional
        private Iterable<Root> roots;
        
        @Optional
        private Iterable<Root> scanRoots;
        
        @Optional
        private Matcher<Root> scanRootsFilter;
        
        @Optional
        private String scanPackages;
        
        @Required
        private Root defaultGenerateTo;
        
        @Optional
        private boolean failOnParseError;
        private boolean scanSubTypes = true;
        
        private final Map<String, String> generators = new HashMap<>();
        
        @Override
        public GeneratorRunner build() {
            Preconditions.checkNotNull(defaultGenerateTo, "expect a default output (generation) root to be set");
            
            Iterable<Root> roots = getRootsOrDefault();
            Iterable<Root> scanRoots = getScanRootsOrDefault();
            Matcher<Root> scanRootsFilter = this.scanRootsFilter != null ? this.scanRootsFilter : ARoot.that().isDirectory().isSrc().isNotType(RootType.GENERATED);//.isNotType(RootType.GENERATED);
            String scanPkg = this.scanPackages == null ? "" : this.scanPackages;
            
            return new GeneratorRunner(roots,scanRoots, scanRootsFilter, scanSubTypes, defaultGenerateTo, scanPkg, generators, failOnParseError);
        }
        
        private Iterable<Root> getRootsOrDefault(){
            if(roots!= null){
                return roots;
            }
            return Roots.with().all().build();
        }
        
        private Iterable<Root> getScanRootsOrDefault(){
            if(scanRoots!= null){
                return scanRoots;
            }
            return Roots.with().srcDirsOnly().build();
        }

        @Optional
        public Builder defaults(){
            return this;
        }

        @Optional
        public Builder roots(IBuilder<? extends Iterable<Root>> builder) {
            roots(builder.build());
            return this;
        }
        
        @Optional
        public Builder roots(Iterable<Root> roots) {
            this.roots = roots;
            return this;
        }

        @Optional
        public Builder scanRoots(IBuilder<? extends Iterable<Root>> builder) {
            scanRoots(builder.build());
            return this;
        }
        
        @Optional
        public Builder scanRoots(Iterable<Root> roots) {
            this.scanRoots = roots;
            return this;
        }

        @Optional
        public Builder scanRootMatching(Matcher<Root> matcher) {
            this.scanRootsFilter = matcher;
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
        @SuppressWarnings("rawtypes")
        public Builder registerGenerator(Class<? extends java.lang.annotation.Annotation> forAnnotation, Class<? extends Generator> generator) {
            registerGenerator(NameUtil.compiledNameToSourceName(forAnnotation), generator);
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

        public Builder scanSubTypes() {
            this.scanSubTypes = true;
            return this;
        }
    }
}
