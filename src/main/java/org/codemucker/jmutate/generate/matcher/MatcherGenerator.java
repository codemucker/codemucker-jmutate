package org.codemucker.jmutate.generate.matcher;

import java.util.NoSuchElementException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmatch.PropertyMatcher;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.matcher.MatcherGenerator.GenerateMatcherOptions;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jmutate.generate.model.TypeModelExtractor;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModelExtractor;
import org.codemucker.jpattern.generate.matcher.GenerateMatcher;

import com.google.inject.Inject;

/**
 * Generates the matcher for a single pojo
 */
public class MatcherGenerator extends AbstractMatchGenerator<GenerateMatcher,GenerateMatcherOptions> {

    private static final Logger LOG = LogManager.getLogger(MatcherGenerator.class);
    
	private final TypeModelExtractor typeExtractor;

    @Inject
    public MatcherGenerator(JMutateContext ctxt,TypeModelExtractor typeExtractor) {
    	super(ctxt,GenerateMatcher.class,GenerateMatcherOptions.class);
    	this.typeExtractor = typeExtractor;
    }

	@Override
	public void generate(JType declaredInType, SmartConfig config,GenerateMatcherOptions options) {	
//		if(!options.keepInSync){
//			return;
//		}
		
		JSourceFile source = declaredInType.getCompilationUnit().getSource();
		String generateFor = options.generateFor;
		if(generateFor == null || Object.class.getName().equals(generateFor)){
			throw new JMutateException("need to set the 'generateFor' in " + source.getResource().getFullPath() + " for annotation " + GenerateMatcher.class + " other than blank or Object");
		}
		
		TypeModel generateForType = typeExtractor.extractModelFromClass(options.generateFor);
		
		PropertyModelExtractor propertyExtractor = ctxt.obtain(PropertyModelExtractor.Builder.class)
				.includeSuperClass(options.inheritParentProperties)
				.build();
		
		PojoModel generateForPropertyModel = null;
		try {
			generateForPropertyModel = propertyExtractor.extractModelFromClass(options.generateFor);
		} catch(NoSuchElementException e){
			throw new JMutateException("couldn't load source or class '" + generateFor + "' to generate matcher, set in 'generateFor' in " + source.getResource().getFullPath() + " for annotation " + GenerateMatcher.class);		
		}

		String superType = declaredInType.getSuperTypeFullName();
		if(superType != null){
			//TODO:check correct super class. 
		}
		
		//TOO:if not exists super!
		declaredInType.asMutator(ctxt).setExtends(PropertyMatcher.class.getName() + "<" + generateForType.getFullName() + ">");
		
		generateDefaultConstructor(source.getMainType(), generateForType);
		generateMatcher(options,generateForPropertyModel, declaredInType);
		writeToDiskIfChanged(source);
	}


	public static class GenerateMatcherOptions extends AbstractMatcherModel<GenerateMatcher> {
		public String generateFor;
    	public boolean oneTimeOnly;
    	public String matcherBaseClass;
    	public String matcherPrefix;
    	public String[] builderMethodNames;
    	
    }


}