package org.codemucker.jmutate.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.Roots;
import org.codemucker.jfind.matcher.ARoot;
import org.codemucker.jfind.matcher.ARootResource;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.DefaultMutateContext;
import org.codemucker.jmutate.DefaultResourceLoader;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ProjectOptions;
import org.codemucker.jmutate.SourceFilter;
import org.codemucker.jmutate.SourceScanner;
import org.codemucker.jmutate.ast.BaseSourceVisitor;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.MutateUtil;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GeneratorOptions;
import org.codemucker.jpattern.generate.IsGenerated;
import org.codemucker.jpattern.generate.IsGeneratorTemplate;
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
    private static final Matcher<JAnnotation> GENERATION_ANNOTATIONS = AJAnnotation.with().fullName(AString.matchingExpression("*.*Generate* && !" + IsGenerated.class.getName() + " && !" + Generated.class.getName() ));

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
	private final Map<String, List<GeneratorConfigImpl>> templateOptions = new HashMap<>();
	/**
	 * Used to prevent constant scanning of already found annotations
	 */
	private final List<String> normalAnnotationsCache = new ArrayList<>();
	
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
        this.annotationFilter = Logical.all(GENERATION_ANNOTATIONS,AJAnnotation.with().fullName(filterAnnotations) );
        
        
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
        List<Annotation> generateAnnotations = scanForGenerationAnnotations();
        if(generateAnnotations.size()==0){
            log.info("no generation annotations found");
            return;
        }
        //compile all the annotations at once instead of calling the compiler once for each annotation. The compiled
        //version will be cached on the ast node of each annotation
        //annotationCompiler.compileAnnotations(generateAnnotations);
        //group the found annotations by the generator they invoke
        Collection<GroupedNodesAndOptions> groups = groupByAnnotationType(generateAnnotations);
        //run the generators in order (of generator)
        for (GroupedNodesAndOptions group : groups) {
            CodeGenerator<?> generator = getGeneratorFor(group.getAnnotationName());
            if (generator != null) {
            	generator.beforeRun();
                invokeGenerator(generator,group);
                generator.afterRun();
            }
        }
        MutateUtil.setClassLoader(null);
    }

    private void invokeGenerator(CodeGenerator<?> generator, GroupedNodesAndOptions nodesForThisGenerator) {
        for (NodeAndOptions pair : nodesForThisGenerator.getNodes()) {
            log.debug("processing annotation '" + pair.options.toString() + "' in '" + pair.enclosedInType.getFullName());
            try {
            	generator.generate(pair.forNode, pair.options);
            } catch(Exception e){
            	throw new JMutateException("error processing node : " + pair.forNode,e);
            }
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
                        .stringContent(MATCH_CONTENT)//prevent parsing nodes we don't need to
                        ;
        if(log.isDebugEnabled()){
             log.debug("match resource " + resourceFilter);
        }
        //find all the code with generation annotations
        SourceScanner scanner = ctxt.obtain(SourceScanner.Builder.class)
                .failOnParseError(failOnParseError)
                .parser(ctxt.getParser())
                .scanRoots(scanRoots)
                .filter(SourceFilter.with()
                        .rootMatches(scanRootFilter)
                        .resourceMatches(resourceFilter)
                        .typeMatches(AJType.with().annotation(GENERATION_ANNOTATIONS).expression(scanSubtypes?null:"notInnerClass")))
                .build();

        GenerationAnnotationCollectorVisitor collector = new GenerationAnnotationCollectorVisitor(annotationFilter);
        scanner.visit(collector);
        
        log("found " + collector.getResults().size() + " code generation annotations");
        return collector.getResults();
    }
    
    /**
     * Given a list of annotations, group them by type (fullname). This is so we can run a generator once for a given annotation type
     * 
     * @param generatorAnnotations
     * @return
     */
    private Collection<GroupedNodesAndOptions> groupByAnnotationType(List<Annotation> generatorAnnotations) {
        //collect all the annotations of a given type to be passed to a generator at once (and so we can apply generator ordering)        
        Map<String, GroupedNodesAndOptions> nodesByAnnotationName = new HashMap<>();
       
		for(Annotation sourceAnnotations:generatorAnnotations){
			 String fullName = JAnnotation.from(sourceAnnotations).getQualifiedName();
			 
		     ASTNode attachedToNode = getOwningNodeFor(sourceAnnotations);
			
			if (isTemplate(fullName)) {
				useTemplateOptionsFor(nodesByAnnotationName, fullName, attachedToNode);
			} else if(isGeneratorAnnotation(sourceAnnotations) && !isTemplateNode(attachedToNode)){
				GeneratorConfigImpl options = new GeneratorConfigImpl(sourceAnnotations);
				addToGroup(nodesByAnnotationName, fullName, attachedToNode, options);
			}
		}
        return nodesByAnnotationName.values();
    }
    
    /**
     * If the provided annotation is a generator annotation (else a normal annotation which got caught up in the scan)
     */
    private boolean isGeneratorAnnotation(Annotation a){
    	//TODO:only use this annotation if this annotation type is marked with 'GeneatorOptions'
		JAnnotation ja = JAnnotation.from(a);
		String qn = ja.getQualifiedName();

		// look up the source first. This may have changed and not yet compiled
		// (freshest)
		RootResource resource = resourceLoader.getResourceOrNullFromClassName(qn);
		if (resource != null && resource.exists()) {
			JSourceFile source = JSourceFile.fromResource(resource,ctxt.getParser());
			JType t = source.findTypesMatching(AJType.with().fullName(qn)).getFirstOrNull();
			if (t == null) {
				log.warn("Can't find type '" + qn + "' in " + source.getResource().getFullPath());
			} else {
				return t.getAnnotations().contains(GeneratorOptions.class);
			}
		}
		// try the compiled version
		Class<?> annotationClass = resourceLoader.loadClassOrNull(qn);
		if (annotationClass != null) {
			return annotationClass.isAnnotationPresent(GeneratorOptions.class);
		}

		log.warn("Can't load annotation as class or resource '" + qn + "'. Ignoring as a generator");
		return false;
    }

    /**
     * Is the given source node a template noe, that is, solely to collect multiple generator annotations and mark-up other classes for generation
     */
	private boolean isTemplateNode(ASTNode node) {
		boolean isTemplateNode = false;
		if(JType.is(node) && JType.from(node).getAnnotations().contains(IsGeneratorTemplate.class)){
			isTemplateNode = true;
		}
		return isTemplateNode;
	}

	private boolean isTemplate(String annotationClassName) {
		if(normalAnnotationsCache.contains(annotationClassName)){
			return false;
		}
		if(templateOptions.containsKey(annotationClassName)){
			return true;
		}
		extractAnnotationDetails(annotationClassName);
		
		return templateOptions.containsKey(annotationClassName);
	}

	private void extractAnnotationDetails(String annotationClassName) {
		//lets see if this annotation is a template
		log.debug("trying to load annotation class " + annotationClassName + " from source");
		//ok, template might be a source file in the project
		String sourcePath = annotationClassName.replace('.', '/') + ".java";
		RootResource resource = ctxt.getResourceLoader().getResourceOrNull(sourcePath);
		//extract the annotation details from source code
		if(resource != null && resource.exists()){
			JSourceFile source = JSourceFile.fromResource(resource, ctxt.getParser());
			if(source.getMainType().getAnnotations().contains(IsGeneratorTemplate.class)){
				//grab all the annotation options from the template. This will be in source form
				GenerationAnnotationCollectorVisitor collector = new GenerationAnnotationCollectorVisitor(annotationFilter);
				source.accept(collector);
				List<Annotation> templateAnnotations = collector.getResults();
				
				//now compile all the options and store them
				List<GeneratorConfigImpl> options = new ArrayList<>();
				for(Annotation a:templateAnnotations){
					options.add(new GeneratorConfigImpl(a));
				}
				templateOptions.put(annotationClassName, options);
				log.debug("found " + options.size() + " generation annotations on template " + annotationClassName + "");
			}
		} else { //extract the annotation details from a compiled class
			try {
				Class<?> normalOrTemplateAnon = ctxt.getResourceLoader().loadClass(annotationClassName);
				
				//handle the case where template might be in an external project instead of as a source file in the current project
				if(normalOrTemplateAnon!= null && normalOrTemplateAnon.isAnnotationPresent(IsGeneratorTemplate.class)){
					//found a template annotation. Extract all annotations from it
					ArrayList<GeneratorConfigImpl> options = new ArrayList<>();
					//only get the annotations related to generation
					for (java.lang.annotation.Annotation a : normalOrTemplateAnon.getAnnotations()) {
						if (a.annotationType().isAnnotationPresent(GeneratorOptions.class)) {
							options.add(new GeneratorConfigImpl(a));
						}
					}
					templateOptions.put(annotationClassName, options);
					log.debug("found " + options.size() + " generation annotations on template " + annotationClassName + "");
				}
			} catch (ClassNotFoundException e) {
				//bugger. oh well, default is to treat as a normal one
			}
		}
	}

	private void useTemplateOptionsFor(Map<String, GroupedNodesAndOptions> nodesByAnnotationName, String annotationClassName,ASTNode attachedToNode) {
		List<GeneratorConfigImpl> options = this.templateOptions.get(annotationClassName);
		if (options != null) {
			for(GeneratorConfigImpl opt:options){
				String optionKey = opt.getKey();
				addToGroup(nodesByAnnotationName, optionKey, attachedToNode, opt);
			}
		}
	}

	private void addToGroup(
			Map<String, GroupedNodesAndOptions> nodesByAnnotationName,
			String annotationClassName, 
			ASTNode attachedToNode,
			GeneratorConfigImpl generatorOptions) {
		GroupedNodesAndOptions group = nodesByAnnotationName.get(annotationClassName);
		if(group == null){
		    group = new GroupedNodesAndOptions(annotationClassName);
		    nodesByAnnotationName.put(annotationClassName, group);
		}
		
		group.addNode(attachedToNode, generatorOptions);
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
        CodeGenerator<?> gen = (CodeGenerator<?>) ctxt.obtain(generatorClass);

        return gen;
    }

    private void autoRegisterGeneratorFor(String annotationType) {
        try {
            Class<?> annotation = ctxt.getResourceLoader().loadClass(NameUtil.sourceNameToCompiledName(annotationType));
            GeneratorOptions defGenAnon = annotation.getAnnotation(GeneratorOptions.class);
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
    
    /**
     * A collection of nodes with the same annotation type. This will be sent to the appropriate generator as a group
     */
    private static class GroupedNodesAndOptions {
        private final String fullAnnotationName;
        private final Set<NodeAndOptions> collectedOptions = new HashSet<>();
        
        public GroupedNodesAndOptions(String annotationName){
            this.fullAnnotationName = annotationName;
        }
        
        public void addNode(ASTNode node,GeneratorConfigImpl options){
            collectedOptions.add(new NodeAndOptions(node,options));
        }

        public Set<NodeAndOptions> getNodes() {
            return collectedOptions;
        }

        public String getAnnotationName() {
            return fullAnnotationName;
        }
    }
    /**
     * Holds a generator config with the node it is applied to
     */
    private static class NodeAndOptions {
    	private final ASTNode forNode;
    	private final GeneratorConfigImpl options;
    	private final JType enclosedInType;

		public NodeAndOptions(ASTNode node, GeneratorConfigImpl options) {
			super();
			this.forNode = node;
			this.options = options;
            this.enclosedInType = findNearestParentTypeFor(node);
		}
    }
    
    static class GenerationAnnotationCollectorVisitor extends BaseSourceVisitor {
    	private final List<Annotation> found = new ArrayList<>();
    	private final Matcher<JAnnotation> annotationMatcher;
    	
    	public GenerationAnnotationCollectorVisitor(Matcher<JAnnotation> annotationMatcher) {
			super();
			this.annotationMatcher = annotationMatcher;
		}
		public List<Annotation> getResults(){
    		return found;
    	}
    	@Override
        public boolean visit(JSourceFile node) {
            //log.debug("visit root:" + node.getPathName());
            return true;
        }
    	
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
          
            if (annotationMatcher.matches(a)) {
                found.add(node);
            } else {
                //log("skipped annotation:" + a.getQualifiedName());
            }
            return false;
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
