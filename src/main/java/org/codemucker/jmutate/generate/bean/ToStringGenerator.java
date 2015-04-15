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
		if(options.generateStringBuilderToString){
			generateStringBuilderToString(bean, model, options);
		} else {
			generateClassicToString(bean, model, options);
		}
	}

	private void generateClassicToString(JType bean, PojoModel model,ToStringOptions options) {
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
	
	private void generateStringBuilderToString(JType bean, PojoModel model,ToStringOptions options) {
		StringBuilder sb = new StringBuilder();
		if(model.hasAnyProperties()){
			boolean comma = false;
			for (PropertyModel property : model.getAllProperties()) {
				if(!(property.hasField() || property.hasGetter())){
					continue;
				}
				if(comma){
					sb.append(".append(',');\n");
				}
				sb.append("\tsb.append(").append(property.getName() ).append(")");
				sb.append(".append(").append(property.getInternalAccessor() ).append(")");
				comma = true;
			}
			sb.append(";");
		}
	
		SourceTemplate toString = newSourceTemplate()
			.pl("@java.lang.Override")
			.pl("public String toString(){")
			.pl("	java.lang.StringBuilder sb = new java.lang.StringBuilder();")
			.pl("	sb.append(this.getClass().getName()).append('@').append(System.identityHashCode(this)).append('[');")
			.pl("	toString(sb);")
			.pl("	sb.append(']');")
			.pl("	return sb.toString();")
			.pl("}");
	
		SourceTemplate toStringBuilder = newSourceTemplate()
			.pl("protected void toString(StringBuilder sb){")
			.pl(sb.toString())
			.pl("}");
		
		addMethod(bean, toString.asMethodNodeSnippet(),options.isMarkGenerated());
		addMethod(bean, toStringBuilder.asMethodNodeSnippet(),options.isMarkGenerated());
		
	}
	
	@Override
	protected ToStringOptions createOptionsFrom(Configuration config, JType type) {
		return new ToStringOptions(config,type);
	}
	
	public static class ToStringOptions extends AbstractBeanOptions<GenerateToString> {

		public boolean generateStringBuilderToString;
		
		public ToStringOptions(Configuration config,JType pojoType) {
			super(config,GenerateToString.class,pojoType);
		}
	}
}