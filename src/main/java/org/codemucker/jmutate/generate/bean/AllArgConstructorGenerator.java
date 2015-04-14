package org.codemucker.jmutate.generate.bean;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.bean.AllArgConstructorGenerator.AllArgOptions;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModel;
import org.codemucker.jpattern.bean.Property;
import org.codemucker.jpattern.generate.GenerateAllArgsCtor;

import com.google.inject.Inject;

public class AllArgConstructorGenerator extends AbstractBeanGenerator<GenerateAllArgsCtor,AllArgOptions> {

	@Inject
	public AllArgConstructorGenerator(JMutateContext ctxt) {
		super(ctxt,GenerateAllArgsCtor.class);
	}

	
	@Override
	protected void generate(JType beanType, SmartConfig config, PojoModel model, AllArgOptions options) {
		if(options.isEnabled()){
			SourceTemplate beanCtor = getCtxt()
					.newSourceTemplate()
					.var("b.name", options.getType().getSimpleNameRaw())
					.pl("private ${b.name} (");
			
			boolean comma = false;
			//args
			List<PropertyModel> props = model.getAllProperties();
			
			for (PropertyModel property : props) {
				if(!property.hasField()){
					continue;
				}
				if(comma){
					beanCtor.p(",");
				}
				if(options.markCtorArgsAsProperties){
					beanCtor.p("@" + Property.class.getName() + "(name=\"" + property.getName() + "\") ");
				}
				beanCtor.p(property.getType().getFullName() + " " + property.getName());
				comma = true;
			}
			
			beanCtor.pl("){");
			//field assignments
			for (PropertyModel property : props) {
				if(property.isSuperClassProperty()){
					beanCtor.pl(property.getSetterName() + "(" + property.getName() + ");");
				} else {
					beanCtor.pl("this." + property.getName() + "=" + property.getName() + ";");
				}
			}
			beanCtor.pl("}");
			addMethod(beanType, beanCtor.asConstructorNodeSnippet(),options.isMarkGenerated());
		}
	}

	@Override
	protected AllArgOptions createOptionsFrom(Configuration config,JType type) {
		return new AllArgOptions(config,type);
	}
	
	public static class AllArgOptions extends AbstractBeanOptions<GenerateAllArgsCtor> {

		public boolean markCtorArgsAsProperties;
		
		public AllArgOptions(Configuration config,JType pojoType) {
			super(config, GenerateAllArgsCtor.class, pojoType);
		}
	}
}