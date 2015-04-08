package org.codemucker.jmutate.generate.builder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.expression.ExpressionParser;
import org.codemucker.jmatch.expression.StringMatcherBuilderCallback;
import org.codemucker.jmutate.ClashStrategyResolver;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.AbstractCodeGenerator;
import org.codemucker.jmutate.generate.CodeGenMetaGenerator;
import org.codemucker.jmutate.generate.GeneratorConfig;
import org.codemucker.jmutate.generate.pojo.PojoModel;
import org.codemucker.jmutate.generate.pojo.PojoProperty;
import org.codemucker.jmutate.generate.pojo.PropertiesExtractor;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.codemucker.jpattern.bean.Property;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateBuilder;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Generates per class builders
 */
public class BuilderGenerator extends AbstractCodeGenerator<GenerateBuilder> {

    private static final Logger LOG = LogManager.getLogger(BuilderGenerator.class);

    private final JMutateContext ctxt;
    private ClashStrategyResolver methodClashResolver;

	private final CodeGenMetaGenerator genInfo;

    @Inject
    public BuilderGenerator(JMutateContext ctxt) {
        this.ctxt = ctxt;
        this.genInfo = new CodeGenMetaGenerator(ctxt,getClass());
    }

	@Override
	public void generate(JType optionsDeclaredInNode, GeneratorConfig options) {
		BuilderModel model = new BuilderModel(optionsDeclaredInNode, options);
        
		
		ClashStrategy methodClashDefaultStrategy = model.getClashStrategy();
		methodClashResolver = new OnlyReplaceMyManagedMethodsResolver(methodClashDefaultStrategy);
		
        extractAllProperties(optionsDeclaredInNode, model);
        
        //TODO:enable builder creation for 3rd party compiled classes
        generateBuilder(optionsDeclaredInNode, model);
	}

	private void extractAllProperties(JType optionsDeclaredInNode,BuilderModel model) {
		LOG.debug("adding properties to Builder for " + model.getPojoType().getFullName());
		
		Matcher<String> fieldMatcher = fieldMatcher(model.getFieldNames());
		
		PropertiesExtractor extractor = PropertiesExtractor.with(ctxt.getResourceLoader(), ctxt.getParser())
			.includeCompiledClasses(true)
			.propertyNameMatcher(fieldMatcher)
			.includeSuperClass(model.isInheritSuperBeanBuilder())
			.build();
		
		PojoModel pojo = extractor.extractProperties(optionsDeclaredInNode);
		
		boolean fromSuper = false;
		while(pojo != null){	
			for(PojoProperty p:pojo.getDeclaredProperties()){				
				if(p.hasField() || !p.isReadOnly()){
					BuilderPropertyModel p2 = new BuilderPropertyModel(model, p, fromSuper);
					
					model.addProperty(p2);
				} else {
					LOG.debug("ignoring readonly property: " + p.getPropertyName());
					
				}
			}
			pojo = pojo.getParent();
			fromSuper = true;
		}
	}

	private Matcher<String> fieldMatcher(String s){
		if(Strings.isNullOrEmpty(s)){
			return AString.equalToAnything();
		}
		return ExpressionParser.parse(s, new StringMatcherBuilderCallback());
	}

	private void generateBuilder(JType optionsDeclaredInNode,BuilderModel model) {
		boolean markGenerated = model.isMarkGenerated();
		
		JSourceFile source = optionsDeclaredInNode.getSource();
		JType pojo = source.getMainType();
		JType builder;
		
		boolean isAbstract = model.isSupportSubclassing();

		if(pojo.getSimpleName().equals(model.getBuilderTypeSimpleRaw())){
			builder = pojo;
		} else{
			builder = pojo.getChildTypeWithNameOrNull(model.getBuilderTypeSimpleRaw());
			if(builder == null){
				SourceTemplate t = ctxt.newSourceTemplate()
					.var("typeBounds", model.getPojoType().getTypeBoundsOrNull())
					.var("type", model.getBuilderTypeSimpleRaw() + Strings.nullToEmpty(model.getBuilderTypeBoundsOrNull()))
					.var("modifier", (isAbstract ?"abstract":""))
					.pl("public static ${modifier} class ${type} { }")
				;
				
				pojo.asMutator(ctxt).addType(t.asResolvedTypeNodeNamed(null));
				builder = pojo.getChildTypeWithName(model.getBuilderTypeSimpleRaw());
				genInfo.addGeneratedMarkers(builder.asAbstractTypeDecl());
			}
		}
		//generate the with() method and aliases
		generateStaticBuilderCreateMethods(model, pojo);
		//TODO:builder ctor
		//TODO:builder clone/from method
		
		//add the self() method
		if(!"this".equals(model.getBuilderSelfAccessor())){
			SourceTemplate selfMethod = ctxt.newSourceTemplate()
				.var("selfType", model.getBuilderSelfType())
				.var("selfGetter", model.getBuilderSelfAccessor())
				
				.pl("protected ${selfType} ${selfGetter} { return (${selfType})this; }");		
			
			addMethod(builder, selfMethod.asMethodNodeSnippet(),markGenerated);	
		}
		
		for (BuilderPropertyModel property : model.getProperties()) {
			generateField(markGenerated, builder, property);
			generateSetter(model, markGenerated, builder, property);
			generateCollectionAddRemove(builder, model, property);
			generateMapAddRemove(builder, model, property);
		}
		
		generateAllArgCtor(pojo, model);
		generateBuildMethod(builder, model);
		
		writeToDiskIfChanged(source);
	}

