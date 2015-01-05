package org.codemucker.jmutate.generate.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.ReflectedClass;
import org.codemucker.jfind.ReflectedField;
import org.codemucker.jfind.matcher.AField;
import org.codemucker.jfind.matcher.AMethod;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.expression.ExpressionParser;
import org.codemucker.jmatch.expression.StringMatcherBuilderCallback;
import org.codemucker.jmutate.ClashStrategyResolver;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJModifier;
import org.codemucker.jmutate.generate.AbstractCodeGenerator;
import org.codemucker.jmutate.generate.CodeGenMetaGenerator;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateBuilder;
import org.codemucker.lang.BeanNameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Generates per class builders
 */
public class Generator extends AbstractCodeGenerator<GenerateBuilder> {

    private final Logger log = LogManager.getLogger(Generator.class);

    static final String VOWELS_UPPER = "AEIOU";
	
    private final Matcher<Annotation> reflectedAnnotationIgnore = AnAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    private final Matcher<JAnnotation> sourceAnnotationIgnore = AJAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    
    private final JMutateContext ctxt;
    private ClashStrategyResolver methodClashResolver;

	private final CodeGenMetaGenerator genInfo;

    @Inject
    public Generator(JMutateContext ctxt) {
        this.ctxt = ctxt;
        this.genInfo = new CodeGenMetaGenerator(ctxt,getClass());
    }

	@Override
	public void generate(JType optionsDeclaredInNode, GenerateBuilder options) {
		ClashStrategy methodClashDefaultStrategy = getOr(options.clashStrategy(),ClashStrategy.SKIP);
		methodClashResolver = new OnlyReplaceMyManagedMethodsResolver(methodClashDefaultStrategy);
		Matcher<String> fieldMatcher = fieldMatcher(options.fieldNames());
        BuilderModel model = new BuilderModel(optionsDeclaredInNode, options);
        extractFields(model, optionsDeclaredInNode, fieldMatcher);
        //TODO:enable builder creation for 3rd party compiled classes
        generateBuilder(optionsDeclaredInNode, model);
	}

	private static <T> T getOr(T val, T defaultVal) {
        if (val == null) {
            return defaultVal;
        }
        return val;
    }

	private Matcher<String> fieldMatcher(String s){
		if(Strings.isNullOrEmpty(s)){
			return AString.equalToAnything();
		}
		return ExpressionParser.parse(s, new StringMatcherBuilderCallback());
	}

