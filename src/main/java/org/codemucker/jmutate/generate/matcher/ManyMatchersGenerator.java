package org.codemucker.jmutate.generate.matcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.RootResource;
import org.codemucker.jmatch.PropertyMatcher;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.matcher.ManyMatchersGenerator.GenerateManyMatchersOptions;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModelExtractor;
import org.codemucker.jpattern.generate.matcher.GenerateManyMatchers;

import com.google.inject.Inject;

/**
 * Generates the matchers for pojos
 */
public class ManyMatchersGenerator extends AbstractMatchGenerator<GenerateManyMatchers,GenerateManyMatchersOptions> {

    private static final Logger LOG = LogManager.getLogger(ManyMatchersGenerator.class);

    @Inject
    public ManyMatchersGenerator(JMutateContext ctxt) {
    	super(ctxt,GenerateManyMatchers.class,GenerateManyMatchersOptions.class);
    }

	@Override
	public void generate(JType optionsDeclaredInNode, SmartConfig config,GenerateManyMatchersOptions options) {
		Root root = optionsDeclaredInNode.getCompilationUnit().getSource().getResource().getRoot();
		
		findAndAddModels(optionsDeclaredInNode,options);
		generateMatchers(options, root);
	}

	private void findAndAddModels(JType optionsDeclaredInNode,GenerateManyMatchersOptions options) {
		PojoSourceAndClassScanner pojoScanner = new PojoSourceAndClassScanner(
				ctxt.getResourceLoader(), 
				options.getPojoDependencies(), 
				options.getPojoNames(),
				options.getPojoTypes());
	    
		PropertyModelExtractor extractor = ctxt.obtain(PropertyModelExtractor.Builder.class)
				.includeSuperClass(options.inheritParentProperties)
				.build();
		
        if(options.isScanSources() ){
            FindResult<JType> pojos = pojoScanner.scanSources();
            // add the appropriate methods and types for each request bean
            for (JType pojo : pojos) {
            	PojoModel model = extractor.extractModelFromClass(pojo);
            	options.addMatcher(model);
            }
        }
        
        if(options.isScanDependencies()){
            FindResult<Class<?>> pojos = pojoScanner.scanForReflectedClasses();
            // add the appropriate methods and types for each request bean
            for (Class<?> pojo : pojos) {
             	PojoModel model = extractor.extractModelFromClass(pojo);
            	options.addMatcher(model);
            }
        }
        LOG.info("found " + options.getPojoModels().size() + " matchers to generate from source and compiled classes");
    	
    }

	private void generateMatchers(GenerateManyMatchersOptions options,Root generateTo) {
		for (PojoModel pojo: options.getPojoModels()) {
			generateMatcherForPojo(options,pojo,generateTo);
		}
	}
	
	private void generateMatcherForPojo(GenerateManyMatchersOptions options,PojoModel model,Root generateTo) {
		JType matcher = newOrExistingMatcherType(options,generateTo,model.getType());
		if(matcher!= null){
			generateMatcher(options, model,matcher);
			writeToDiskIfChanged(matcher.getCompilationUnit().getSource());	
		}
	}

    protected JType newOrExistingMatcherType(GenerateManyMatchersOptions options,Root generateTo,TypeModel forType) {
    	TypeModel matcherType = toMatcherType(forType);
    	LOG.debug("checking for source file for " + matcherType.getFullName() + "");
    	//Hmm, TODO, sourceLoader???
    	String path = matcherType.getFullName().replace('.', '/') + ".java";
    	RootResource sourceFile = generateTo.getResource(path);
    	JSourceFile  source;
    	if(sourceFile.exists()){
    		if(!options.isKeepInSync()){
				LOG.debug("skipping source as marked to genrate one time only");
    			return null;//skip this generation
			}
    		LOG.debug("matcher source file " + path + " exists, loading");
    		source = JSourceFile.fromResource(sourceFile, ctxt.getParser());    
    	} else {
    		LOG.debug("creating new matcher source file " + path + "");
    		SourceTemplate t = ctxt.newSourceTemplate().pl("package " + matcherType.getPkg() + ";").pl("");
            addGeneratedMarkers(t);
            t.pl("public class " + matcherType.getSimpleName() + " extends  " + PropertyMatcher.class.getName() + "<" + forType.getFullName() + ">{}");
            source = t.asSourceFileSnippet();
    	}
    	
    	generateDefaultConstructor(source.getMainType(), forType);
    	
    	return source.getMainType();
    }

    
    public class GenerateManyMatchersOptions extends AbstractMatcherModel<GenerateManyMatchers> {
    	
    	public String pojoDependencies;
    	public String  pojoNames;
    	public String pojoTypes;
    	
    	public String defaultPackage;
    	public boolean scanSources;
    	public boolean scanDependencies;
    	
        private final List<PojoModel> matchers = new ArrayList<>();

        void addMatcher(PojoModel model){
            this.matchers.add(model);
        }

    	public String getDefaultPackage() {
    		return defaultPackage;
    	}

    	List<PojoModel> getPojoModels() {
    		return matchers;
    	}

    	public String getPojoDependencies() {
    		return pojoDependencies;
    	}

    	public String getPojoNames() {
    		return pojoNames;
    	}

    	public String getPojoTypes() {
    		return pojoTypes;
    	}

    	public boolean isScanSources() {
    		return scanSources;
    	}

    	public boolean isScanDependencies() {
    		return scanDependencies;
    	}
    	
    }


}