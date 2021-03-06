package org.codemucker.jmutate.generate.builder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.AbstractGenerator;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModelExtractor;
import org.codemucker.jpattern.bean.Property;
import org.codemucker.jpattern.generate.GenerateBuilder;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Generates per class builders
 */
public class BuilderGenerator extends AbstractGenerator<GenerateBuilder> {

    private static final Logger LOG = LogManager.getLogger(BuilderGenerator.class);

    @Inject
    public BuilderGenerator(JMutateContext ctxt) {
        super(ctxt);
    }

	@Override
	public void generate(JType optionsDeclaredInNode, SmartConfig config) {
		BuilderOptions opts = config.mapFromTo(GenerateBuilder.class, BuilderOptions.class);
		BuilderModel model = new BuilderModel(optionsDeclaredInNode, opts);
        
		setClashStrategy(model.getClashStrategy());
		
        extractAllProperties(optionsDeclaredInNode, model);
        
        //TODO:enable builder creation for 3rd party compiled classes
        generateBuilder(optionsDeclaredInNode, model);
	}

	private void extractAllProperties(JType optionsDeclaredInNode,BuilderModel model) {
		LOG.debug("adding properties to Builder for " + model.getPojoType().getFullName());
		
		PropertyModelExtractor extractor = getContext().obtain(PropertyModelExtractor.Builder.class)
			.includeCompiledClasses(true)
			.propertyNameMatching(model.getFieldNames())
			.includeSuperClass(model.isInheritSuperBeanBuilder())
			.build();
		
		PojoModel pojo = extractor.extractModelFromClass(optionsDeclaredInNode);
		
		boolean fromSuper = false;
		while(pojo != null){	
			for(PropertyModel p:pojo.getDeclaredProperties()){	
				BuilderPropertyModel p2 = new BuilderPropertyModel(model, p, fromSuper);
				if(p2.isWriteable()){
					model.addProperty(p2);
				} else {
					LOG.debug("ignoring readonly property: " + p.getName());
				}
			}
			pojo = pojo.getParent();
			fromSuper = true;
		}
	}

	private void generateBuilder(JType optionsDeclaredInNode,BuilderModel model) {
		boolean markGenerated = model.isMarkGenerated();
		
		JSourceFile source = optionsDeclaredInNode.getCompilationUnit().getSource();
		JType pojo = source.getMainType();
		JType builder;
		
		boolean isAbstract = model.isSupportSubclassing();

		if(pojo.getSimpleName().equals(model.getBuilderTypeSimpleRaw())){
			builder = pojo;
		} else{
			builder = pojo.getChildTypeWithNameOrNull(model.getBuilderTypeSimpleRaw());
			if(builder == null){
				SourceTemplate t = newSourceTemplate()
					.var("typeBounds", model.getPojoType().getTypeBoundsOrNull())
					.var("type", model.getBuilderTypeSimpleRaw() + Strings.nullToEmpty(model.getBuilderTypeBoundsOrNull()))
					.var("modifier", (isAbstract ?"abstract":""))
					.pl("public static ${modifier} class ${type} { }")
				;
				
				pojo.asMutator(getContext()).addType(t.asResolvedTypeNodeNamed(null));
				builder = pojo.getChildTypeWithName(model.getBuilderTypeSimpleRaw());
				getGeneratorMeta().addGeneratedMarkers(builder.asAbstractTypeDecl());
			}
		}
		//generate the with() method and aliases
		generateStaticBuilderCreateMethods(model, pojo);
		//TODO:builder ctor
		//TODO:builder clone/from method
		
		//add the self() method
		if(!"this".equals(model.getBuilderSelfAccessor())){
			SourceTemplate selfMethod = newSourceTemplate()
				.var("selfType", model.getBuilderSelfType())
				.var("selfGetter", model.getBuilderSelfAccessor())
				
				.pl("protected ${selfType} ${selfGetter} { return (${selfType})this; }");		
			
			addMethod(builder, selfMethod.asMethodNodeSnippet(),markGenerated);	
		}
		
		for (BuilderPropertyModel property : model.getProperties()) {
			if(property.isWriteable()){
				generateField(markGenerated, builder, property);
				generateSetter(model, markGenerated, builder, property);
				generateCollectionAddRemove(builder, model, property);
				generateMapAddRemove(builder, model, property);
			}
		}
		
		generateAllArgCtor(pojo, model);
		generateBuildMethod(builder, model);
		
		writeToDiskIfChanged(source);
	}

