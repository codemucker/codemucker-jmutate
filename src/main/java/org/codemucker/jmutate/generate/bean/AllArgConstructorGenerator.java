package org.codemucker.jmutate.generate.bean;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.bean.Property;
import org.codemucker.jpattern.generate.DontGenerate;
import org.codemucker.jpattern.generate.GenerateAllArgsCtor;

import com.google.inject.Inject;

/**
 * Generates property getters/setters for a bean, along with various bean
 * bindings if required
 */
public class AllArgConstructorGenerator extends AbstractBeanGenerator<GenerateAllArgsCtor> {

	private static final Logger LOG = LogManager.getLogger(AllArgConstructorGenerator.class);

	@Inject
	public AllArgConstructorGenerator(JMutateContext ctxt) {
		super(ctxt);
	}

	
	@Override
	protected void generate(JType beanType, BeanModel model) {
		if(model.options.isGenerateAllArgCtor()){
			SourceTemplate beanCtor = getCtxt()
					.newSourceTemplate()
					.var("b.name", model.options.getType().getSimpleNameRaw())
					.pl("private ${b.name} (");
			
			boolean comma = false;
			//args
			for (BeanPropertyModel property : model.getProperties()) {
				if(!property.hasField()){
					continue;
				}
				if(comma){
					beanCtor.p(",");
				}
				if(model.options.isMarkCtorArgsAsProperties()){
					beanCtor.p("@" + Property.class.getName() + "(name=\"" + property.getPropertyName() + "\") ");
				}
				beanCtor.p(property.getType().getFullName() + " " + property.getPropertyName());
				comma = true;
			}
			
			beanCtor.pl("){");
			//field assignments
			for (BeanPropertyModel property : model.getProperties()) {
				if(property.isFromSuperClass()){
					beanCtor.pl(property.getPropertySetterName() + "(" + property.getPropertyName() + ");");
				} else {
					beanCtor.pl("this." + property.getPropertyName() + "=" + property.getPropertyName() + ";");
				}
			}
			beanCtor.pl("}");
			addMethod(beanType, beanCtor.asConstructorNodeSnippet(),model.options.isMarkGenerated());
		}
	}
	
	@Override
	protected GenerateAllArgsCtor getAnnotation() {
		return Defaults.class.getAnnotation(GenerateAllArgsCtor.class);
	}
	
	@DontGenerate
	@GenerateAllArgsCtor
	private static class Defaults {}
}