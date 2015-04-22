package org.codemucker.jmutate.generate.bean;

import java.lang.annotation.Annotation;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.AbstractGenerator;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModelExtractor;

/**
 * 
 * @param <T> the annotation this generator is using for configuration
 * @param <TOptions> the options class this generator is mapping the annotation to. Property setters or fields must match those 
 * of the annotation. That is, foo() on the annotation is setFoo() on the options
 */
public abstract class AbstractBeanGenerator<T extends Annotation,TOptions extends AbstractBeanOptions<T>> extends AbstractGenerator<T>  {

	private static final Logger LOG = LogManager.getLogger(AbstractBeanGenerator.class);

	private Class<T> annotationType;
	
	protected abstract TOptions createOptionsFrom(Configuration config, JType type);
	protected abstract void generate(JType bean, SmartConfig config, PojoModel model,TOptions options);

	public AbstractBeanGenerator(JMutateContext ctxt, Class<T> annotationType) {
		super(ctxt);
		this.annotationType = annotationType;
	}

	@Override
	public void generate(JType node, SmartConfig config) {
		TOptions options = createOptionsFrom(config.getConfigFor(annotationType),node);
		
		if(!options.isEnabled()){
			LOG.info("generator annotation marked as disabled, not running generation");
			return;
		}
		if(node.isInterface()){
			LOG.warn("the " + annotationType.getName() + " generation annotation on an interface is not supported");
			return;
		}

		PojoModel model = extractPropertiesModelFrom(node, options);
		setClashStrategy(options.getClashStrategy());
		
		LOG.debug("found " + model.getAllProperties().toList().size() + " bean properties for "  + options.getType().getFullNameRaw());
		
		JSourceFile source = node.getCompilationUnit().getSource();
		generate(node, config, model, options);	
		writeToDiskIfChanged(source);	
	}
	
    private PojoModel extractPropertiesModelFrom(JType node, AbstractBeanOptions<?> options){
    	PropertyModelExtractor extractor = getContext().obtain(PropertyModelExtractor.Builder.class)
    		.includeCompiledClasses(true)
    		.propertyNameMatching(options.getFieldNames())
    		.includeSuperClass(options.isInheritParentProperties())
    		.build();
    		
    	PojoModel pojo = extractor.extractModelFromClass(node);

    	return pojo;
    }


}