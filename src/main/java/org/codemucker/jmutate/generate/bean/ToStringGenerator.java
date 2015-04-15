package org.codemucker.jmutate.generate.bean;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.bean.ToStringGenerator.ToStringOptions;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModel;
import org.codemucker.jpattern.generate.GenerateToString;

import com.google.inject.Inject;

/**
 * Generates the 'toString' method on pojos
 */
public class ToStringGenerator extends AbstractBeanGenerator<GenerateToString,ToStringOptions> {

	@Inject
	public ToStringGenerator(JMutateContext ctxt) {
		super(ctxt,GenerateToString.class);
	}
	
	@Override
	protected void generate(JType bean, SmartConfig config,PojoModel model, ToStringOptions options) {
		if(options.isEnabled()){
			StringBuilder sb = new StringBuilder();
			sb.append("\" [");
			if(!model.hasAnyProperties()){
				sb.append("]\"");
			} else {
				boolean comma = false;
				for (PropertyModel property : model.getAllProperties()) {
					if(!(property.hasField() || property.hasGetter())){
						continue;
					}
					if(comma){
						sb.append(" + \",");
					}
					sb.append(property.getName() ).append("=\" + ");
					sb.append(property.getInternalAccessor());
					comma = true;
				}
				sb.append(" + \"]\"");
			}
			
			SourceTemplate toString = newSourceTemplate()
				.pl("@java.lang.Override")
				.pl("public String toString(){")
				.pl("return this.getClass().getName() + \"@\" + System.identityHashCode(this) + " + sb.toString() + ";")
				.pl("}");
			addMethod(bean, toString.asMethodNodeSnippet(),options.isMarkGenerated());
		}
	}
	
	@Override
	protected ToStringOptions createOptionsFrom(Configuration config, JType type) {
		return new ToStringOptions(config,type);
	}
	
	public static class ToStringOptions extends AbstractBeanOptions<GenerateToString> {

		public ToStringOptions(Configuration config,JType pojoType) {
			super(config,GenerateToString.class,pojoType);
		}
	}
}