	private void generateSetter(BuilderModel model, boolean markGenerated,JType builder, BuilderPropertyModel property) {

		SourceTemplate setterMethod = newSourceTemplate()
			.var("p.name", property.getPropertyName())
			.var("p.type", property.getType().getFullName())
			.var("p.type_raw", property.getType().getFullNameRaw())
			.var("self.type", model.getBuilderSelfType())
			.var("self.getter", model.getBuilderSelfAccessor())
			
			.pl("public ${self.type} ${p.name}(final ${p.type} val){")
			.pl("	this.${p.name} = val;")
			.pl("	return ${self.getter};")
			.pl("}");
			
		addMethod(builder, setterMethod.asMethodNodeSnippet(),markGenerated);
	}

	private void generateField(boolean markGenerated, JType builder,BuilderPropertyModel property) {
		SourceTemplate field = newSourceTemplate()
			.var("p.name", property.getPropertyName())
			.var("p.type", property.getType().getFullName())
			.pl("private ${p.type} ${p.name};");
			
			addField(builder, field.asFieldNodeSnippet(),markGenerated);
	}

	
	private void generateStaticBuilderCreateMethods(BuilderModel model,JType beanType) {
		if(model.isGenerateStaticBuilderMethod()){
			SourceTemplate t = newSourceTemplate()
					.var("self.type", model.getBuilderTypeSimple())
					.var("typeBounds", model.getPojoType().getTypeBoundsOrEmpty());
		
			for (String name : model.getStaticBuilderMethodNames()) {
				addMethod(beanType,t.child().pl("public static ${typeBounds} ${self.type} " + name + " (){ return new ${self.type}(); }").asMethodNodeSnippet(),model.isMarkGenerated());
			}
		}
	}
	
	private void generateCollectionAddRemove(JType bean, BuilderModel model,BuilderPropertyModel property) {
		if(property.getType().isCollection() && model.isGenerateAddRemoveMethodsForIndexedProperties()){
			SourceTemplate add = newSourceTemplate()
				.var("p.name", property.getPropertyName())
				.var("p.addName", property.getPropertyAddName())
				.var("p.type", property.getType().getObjectTypeFullName())
				.var("p.newType", property.getPropertyConcreteType())
				.var("p.genericPart", property.getType().getGenericPartOrEmpty())
				.var("p.valueType", property.getType().getIndexedValueTypeNameOrNull())
				.var("self.type", model.getBuilderSelfType())
				.var("self.getter", model.getBuilderSelfAccessor())
				
				.pl("public ${self.type} ${p.addName}(final ${p.valueType} val){")
				.pl("	if(this.${p.name} == null){ ")
				.pl("		this.${p.name} = new ${p.newType}${p.genericPart}(); ")
				.pl("	}")	
				.pl("	this.${p.name}.add(val);")
				.pl("	return ${self.getter};")
				.pl("}");
				
			addMethod(bean, add.asMethodNodeSnippet(),model.isMarkGenerated());
			
			SourceTemplate remove = newSourceTemplate()
				.var("p.name", property.getPropertyName())
				.var("p.removeName", property.getPropertyRemoveName())
				.var("p.type", property.getType().getObjectTypeFullName())
				.var("p.newType", property.getPropertyConcreteType())
				.var("p.valueType", property.getType().getIndexedValueTypeNameOrNull())
				.var("self.type", model.getBuilderSelfType())
				.var("self.getter", model.getBuilderSelfAccessor())
				
				.pl("public ${self.type} ${p.removeName}(final ${p.valueType} val){")
				.pl("	if(this.${p.name} != null){ ")
				.pl("		this.${p.name}.remove(val);")
				.pl("	}")
				.pl("	return ${self.getter};")
				.pl("}");
				
			addMethod(bean, remove.asMethodNodeSnippet(),model.isMarkGenerated());
		}
	}
	
