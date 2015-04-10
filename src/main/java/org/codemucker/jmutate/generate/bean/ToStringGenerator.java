package org.codemucker.jmutate.generate.bean;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.generate.GenerateToStringMethod;
import org.codemucker.jpattern.generate.DisableGenerators;

import com.google.inject.Inject;

/**
 * Generates the 'toString' method on pojos
 */
public class ToStringGenerator extends AbstractBeanGenerator<GenerateToStringMethod> {

	private static final Logger LOG = LogManager.getLogger(ToStringGenerator.class);

	@Inject
	public ToStringGenerator(JMutateContext ctxt) {
		super(ctxt);
	}
	
	protected void generate(JType bean, BeanModel model) {
		if(model.options.isGenerateToString()){
			StringBuilder sb = new StringBuilder();
			sb.append("\" [");
			if(model.getProperties().isEmpty()){
				sb.append("]\"");
			} else {
				boolean comma = false;
				for (BeanPropertyModel property : model.getProperties()) {
					if(!(property.hasField() || property.hasGetter())){
						continue;
					}
					if(comma){
						sb.append(" + \",");
					}
					sb.append(property.getPropertyName() ).append("=\" + ");
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
			addMethod(bean, toString.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}
	
	@Override
	protected GenerateToStringMethod getAnnotation() {
		return Defaults.class.getAnnotation(GenerateToStringMethod.class);
	}
	
	@DisableGenerators
	@GenerateToStringMethod
	private static class Defaults {}
}