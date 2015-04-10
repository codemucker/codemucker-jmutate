package org.codemucker.jmutate.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.Roots;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jfind.matcher.ARootResource;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;

import static org.codemucker.jmatch.Logical.*;

import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.DefaultResourceLoader;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ProjectOptions;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceScanner;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.generate.GeneratorRunner.GroupedAnnotations.Group;
import org.codemucker.jmutate.generate.GeneratorRunner.GroupedAnnotations.NodeAndOptions;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.IsGeneratorConfig;
import org.codemucker.jpattern.generate.DisableGenerators;
import org.codemucker.jpattern.generate.IsGenerated;
import org.codemucker.jpattern.generate.IsGeneratorTemplate;
import org.codemucker.lang.IBuilder;
import org.codemucker.lang.annotation.Optional;
import org.codemucker.lang.annotation.Required;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
    private static final Matcher<JAnnotation> CLIENT_OPTIONS_ANNOTATIONS = AJAnnotation.with().fullName(AString.matchingExpression("*.*Generate* && !" + IsGenerated.class.getName() + " && !" + Generated.class.getName() ));
    
    private static final Matcher<java.lang.annotation.Annotation> COMPILED_GENERATION_ANNOTATION = AnAnnotation.with().fullName(AString.matchingExpression("*.*Generate* && !" + IsGenerated.class.getName() + " && !" + Generated.class.getName() )).annotationPresent(IsGeneratorConfig.class);

    
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
	
	/**
	 * Used to prevent constant scanning of already found template nodes
	 */
	private final Map<String, List<InternalGeneratorConfig>> templateOptions = new HashMap<>();
	/**
	 * Used to prevent constant scanning of already found annotations
	 */
	private final List<String> normalAnnotationsToIgnore = new ArrayList<>();
	
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
        		CLIENT_OPTIONS_ANNOTATIONS,
        		AJAnnotation.with().fullName(filterAnnotations));
        
        
        ctxt = DefaultMutateContext.with()
                .defaults()
                .projectOptions(options)
                .parser(JAstParser.with()
                        .defaults()
                        .resourceLoader(resourceLoader)
                        .build())
                .generationRoot(generationRoot)
                .clashStrategy(defaultClashStrategy)
                .build();
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
        GroupedAnnotations groups = groupByAnnotationType(annotations);
        //run the generators in order (of generator)
        for (Group group : groups.getGroups()) {
            CodeGenerator<?> generator = getGeneratorFor(group.getAnnotationType());
            if (generator != null) {
            	invokeGenerator(generator,group);        
            }
        }
        MutateUtil.setClassLoader(null);
    }

    private void invokeGenerator(CodeGenerator<?> generator, Group group) {
    	log.debug("invoking generator " + generator.getClass().getName());
    	try {
	    	generator.beforeRun();
	        
	    	Configuration defaults = generator.getDefaultConfig();
	    	
	    	for (NodeAndOptions pair : group.getNodes()) {
	            log.debug("processing annotation '" + pair.options.toString() + "' in '" + pair.enclosedInType.getFullName());
	            try {
	            	Configuration cfg = merge(pair.options.getConfig(),defaults);
	            	generator.generate(pair.forNode, cfg);
	            } catch(Exception e){
	            	throw new JMutateException("error processing node : " + pair.forNode,e);
	            }
	        }
    	} catch(Exception e){
         	throw new JMutateException("error invoking generator " + generator.getClass().getName(),e);
    	} finally {
    		generator.afterRun();
    	}
    }
    
    private Configuration merge(Configuration cfg,Configuration defaults){
    	CompositeConfiguration merged = new CompositeConfiguration();
    	//first one wins
    	merged.addConfiguration(cfg);
    	if(defaults !=null){
    		merged.addConfiguration(defaults);
    	}
    	return merged;
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
                        .stringContent(MATCH_CONTENT)//prevent parsing nodes we don't need to
                        ;
        if(log.isDebugEnabled()){
             log.debug("match resource " + resourceFilter);
        }
        Matcher<JType> typeMatcher= all(
        		AJType.with().annotation(annotationFilter).expression(scanSubtypes?null:"notInnerClass"),
        		not(AJType.with().annotation(AJAnnotation.with().fullName(IsGeneratorTemplate.class))),
        		not(AJType.with().annotation(AJAnnotation.with().fullName(DisableGenerators.class))));
				
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

    /**
     * Given a list of annotations, group them by type (fullname). This is so we can run a generator once for a given annotation type
     * 
     * @param annotations
     * @return
     */
    private GroupedAnnotations groupByAnnotationType(List<Annotation> annotations) {
        //collect all the annotations of a given type to be passed to a generator at once (and so we can apply generator ordering)        
    	GroupedAnnotations groups = new GroupedAnnotations();
    	
		for(Annotation sourceAnnotation:annotations){
		     ASTNode attachedToNode = getOwningNodeFor(sourceAnnotation);
		     extractOptions(groups, attachedToNode, sourceAnnotation);
		}
        return groups;
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

    private CodeGenerator<?> getGeneratorFor(String annotationType){
        String generatorClassName = generators.get(annotationType);
        if (generatorClassName == null && autoRegisterDefaultGenerators) {
            autoRegisterGeneratorFor(annotationType);
            generatorClassName = generators.get(annotationType);
        }
        
        if(!filterGenerators.matches(generatorClassName)){
    		log.info("generator '" + generatorClassName + "' is marked to be ignored, skipping");
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


    /**
     * If the provided annotation is a generator annotation (else a normal annotation which got caught up in the scan)
     */
    private void extractOptions(GroupedAnnotations groups, ASTNode forNode, Annotation annotationOnNode){
		String annotationType = JAnnotation.from(annotationOnNode).getQualifiedName();
		//skip if we've already rules out this annotation type
		if(normalAnnotationsToIgnore.contains(annotationType)){
			return;
		}
		
		//use the templates options if it's already been loaded
		List<InternalGeneratorConfig> options = templateOptions.get(annotationType);
		
		if(options != null){
			//only add if not the template!
			
			
			groups.addOptions(forNode, options);
			return;
		}
		
		//haven't come across this annotation yet, let's see if we can load it 
		
		// look up the source first. This may have changed and not yet compiled
		// (freshest)
		RootResource resource = resourceLoader.getResourceOrNullFromClassName(annotationType);
		if (resource != null && resource.exists()) {
			JSourceFile annotatinDeclarationSource = JSourceFile.fromResource(resource,ctxt.getParser());
			JType annotationDeclarationType = annotatinDeclarationSource.findTypesMatching(AJType.with().fullName(annotationType)).getFirstOrNull();
			if (annotationDeclarationType == null) {
				log.warn("Can't find type '" + annotationType + "' in " + annotatinDeclarationSource.getResource().getFullPath());
			} else {
				if(annotationDeclarationType.getAnnotations().contains(IsGeneratorTemplate.class)){//this is a template annotation
					//TODO:make recursive! if these annotations point back to other generators, include them too!
					groups.addOptions(forNode, extractTemplateOptions(annotationDeclarationType));
					return;
				} else if (annotationDeclarationType.getAnnotations().contains(IsGeneratorConfig.class)){//this is an annotation for a generator
					groups.addOptions(forNode, new InternalGeneratorConfig(annotationOnNode));
					return;
				} else {//ensure we ignore so we don't try to scan again
					normalAnnotationsToIgnore.add(annotationType);
				}
				return;
			}
		}
		// try the compiled version
		Class<?> annotationClass = resourceLoader.loadClassOrNull(annotationType);
		if (annotationClass != null) {
			if(annotationClass.isAnnotationPresent(IsGeneratorTemplate.class)){//this is a template annotation
				groups.addOptions(forNode, extractTemplateOptions(annotationClass));
				return;
			} else if(annotationClass.isAnnotationPresent(IsGeneratorConfig.class)){//this is an annotation for a generator
				groups.addOptions(forNode, new InternalGeneratorConfig(annotationOnNode));
				return;
			} else {//ensure we ignore so we don't try to scan again
				normalAnnotationsToIgnore.add(annotationType);
			}
			return;
		}

		log.warn("Can't load annotation as class or resource '" + annotationType + "'. Ignoring as a generator");
    }

	private List<InternalGeneratorConfig> extractTemplateOptions(JType t) {
		List<InternalGeneratorConfig> options = new ArrayList<>();
		FindResult<JAnnotation> templateAnnotations = t.getAnnotations().find(CLIENT_OPTIONS_ANNOTATIONS);
		for(JAnnotation templateAnon:templateAnnotations){
			options.add(new InternalGeneratorConfig(templateAnon.getAstNode()));
		}
		templateOptions.put(t.getFullName(),options);
		return options;
	}
    
	private List<InternalGeneratorConfig> extractTemplateOptions(Class<?> t) {
		List<InternalGeneratorConfig> options = new ArrayList<>();
		for(java.lang.annotation.Annotation a : t.getAnnotations()){
			if(COMPILED_GENERATION_ANNOTATION.matches(a)){ //an annotation which is a generator annotation
				options.add(new InternalGeneratorConfig(a));
			}
		}
		templateOptions.put(t.getName(),options);
		return options;
	}
	
    private void autoRegisterGeneratorFor(String annotationType) {
        try {
            Class<?> annotation = ctxt.getResourceLoader().loadClass(NameUtil.sourceNameToCompiledName(annotationType));
            IsGeneratorConfig defGenAnon = annotation.getAnnotation(IsGeneratorConfig.class);
            if(defGenAnon!=null){
                log.info("auto registering generator '" + defGenAnon.defaultGenerator() + "' for annotation '" + annotationType + "'");
                generators.put(annotationType, defGenAnon.defaultGenerator());
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

	static class GroupedAnnotations {
        private Map<String, Group> groupByAnnotationType = new HashMap<>();
        
        public void addOptions(ASTNode forNode,Iterable<InternalGeneratorConfig> options) {
        	if(options != null){
        		for(InternalGeneratorConfig option:options){
        			getOrCreateGroup(option.getAnnotationType()).addParentOptions(forNode, option);
        		}
        	}
    	}

    	public void addOptions(ASTNode forNode,InternalGeneratorConfig options) {
    		getOrCreateGroup(options.getAnnotationType()).addParentOptions(forNode, options);
    	}

    	private Group getOrCreateGroup(String annotationType) {
			Group group = groupByAnnotationType.get(annotationType);
			if(group == null){
    		    group = new Group(annotationType);
    		    groupByAnnotationType.put(annotationType, group);
    		}
			return group;
		}
    	
    	public Iterable<Group> getGroups(){
    		return groupByAnnotationType.values();
    	}
        
        /**
         * A collection of nodes with the same annotation type. This will be sent to the appropriate generator as a group
         */
        static class Group {
        	private final String fullAnnotationName;
            
            private final Map<ASTNode,NodeAndOptions> nodes = new HashMap<>();
            
            public Group(String annotationName){
                this.fullAnnotationName = annotationName;
            }
       
            public void addParentOptions(ASTNode forNode,InternalGeneratorConfig options){
            	NodeAndOptions existing = nodes.get(forNode);
            	if(existing == null){
            		nodes.put(forNode, new NodeAndOptions(forNode, options));
            	} else {
            		existing.options.addParentConfig(options.getConfig());
            	}
            }
           
            public Collection<NodeAndOptions> getNodes() {
                return nodes.values();
            }

            public String getAnnotationType() {
                return fullAnnotationName;
            }
        }
        /**
         * Holds a generator config with the node it is applied to
         */
        static class NodeAndOptions {
        	private final ASTNode forNode;
        	private final InternalGeneratorConfig options;
        	private final JType enclosedInType;

    		public NodeAndOptions(ASTNode node, InternalGeneratorConfig options) {
    			super();
    			this.forNode = node;
    			this.options = options;
                this.enclosedInType = findNearestParentTypeFor(node);
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
