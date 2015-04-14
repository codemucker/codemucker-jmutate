package org.codemucker.jmutate.generate;

import static org.codemucker.jmatch.Logical.all;
import static org.codemucker.jmatch.Logical.not;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jfind.matcher.ARootResource;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.DefaultResourceLoader;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ProjectOptions;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceScanner;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.DontGenerate;
import org.codemucker.jpattern.generate.IsGenerated;
import org.codemucker.jpattern.generate.IsGeneratorConfig;
import org.codemucker.jpattern.generate.IsGeneratorTemplate;
import org.codemucker.lang.IBuilder;
import org.codemucker.lang.annotation.Optional;
import org.codemucker.lang.annotation.Required;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * I scan and filter the source code to find build annotations. I then pass this off to the appropriate generator
 */
public class GeneratorRunner {

    private static final Matcher<String> CONTENT_GENERATOR_PRESENT = AString.matchingExpression("**Generate**||**Enforce**");
    static final Matcher<JAnnotation> SOURCE_GENERATION_PRESENT_ANNOTATION = AJAnnotation.with().fullName(AString.matchingExpression("(**.*Generate*||**.*Enforce*) && !(" + IsGenerated.class.getName() + "||" + Generated.class.getName() + ")" ));
    static final Matcher<java.lang.annotation.Annotation> COMPILED_GENERATION_PRESENT_ANNOTATION = AnAnnotation.with().fullName(AString.matchingExpression("(**.*Generate*||**.*Enforce*) && !(" + IsGenerated.class.getName() + "||" + Generated.class.getName() + ")"));

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
	private final Matcher<String> filterGenerators;
	private final Matcher<JAnnotation> annotationFilter;
	
	private final ConfigExtractor configExtractor;
	
    public static Builder with() {
        return new Builder();
    }

    public GeneratorRunner(Iterable<Root> roots, Iterable<Root> scanRoots, Matcher<Root> scanRootsFilter, boolean scanSubtypes,Root generationRoot, String searchPkg,
    		Map<String, String> generators, boolean failOnParseError,ClashStrategy defaultClashStrategy, Matcher<String> filterAnnotations, Matcher<String> filterGenerators,
    		ProjectOptions options) {
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

        this.filterGenerators = filterGenerators;
        this.annotationFilter = all(
        		SOURCE_GENERATION_PRESENT_ANNOTATION,
        		AJAnnotation.with().fullName(filterAnnotations));
        
        
        this.ctxt = DefaultMutateContext.with()
                .defaults()
                .projectOptions(options)
                .parser(JAstParser.with()
                        .defaults()
                        .resourceLoader(resourceLoader)
                        .build())
                .generationRoot(generationRoot)
                .clashStrategy(defaultClashStrategy)
                .build();
        
        this.configExtractor = ctxt.obtain(ConfigExtractor.class);
        
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
        List<Annotation> annotations = scanForGenerationAnnotations();
        if(annotations.size()==0){
            log.info("no generation annotations found");
            return;
        }

        //group the found annotations by the generator they invoke
        Set<ASTNode> nodes = configExtractor.processAnnotations(annotations);
        //run the generators in order (of generator)
        for (ASTNode node: nodes) {
        	
        	SmartConfig sc = SmartConfig.get(node);
        	
        	for(String annotationType:sc.getAnnotations()){
        		CodeGenerator<?> generator = getGeneratorFor(annotationType);
        		if (generator != null) {
        			
        			log.debug("processing annotation '" + annotationType + "' on '" + node);
    	            
                	invokeGenerator(generator,sc, node);        
                }    	
        	}
        	
            
        }
        MutateUtil.setClassLoader(null);
    }

    private void invokeGenerator(CodeGenerator<?> generator, SmartConfig config, ASTNode node) {
    	log.debug("invoking generator " + generator.getClass().getName());
    	try {
	    	generator.beforeRun();
        	try {
        		generator.generate(node, config);
            } catch(Exception e){
            	throw new JMutateException("error processing node : " + node,e);
            }
    	} catch(Exception e){
         	throw new JMutateException("error invoking generator " + generator.getClass().getName(),e);
    	} finally {
    		generator.afterRun();
    	}
    }
  
    /**
     * FInd all the annotations which mark a request to generate some code
     * 
     * @return
     */
    private List<Annotation> scanForGenerationAnnotations() {
        final ARootResource resourceFilter = ARootResource.with()
                .packageName(AString
                        .matchingAntPattern(scanPackagesAntPattern))//limit search
                        .stringContent(CONTENT_GENERATOR_PRESENT)//prevent parsing nodes we don't need to
                        ;
        if(log.isDebugEnabled()){
             log.debug("match resource " + resourceFilter);
        }
        Matcher<JType> typeMatcher= all(
        		AJType.with().annotation(annotationFilter).expression(scanSubtypes?null:"notInnerClass"),
        		not(AJType.with().annotation(AJAnnotation.with().fullName(IsGeneratorTemplate.class))),
        		not(AJType.with().annotation(AJAnnotation.with().fullName(DontGenerate.class))));
				
        //find all the code with generation annotations
        SourceScanner scanner = ctxt.obtain(SourceScanner.Builder.class)
                .failOnParseError(failOnParseError)
                .parser(ctxt.getParser())
                .scanRoots(scanRoots)
                .filter(SourceFilter.with()
                        .rootMatches(scanRootFilter)
                        .resourceMatches(resourceFilter)
                        .typeMatches(typeMatcher))
                .build();

        //for now just handle generators on types. SHould look to do the same for methods
        FindResult<JType> found = scanner.findTypes();
        
        List<Annotation> ret = new ArrayList<>();
        for(JType t:found){
        	List<JAnnotation> as = t.getAnnotations().getAllDirect();
        	for(JAnnotation a:as){
        		if(annotationFilter.matches(a)){
        			ret.add(a.getAstNode());
        		}
        	}
        }
        log("found " + ret.size() + " code generation annotations");
        return ret;
    }