	private void generateSetter(BuilderModel model, boolean markGenerated,JType builder, BuilderPropertyModel property) {

		SourceTemplate setterMethod = ctxt.newSourceTemplate()
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
		SourceTemplate field = ctxt.newSourceTemplate()
			.var("p.name", property.getPropertyName())
			.var("p.type", property.getType().getFullName())
			.pl("private ${p.type} ${p.name};");
			
			addField(builder, field.asFieldNodeSnippet(),markGenerated);
	}

	
	private void generateStaticBuilderCreateMethods(BuilderModel model,JType beanType) {
		if(model.isGenerateStaticBuilderMethod()){
			SourceTemplate t = ctxt
					.newSourceTemplate()
					.var("self.type", model.getBuilderTypeSimple())
					.var("typeBounds", model.getPojoType().getTypeBoundsOrEmpty());
		
			for (String name : model.getStaticBuilderMethodNames()) {
				addMethod(beanType,t.child().pl("public static ${typeBounds} ${self.type} " + name + " (){ return new ${self.type}(); }").asMethodNodeSnippet(),model.isMarkGenerated());
			}
		}
	}
	
	private void generateCollectionAddRemove(JType bean, BuilderModel model,BuilderPropertyModel property) {
		if(property.getType().isCollection() && model.isGenerateAddRemoveMethodsForIndexedProperties()){
			SourceTemplate add = ctxt
				.newSourceTemplate()
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
			
			SourceTemplate remove = ctxt
				.newSourceTemplate()
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
			SourceTemplate add = ctxt
				.newSourceTemplate()
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
			
			SourceTemplate remove = ctxt
				.newSourceTemplate()
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
			SourceTemplate buildMethod = ctxt
				.newSourceTemplate()
				.var("b.type", model.getPojoType().getSimpleName())
				.var("buildName", model.getBuildPojoMethodName())	
				.pl("public ${b.type} ${buildName}(){")
				.p("	return new ${b.type}(");
			
			boolean comma = false;
			for (BuilderPropertyModel property : model.getProperties()) {
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
			SourceTemplate beanCtor = ctxt
					.newSourceTemplate()
					.var("b.name", model.getPojoType().getSimpleNameRaw())
					.pl("private ${b.name} (");
			
			boolean comma = false;
			//args
			for (BuilderPropertyModel property : model.getProperties()) {
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
				//TODO:figure out if we can set field directly, via setter, or via ctor args
				//for now assume a setter
				if(property.isFromSuperClass()){
					beanCtor.pl(property.getPropertySetterName() + "(" + property.getPropertyName() + ");");
				} else {
					beanCtor.pl("this." + property.getPropertyName() + "=" + property.getPropertyName() + ";");
				}
				comma = true;
			}
			beanCtor.pl("}");
			addMethod(beanType, beanCtor.asConstructorNodeSnippet(),model.isMarkGenerated());
		}
	}
	
	private void addField(JType type, FieldDeclaration f, boolean markGenerated) {
		if(markGenerated){
			genInfo.addGeneratedMarkers(f);
		}
		ctxt
			.obtain(InsertFieldTransform.class)
			.clashStrategy(ClashStrategy.REPLACE)
			.target(type)
			.field(f)
			.transform();
	}
	
	private void addMethod(JType type, MethodDeclaration m,boolean markGenerated) {
		if(markGenerated){
			genInfo.addGeneratedMarkers(m);
		}
		ctxt
			.obtain(InsertMethodTransform.class)
			.clashStrategy(methodClashResolver)
			.target(type)
			.method(m)
			.transform();
	}

    private void writeToDiskIfChanged(JSourceFile source) {
        if (source != null) {
            cleanupImports(source.getAstNode());
            source = source.asMutator(ctxt).writeModificationsToDisk();
        }
    }

    private void cleanupImports(ASTNode node) {
        ctxt.obtain(CleanImportsTransform.class)
            .addMissingImports(true)
            .nodeToClean(node)
            .transform();
    }

	private class OnlyReplaceMyManagedMethodsResolver implements ClashStrategyResolver{

		private final ClashStrategy fallbackStrategy;
		
		public OnlyReplaceMyManagedMethodsResolver(ClashStrategy fallbackStrategy) {
			super();
			this.fallbackStrategy = fallbackStrategy;
		}

		@Override
		public ClashStrategy resolveClash(ASTNode existingNode,ASTNode newNode) {
			if(genInfo.isManagedByThis(JMethod.from(existingNode).getAnnotations())){
				return ClashStrategy.REPLACE;
			}
			return fallbackStrategy;
		}
		
	}

}