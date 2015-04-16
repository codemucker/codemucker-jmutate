package org.codemucker.jmutate.generate.matcher;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.matcher.MatcherGenerator.GenerateMatcherOptions;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModelExtractor;
import org.codemucker.jpattern.generate.matcher.GenerateMatcher;

import com.google.inject.Inject;

/**
 * Generates the matchers for pojos
 */
public class MatcherGenerator extends AbstractMatchGenerator<GenerateMatcher,GenerateMatcherOptions> {

    private static final Logger LOG = LogManager.getLogger(MatcherGenerator.class);

    @Inject
    public MatcherGenerator(JMutateContext ctxt) {
    	super(ctxt,GenerateMatcher.class,GenerateMatcherOptions.class);
    }

	@Override
	public void generate(JType declaredInType, SmartConfig config,GenerateMatcherOptions options) {	
//		if(!options.keepInSync){
//			return;
//		}
		PropertyModelExtractor extractor = PropertyModelExtractor.with(ctxt.getResourceLoader(), ctxt.getParser())
				.includeSuperClass(options.inheritParentProperties)
				.build();
		
	  	PojoModel model = extractor.extractModel(declaredInType);
		JSourceFile source = declaredInType.getCompilationUnit().getSource();
		String generateFor = options.generateFor;
		if( generateFor == null || Object.class.getName().equals(generateFor)){
			LOG.error("need to set the 'generateFor' in " + source.getResource().getFullPath() + " for annotation " + GenerateMatcher.class);
		}
		String superType = declaredInType.getSuperTypeFullName();
		if(superType != Object.class.getName()){
			//TODO:check correct super class. 
			
		}
		TypeModel matcherType = model.getType();//TODO:if 
		generateMatcher(options,model, declaredInType);
		writeToDiskIfChanged(source);
	}

    private String findBean(JType declaredInType, GenerateMatcherOptions options) {
    	//look in superclass generic type
		//declaredInType.
    			//if current class 'AFoo', try 'Foo'
    			
    			//else fail
    	    	
    	return null;
	}

	public static class GenerateMatcherOptions extends AbstractMatcherModel<GenerateMatcher> {
		public String generateFor;
    	public boolean oneTimeOnly;
    	public String matcherBaseClass;
    	public String matcherPrefix;
    	public String[] builderMethodNames;
    	
    }


}