    private CodeGenerator<?> getGeneratorFor(String annotationType){
        String generatorClassName = generators.get(annotationType);
        if (generatorClassName == null && autoRegisterDefaultGenerators) {
            autoRegisterGeneratorFor(annotationType);
            generatorClassName = generators.get(annotationType);
        }
        
        if(!filterGenerators.matches(generatorClassName)){
    		log.debug("generator '" + generatorClassName + "' is marked to be ignored, skipping");
    		return null;
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
        if (!CodeGenerator.class.isAssignableFrom(generatorClass)) {
            throw new JMutateException("Registered generator class %s for annotation %s does not implement %s", generatorClassName, annotationType,
                    CodeGenerator.class.getName());
        }
        //ensure we inject all the generator dependencies
        CodeGenerator<?> gen = (CodeGenerator<?>) ctxt.obtain(generatorClass);

        return gen;
    }
	
    private void autoRegisterGeneratorFor(String annotationType) {
        try {
            Class<?> annotation = ctxt.getResourceLoader().loadClass(NameUtil.sourceNameToCompiledName(annotationType));
            IsGeneratorConfig generatorConfig = annotation.getAnnotation(IsGeneratorConfig.class);
            if(generatorConfig!=null){
                log.info("auto registering generator '" + generatorConfig.defaultGenerator() + "' for annotation '" + annotationType + "'");
                generators.put(annotationType, generatorConfig.defaultGenerator());
            } else {
                log.warn("skipping auto registering generator for annotation '" + annotationType + "' as no default generator supplied");    
            }
        } catch (ClassNotFoundException e) {
            log.warn(String.format("couldn't load annotation class %s, using roots:%n%s",annotationType,Joiner.on("\n").join(ctxt.getResourceLoader().getAllRoots())), e);
        }
    }
 
    private void log(String msg) {
        log.debug(msg);
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
        
        @Optional
        private boolean scanSubTypes = true;
        
        @Optional
        private ClashStrategy defaultClashStrategy = ClashStrategy.SKIP;
        
        @Optional
        private ProjectOptions projectOptions;
        
		private final Map<String, String> generators = new HashMap<>();

		private Matcher<String> annotationNameMatcher;

		private Matcher<String> generatorNameMatcher;
        
        @Override
        public GeneratorRunner build() {
            Preconditions.checkNotNull(defaultGenerateTo, "expect a default output (generation) root to be set");
            
            Iterable<Root> roots = getRootsOrDefault();
            Iterable<Root> scanRoots = getScanRootsOrDefault();
            Matcher<Root> scanRootsFilter = this.scanRootsFilter != null ? this.scanRootsFilter : ARoot.that().isDirectory().isSrc().isNotType(RootType.GENERATED);//.isNotType(RootType.GENERATED);
            String scanPkg = this.scanPackages == null ? "" : this.scanPackages;
            Matcher<String> annotationsMatcher = this.annotationNameMatcher != null ? this.annotationNameMatcher : AString.equalToAnything();
            Matcher<String> generatorMatcher = this.generatorNameMatcher != null ? this.generatorNameMatcher : AString.equalToAnything();
            
            return new GeneratorRunner(roots,scanRoots, scanRootsFilter, scanSubTypes, defaultGenerateTo, scanPkg, generators, failOnParseError,defaultClashStrategy, annotationsMatcher, generatorMatcher, projectOptions);
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
        public Builder registerGenerator(Class<? extends java.lang.annotation.Annotation> forAnnotation, Class<? extends CodeGenerator> generator) {
            registerGenerator(NameUtil.compiledNameToSourceName(forAnnotation), generator);
            return this;
        }
        
        @Optional
        @SuppressWarnings("rawtypes")
        public Builder registerGenerator(String forAnnotationClass,Class<? extends CodeGenerator> generator){
            generators.put(forAnnotationClass, generator.getName());
            return this;
        }
        
        @Optional
        public Builder registerGenerator(String forAnnotation,String generatorClass){
            generators.put(forAnnotation, generatorClass);
            return this;
        }

        @Optional
        public Builder failOnParseError(boolean failOnError) {
            this.failOnParseError = failOnError;
            return this;
        }

        @Optional
        public Builder scanSubTypes() {
            this.scanSubTypes = true;
            return this;
        }
        
        @Optional
        public Builder defaultClashStrategy(ClashStrategy defaultClashStrategy) {
			this.defaultClashStrategy = defaultClashStrategy;
			return this;
        }

        /**
         * A a filter to only processes annotations with full names matching the given matcher
         * @param matcher
         * @return
         */
        @Optional
		public Builder matchAnnotations(Matcher<String> matcher) {
			this.annotationNameMatcher = matcher;
			return this;
		}

        @Optional
		public Builder matchGenerator(Class<?> generatorClass) {
        	matchGenerator(AString.equalTo(NameUtil.compiledNameToSourceName(generatorClass)));
        	return this;
		}
        
        /**
         * A a filter to only run generators with full names matching the given matcher
         * @param matcher
         * @return
         */
        @Optional
		public Builder matchGenerator(Matcher<String> matcher) {
        	this.generatorNameMatcher = matcher;
        	return this;
		}

        @Optional
		public Builder projectOptions(ProjectOptions projectOptions) {
			this.projectOptions = projectOptions;
			return this;
        }

    }
}
