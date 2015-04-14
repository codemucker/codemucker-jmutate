package org.codemucker.jmutate.generate.bean;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.bean.CloneGenerator.CloneOptions;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModel;
import org.codemucker.jpattern.generate.GenerateCloneMethod;

import com.google.inject.Inject;

/**
 * Generates the 'clone' method on pojos
 */
public class CloneGenerator extends AbstractBeanGenerator<GenerateCloneMethod,CloneOptions> {

	private static final Logger LOG = LogManager.getLogger(CloneGenerator.class);

	@Inject
	public CloneGenerator(JMutateContext ctxt) {
		super(ctxt,GenerateCloneMethod.class);
	}
	
	protected void generate(JType bean, SmartConfig config, PojoModel model,CloneOptions options) {
		
		if(options.isEnabled() && !bean.isAbstract()){
			String methodName = options.methodName;
			
			LOG.debug("adding method '" + methodName + "'");

			SourceTemplate clone = newSourceTemplate()
				.var("method.name", methodName)
				.var("b.type", options.getType().getSimpleName())
				.var("b.genericPart", options.getType().getGenericPartOrEmpty())
				.var("b.typeBounds", options.getType().getTypeBoundsOrEmpty())
				
				
				.pl("public static ${b.typeBounds} ${b.type} ${method.name}(${b.type} bean){")
				.pl("if(bean == null){ return null;}")
				.pl("final ${b.type} clone = new ${b.type}();");
			
			for (PropertyModel property : model.getAllProperties()) {
				if(property.isSuperClassProperty() && !property.hasSetter() && !property.hasGetter()){
					continue;
				}
				if(!property.hasField() && (!property.hasSetter() || !property.hasGetter())){
					continue;
				}
				
				SourceTemplate t = clone
					.child()
				//	.var("p.name",property.getPropertyName())
					.var("f.name",property.getFieldName())
					.var("p.getter",property.getGetterName())
					.var("p.setter",property.getSetterName())
					.var("p.type",property.getType().getFullName())
					.var("p.concreteType",property.getConcreteType())
					.var("p.rawType",property.getType().getFullNameRaw())
					.var("p.genericPart",property.getType().getGenericPartOrEmpty());
				
				if(property.getType().isPrimitive()){
					if(property.hasSetter() || property.hasField()){
						if(property.isSuperClassProperty()){
							t.pl("	clone.${p.setter}(bean.${p.getter}());");
						} else {
							if(property.hasField()){
								t.pl("	clone.${f.name} = bean.${f.name};");
							} else {
								t.pl("	clone.${p.setter}(bean.${p.getter}());");
							}
						}
					}
				} else if(property.getType().isArray()){
					if(property.isSuperClassProperty()){
						t.pl("	if(bean.${p.getter}() == null){");
						t.pl("		clone.${p.setter}(null);");
						t.pl("	} else {");
						t.pl("		${p.rawType}[] src = bean.${p.getter}();");
						t.pl("		${p.rawType}[] copy = new ${p.rawType}[src.length];");
						t.pl("		System.arraycopy(src,0,copy,0,src.length);");
						t.pl("		clone.${p.setter}(copy);");
						t.pl("	}");
					} else {
						t.pl("	if(bean.${f.name} == null){");
						t.pl("		clone.${f.name} = null;");
						t.pl("	} else {");
						t.pl("		clone.${f.name} = new ${p.rawType}[bean.${f.name}.length];");
						t.pl("		System.arraycopy(bean.${f.name},0,clone.${f.name},0,bean.${f.name}.length);");
						t.pl("	}");
					}
				} else if(property.getType().isIndexed()){
					if(property.isSuperClassProperty()){
						//TODO:add a safe copy util in here. codemucker.lang?
						t.pl("	clone.${p.setter}(bean.${p.getter}()} == null?null:new ${p.concreteType}${p.genericPart}(bean.${p.getter}());");
					} else {
						t.pl("	clone.${f.name} = bean.${f.name} == null?null:new ${p.concreteType}${p.genericPart}(bean.${f.name});");
					}
				} else {
			//		if(hasClassGotMethod(property.propertyTypeRaw, AString.matchingAntPattern("*newInstanceOf"))){
					//	t.pl("	clone.${f.name} = bean.${f.name} == null?null:${p.rawType}.newInstanceOf(bean.${f.name});");
				//	} else {
					if(property.isSuperClassProperty()){
						t.pl("	clone.${p.setter}(bean.${p.getter}());");
					} else {
						t.pl("	clone.${f.name} = bean.${f.name};");
					}
					//}
				}
						
				clone.add(t);
			}
			clone.pl("return clone;");
		
			clone.pl("}");
			
			addMethod(bean, clone.asMethodNodeSnippet(),options.isMarkGenerated());
		}
	}
	
	protected CloneOptions createOptionsFrom(Configuration config, JType type){
		return new CloneOptions(config,type);
	}
	

	public static class CloneOptions extends AbstractBeanOptions<GenerateCloneMethod> {

		public String methodName;
		
		public CloneOptions(Configuration config,JType type) {
			super(config,GenerateCloneMethod.class,type);
		}
	}
}