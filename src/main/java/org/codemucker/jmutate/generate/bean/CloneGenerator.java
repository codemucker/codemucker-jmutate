package org.codemucker.jmutate.generate.bean;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.generate.GenerateCloneMethod;
import org.codemucker.jpattern.generate.DisableGenerators;

import com.google.inject.Inject;

/**
 * Generates the 'clone' method on pojos
 */
public class CloneGenerator extends AbstractBeanGenerator<GenerateCloneMethod> {

	private static final Logger LOG = LogManager.getLogger(CloneGenerator.class);

	@Inject
	public CloneGenerator(JMutateContext ctxt) {
		super(ctxt);
	}
	
	protected void generate(JType bean, BeanModel model) {
		if(model.options.isGenerateCloneMethod() && !bean.isAbstract()){
			String methodName = model.options.getCloneMethodName();
			
			LOG.debug("adding method '" + methodName + "'");

			SourceTemplate clone = newSourceTemplate()
				.var("method.name", methodName)
				.var("b.type", model.options.getType().getSimpleName())
				.var("b.genericPart", model.options.getType().getGenericPartOrEmpty())
				.var("b.typeBounds", model.options.getType().getTypeBoundsOrEmpty())
				
				
				.pl("public static ${b.typeBounds} ${b.type} ${method.name}(${b.type} bean){")
				.pl("if(bean == null){ return null;}")
				.pl("final ${b.type} clone = new ${b.type}();");
			
			for (BeanPropertyModel property : model.getProperties()) {
				if(property.isFromSuperClass() && !property.hasSetter() && !property.hasGetter()){
					continue;
				}
				if(!property.hasField() && (!property.hasSetter() || !property.hasGetter())){
					continue;
				}
				
				SourceTemplate t = clone
					.child()
				//	.var("p.name",property.getPropertyName())
					.var("f.name",property.getFieldName())
					.var("p.getter",property.getPropertyGetterName())
					.var("p.setter",property.getPropertySetterName())
					.var("p.type",property.getType().getFullName())
					.var("p.concreteType",property.getPropertyConcreteType())
					.var("p.rawType",property.getType().getFullNameRaw())
					.var("p.genericPart",property.getType().getGenericPartOrEmpty());
				
				if(property.getType().isPrimitive()){
					if(property.hasSetter() || property.hasField()){
						if(property.isFromSuperClass()){
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
					if(property.isFromSuperClass()){
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
					if(property.isFromSuperClass()){
						//TODO:add a safe copy util in here. codemucker.lang?
						t.pl("	clone.${p.setter}(bean.${p.getter}()} == null?null:new ${p.concreteType}${p.genericPart}(bean.${p.getter}());");
					} else {
						t.pl("	clone.${f.name} = bean.${f.name} == null?null:new ${p.concreteType}${p.genericPart}(bean.${f.name});");
					}
				} else {
			//		if(hasClassGotMethod(property.propertyTypeRaw, AString.matchingAntPattern("*newInstanceOf"))){
					//	t.pl("	clone.${f.name} = bean.${f.name} == null?null:${p.rawType}.newInstanceOf(bean.${f.name});");
				//	} else {
					if(property.isFromSuperClass()){
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
			
			addMethod(bean, clone.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}
	
	@Override
	protected GenerateCloneMethod getAnnotation() {
		return Defaults.class.getAnnotation(GenerateCloneMethod.class);
	}
	
	@DisableGenerators
	@GenerateCloneMethod
	private static class Defaults {}

}