	private void generateBuilder(JType optionsDeclaredInNode,BuilderModel model) {
		boolean markGenerated = model.markGenerated;
		
		JSourceFile source = optionsDeclaredInNode.getSource();
		JType mainType = source.getMainType();
		JType builder;
		if(mainType.getSimpleName().equals(model.builderTypeSimple)){
			builder = mainType;
		}else{
			builder = mainType.getChildTypeWithNameOrNull(model.builderTypeSimple);
			if(builder == null){
				mainType.asMutator(ctxt).addType("public static class " + model.builderTypeSimple + " {}");
				builder = mainType.getChildTypeWithName(model.builderTypeSimple);				
			}
		}
		
		SourceTemplate baseTemplate = ctxt.newSourceTemplate().var("selfType", model.builderTypeSimple);
		//factory method
		for (String name : model.staticBuilderMethodNames) {
			addMethod(mainType,baseTemplate.child().pl("public static ${selfType} " + name + " (){ return new ${selfType}(); }").asMethodNodeSnippet(),markGenerated);
		}
		//TODO:builder ctor
		//TODO:builder clone/from method
		
		//builder property setters
		for (PropertyModel property : model.properties.values()) {
			//add the field
			SourceTemplate field = baseTemplate
				.child()
				.var("p.name", property.propertyName)
				.var("p.type", property.propertyTypeAsObject)
				.pl("private ${p.type} ${p.name};");
				
				addField(builder, field.asFieldNodeSnippet(),markGenerated);
		
			//add the setter
			SourceTemplate setterMethod = baseTemplate
				.child()
				.var("p.name", property.propertyName)
				.var("p.type", property.propertyTypeAsObject)
				.var("p.type_raw", NameUtil.removeGenericPart(property.propertyTypeAsObject))
				
				.pl("public ${selfType} ${p.name}(final ${p.type} val){")
				.pl("		this.${p.name} = val;")
				.pl("		return this;")
				.pl("}");
				
			addMethod(builder, setterMethod.asMethodNodeSnippet(),markGenerated);
		}
		
		//bean all arg ctor
		{
			SourceTemplate beanCtor = baseTemplate
					.child()
					.var("b.name", model.pojoTypeSimple)
					.pl("private ${b.name} (");
			
			boolean comma = false;
			//args
			for (PropertyModel property : model.properties.values()) {
				if(comma){
					beanCtor.p(",");
				}
				beanCtor.p(property.propertyType + " " + property.propertyName);
				comma = true;
			}
			
			beanCtor.pl("){");
			//field assignments
			for (PropertyModel property : model.properties.values()) {
				beanCtor.pl("this." + property.propertyName + "=" + property.propertyName + ";");
				comma = true;
			}
			beanCtor.pl("}");
			addMethod(mainType, beanCtor.asConstructorNodeSnippet(),markGenerated);
		}
		//build method
		{
			SourceTemplate buildMethod = baseTemplate
					.child()
					.var("b.name", model.pojoTypeSimple)
					.var("buildName", model.buildMethodName)
					
					.pl("public ${b.name} ${buildName}(){")
					.p("	return new ${b.name}(");
				
			boolean comma = false;
			for (PropertyModel property : model.properties.values()) {
				if(comma){
					buildMethod.p(",");
				}
				buildMethod.p( "this." + property.propertyName);
				comma = true;
			}
			buildMethod.pl(");");
			buildMethod.pl("}");
			addMethod(builder, buildMethod.asMethodNodeSnippet(),markGenerated);
		}
		
		writeToDiskIfChanged(source);
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

	private void extractFields(BuilderModel model, Class<?> requestType) {
        ReflectedClass requestBean = ReflectedClass.from(requestType);
        FindResult<Field> fields = requestBean.findFieldsMatching(AField.that().isNotStatic().isNotNative());
        log.trace("found " + fields.toList().size() + " fields");
        for (Field f : fields) {
            ReflectedField field = ReflectedField.from(f);
            if (field.hasAnnotation(reflectedAnnotationIgnore)) {
                log("ignoring field:" + f.getName());
                continue;
            }
            PropertyModel property = new PropertyModel(model, f.getName(), f.getGenericType().getTypeName());

            String getterName = BeanNameUtil.toGetterName(field.getName(), field.getType());
            String getter = getterName + "()";
            if (!requestBean.hasMethodMatching(AMethod.with().name(getterName).numArgs(0))) {
                if (!field.isPublic()) {
                    //can't access field, lets skip
                	continue;
                }
                getter = field.getName();// direct field access
            }
            property.propertyGetter = getter;
            
            model.addField(property);
        }
    }

    private void extractFields(BuilderModel model, JType pojoType, Matcher<String> fieldMatcher) {
        // call request builder methods for each field/method exposed
        FindResult<JField> fields = pojoType.findFieldsMatching(AJField.with().modifiers(AJModifier.that().isNotStatic().isNotNative()).name(fieldMatcher));
        log("found " + fields.toList().size() + " fields");
        for (JField field: fields) {
            if (field.getAnnotations().contains(sourceAnnotationIgnore)) {
                log("ignoring field:" + field.getName());
                continue;
            }
            PropertyModel property = new PropertyModel(model, field.getName(), field.getFullTypeName());
            String getterName = BeanNameUtil.toGetterName(field.getName(), NameUtil.isBoolean(field.getFullTypeName()));
            String getter = getterName + "()";
            if (!pojoType.hasMethodMatching(AJMethod.with().name(getterName).numArgs(0))) {
                log("no method " + getter);
                if (!field.getJModifiers().isPublic()) {
                    //can't access field, lets skip
                	continue;
                }
                getter = field.getName();// direct field access
            }
            property.propertyGetter = getter;
            
            model.addField(property);
        }
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

    private void log(String msg) {
        log.debug(msg);
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