	private void generateMapAddRemove(JType bean, BuilderModel model,BuilderPropertyModel property) {
		if(property.getType().isMap() && model.isGenerateAddRemoveMethodsForIndexedProperties()){
			SourceTemplate add = newSourceTemplate()
				.var("p.name", property.getPropertyName())
				.var("p.addName", property.getPropertyAddName())
				.var("p.type", property.getType().getObjectTypeFullName())
				.var("p.newType", property.getPropertyConcreteType())
				.var("p.genericPart", property.getType().getGenericPartOrEmpty())
				.var("p.keyType", property.getType().getIndexedKeyTypeNameOrNull())
				.var("p.valueType", property.getType().getIndexedValueTypeNameOrNull())
				.var("self.type", model.getBuilderSelfType())
				.var("self.getter", model.getBuilderSelfAccessor())
					
				.pl("public ${self.type} ${p.addName}(final ${p.keyType} key,final ${p.valueType} val){")
				.pl("	if(this.${p.name} == null){ this.${p.name} = new ${p.newType}${p.genericPart}(); }")
				.pl("	this.${p.name}.put(key, val);")
				.pl("	return ${self.getter};")
				.pl("}");
				
			addMethod(bean, add.asMethodNodeSnippet(),model.isMarkGenerated());
			
			SourceTemplate remove = newSourceTemplate()
				.var("p.name", property.getPropertyName())
				.var("p.removeName", property.getPropertyRemoveName())
				.var("p.type", property.getType().getObjectTypeFullName())
				.var("p.newType", property.getPropertyConcreteType())
				.var("p.keyType", property.getType().getIndexedKeyTypeNameOrNull())
				.var("self.type", model.getBuilderSelfType())
				.var("self.getter", model.getBuilderSelfAccessor())
				
				.pl("public ${self.type} ${p.removeName}(final ${p.keyType} key){")
				.pl("	if(this.${p.name} != null){ ")
				.pl("		this.${p.name}.remove(key);")
				.pl("	}")
				.pl("	return ${self.getter};")
				.pl("}");
				
			addMethod(bean, remove.asMethodNodeSnippet(),model.isMarkGenerated());
		}
	}

	private void generateBuildMethod(JType builder,BuilderModel model) {
		if(!model.isSupportSubclassing()){
			SourceTemplate buildMethod = newSourceTemplate()
				.var("b.type", model.getPojoType().getSimpleName())
				.var("buildName", model.getBuildPojoMethodName())	
				.pl("public ${b.type} ${buildName}(){")
				.p("	return new ${b.type}(");
			
			boolean comma = false;
			for (BuilderPropertyModel property : model.getProperties()) {
				if(property.isReadOnly()){
					continue;
				}
				if(comma){
					buildMethod.p(",");
				}
				buildMethod.p( "this." + property.getPropertyName());
				comma = true;
			}
			buildMethod.pl(");");
			buildMethod.pl("}");
			addMethod(builder, buildMethod.asMethodNodeSnippet(),model.isMarkGenerated());
		}
	}

	private void generateAllArgCtor(JType beanType, BuilderModel model) {
		if(!beanType.isAbstract()){
			SourceTemplate beanCtor = newSourceTemplate()
					.var("b.name", model.getPojoType().getSimpleNameRaw())
					.pl("private ${b.name} (");
			
			boolean comma = false;
			//args
			for (BuilderPropertyModel property : model.getProperties()) {
				if(property.isReadOnly()){
					continue;
				}
				if(comma){
					beanCtor.p(",");
				}
				if(model.isMarkCtorArgsAsProperties()){
					beanCtor.p("@" + Property.class.getName() + "(name=\"" + property.getPropertyName() + "\") ");
				}
				beanCtor.p(property.getType().getFullName() + " " + property.getPropertyName());
				comma = true;
			}
			
			beanCtor.pl("){");
			//field assignments
			for (BuilderPropertyModel property : model.getProperties()) {
				if(property.isReadOnly()){
					continue;
				}
				//TODO:figure out if we can set field directly, via setter, or via ctor args
				//for now assume a setter
				if(property.isFromSuperClass()){
					beanCtor.pl(property.getPropertySetterName() + "(" + property.getPropertyName() + ");");
				} else {
					beanCtor.pl("this." + property.getFieldName() + "=" + property.getPropertyName() + ";");
				}
				comma = true;
			}
			beanCtor.pl("}");
			addMethod(beanType, beanCtor.asConstructorNodeSnippet(),model.isMarkGenerated());
		}
	}

}