package org.codemucker.jmutate.generate.matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.ReflectedClass;
import org.codemucker.jfind.ReflectedField;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.matcher.AField;
import org.codemucker.jfind.matcher.AMethod;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.PropertyMatcher;
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
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateMatchers;
import org.codemucker.lang.BeanNameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.inject.Inject;

/**
 * Generates the matchers for pojos
 */
public class MatcherGenerator extends AbstractCodeGenerator<GenerateMatchers> {

    private static final Logger log = LogManager.getLogger(MatcherGenerator.class);

    static final String VOWELS_UPPER = "AEIOU";
    
    private final Matcher<Annotation> reflectedAnnotationIgnore = AnAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    private final Matcher<JAnnotation> sourceAnnotationIgnore = AJAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    
    private final JMutateContext ctxt;
    private ClashStrategyResolver methodClashResolver;

	private final CodeGenMetaGenerator genInfo;
    
	private static Map<String, String> MATCHER_BY_TYPE = new HashMap<String, String>();

	static {
		MATCHER_BY_TYPE.put("java.lang.String", "org.codemucker.jmatch.AString.equalTo");
		MATCHER_BY_TYPE.put("java.lang.Integer", "org.codemucker.jmatch.AnInt.equalTo");
		MATCHER_BY_TYPE.put("java.lang.Boolean", "org.codemucker.jmatch.ABool.equalTo");
		MATCHER_BY_TYPE.put("java.lang.Short", "org.codemucker.jmatch.AShort.equalTo");
		//MATCHER_BY_TYPE.put("java.lang.Character", "org.codemucker.jmatch.AChar.equalTo");
		MATCHER_BY_TYPE.put("java.lang.Float", "org.codemucker.jmatch.AFloat.equalTo");
		MATCHER_BY_TYPE.put("java.lang.Double", "org.codemucker.jmatch.ADouble.equalTo");
		MATCHER_BY_TYPE.put("java.lang.Long", "org.codemucker.jmatch.ALong.equalTo");
		//MATCHER_BY_TYPE.put("java.lang.Byte", "org.codemucker.jmatch.AByte.equalTo");
		//MATCHER_BY_TYPE.put("java.util.Date", "org.codemucker.jmatch.ADate.equalTo");
	}

    @Inject
    public MatcherGenerator(JMutateContext ctxt) {
        this.ctxt = ctxt;
        this.genInfo = new CodeGenMetaGenerator(ctxt,getClass());
    }

	@Override
	public void generate(JType optionsDeclaredInNode, SmartConfig config) {
		AllMatchersModel models = new AllMatchersModel(optionsDeclaredInNode, config.getConfigFor(GenerateMatchers.class));
		methodClashResolver = new OnlyReplaceMyManagedMethodsResolver(models.getClashStrategy());
		
		findAndAddModels(optionsDeclaredInNode,models);
		generateMatchers(optionsDeclaredInNode, models);
	}

	private void findAndAddModels(JType optionsDeclaredInNode,AllMatchersModel models) {
		PojoSourceAndClassScanner pojoScanner = new PojoSourceAndClassScanner(
				ctxt.getResourceLoader(), 
				models.getPojoDependencies(), 
				models.getPojoNames(),
				models.getPojoTypes());
	    
		
        if(models.isScanSources() ){
            FindResult<JType> pojos = pojoScanner.scanSources();
            // add the appropriate methods and types for each request bean
            for (JType pojo : pojos) {
                MatcherModel requestModel = new MatcherModel(models, pojo);
                extractFields(requestModel, pojo);
                models.addMatcher(requestModel);
            }
        }
        
        if(models.isScanDependencies()){
            FindResult<Class<?>> pojos = pojoScanner.scanForReflectedClasses();
            // add the appropriate methods and types for each request bean
            for (Class<?> pojo : pojos) {
                MatcherModel requestModel = new MatcherModel(models, pojo);
                extractFields(requestModel, pojo);
                models.addMatcher(requestModel);
            }
        }
        
        log.info("found " + models.getMatchers().size() + " matchers to generate from source and compiled classes");
	}

	private void generateMatchers(JType optionsDeclaredInNode,AllMatchersModel models) {
		for (MatcherModel model : models.getMatchers()) {
			boolean markGenerated = model.isMarkGenerated();
			JSourceFile source = newOrExistingMatcherSourceFile(model);
			if(source.getResource().exists() && !model.isKeepInSync()){
				continue;//skip this generation
			}
			JType matcher = source.getMainType();
			SourceTemplate baseTemplate = ctxt.newSourceTemplate().var("selfType", model.getMatcherTypeSimple());

			// custom user builder factory methods
			for (String name : model.getStaticBuilderMethodNames()) {
				addMethod(matcher,baseTemplate.child().pl("public static ${selfType} " + name + " (){ return with(); }").asMethodNodeSnippet(),markGenerated);
			}

			// standard builder factory method
			addMethod(matcher,baseTemplate.child().pl("public static ${selfType} with(){ return new ${selfType}(); }").asMethodNodeSnippet(),markGenerated);

			for (MatcherPropertyModel property : model.getProperties().values()) {
				//add default equals matchers for known types
				String equalMatcherSnippet = MATCHER_BY_TYPE.get(property.propertyTypeAsObject);
				if (equalMatcherSnippet != null) {
					SourceTemplate equalsMethod = baseTemplate
						.child()
						.var("p.name", property.getPropertyName())
						.var("p.type", property.propertyTypeAsObject)
						.var("matcher", equalMatcherSnippet)
						
						.pl("public ${selfType} ${p.name}(final ${p.type} val){")
						.pl("		${p.name}(${matcher}(val)); ")
						.pl("		return this;")
						.pl("}");
					addMethod(matcher, equalsMethod.asMethodNodeSnippet(),markGenerated);
				}
				//add the matcher method
				SourceTemplate matcherMethod = baseTemplate
					.child()
					.var("p.name", property.getPropertyName())
					.var("p.type", property.propertyTypeAsObject)
					.var("p.type_raw", NameUtil.removeGenericOrArrayPart(property.propertyTypeAsObject))
					.var("matcher", equalMatcherSnippet)
					
					.pl("public ${selfType} ${p.name}(final org.codemucker.jmatch.Matcher<? super ${p.type}> matcher){")
					.pl("		matchProperty('${p.name}',${p.type_raw}.class, matcher); ")
					.pl("		return this;")
					.pl("}")
					.singleToDoubleQuotes();
					
				addMethod(matcher, matcherMethod.asMethodNodeSnippet(),markGenerated);
			}
			
			writeToDiskIfChanged(source);
		}
	}

    private JSourceFile newOrExistingMatcherSourceFile(MatcherModel model) {
    	log.debug("checking for source file for " + model.getMatcherTypeFull() + "");
    	
    	String path = model.getMatcherTypeFull().replace('.', '/') + ".java";
    	RootResource sourceFile = ctxt.getDefaultGenerationRoot().getResource(path);
    	JSourceFile  source;
    	if(sourceFile.exists()){
    		log.debug("matcher source file " + path + " exists, loading");
    		source = JSourceFile.fromResource(sourceFile, ctxt.getParser());    
    	} else {
    		log.debug("creating new matcher source file " + path + "");
    		SourceTemplate t = ctxt.newSourceTemplate().pl("package " + model.getPkg() + ";").pl("");
            addGeneratedMarkers(t);
            t.pl("public class " + model.getMatcherTypeSimple() + " extends  " + PropertyMatcher.class.getName() + "<" + model.getPojoTypeFull() + ">{}");
            source = t.asSourceFileSnippet();
    	}
    	
    	//add default ctor
        SourceTemplate ctor= ctxt.newSourceTemplate();
        ctor.pl("public " + model.getMatcherTypeSimple() + "(){super(" + model.getPojoTypeFull() + ".class);}");
        addMethod(source.getMainType(),ctor.asConstructorNodeSnippet(),model.isMarkGenerated());
    	
    	return source;
    }

	private void addMethod(JType matcher, MethodDeclaration m, boolean markGenerated) {
		if(markGenerated){
			genInfo.addGeneratedMarkers(m);
		}
		ctxt
			.obtain(InsertMethodTransform.class)
			.clashStrategy(methodClashResolver)
			.target(matcher)
			.method(m)
			
			.transform();
	}

	private void extractFields(MatcherModel model, Class<?> requestType) {
        ReflectedClass requestBean = ReflectedClass.from(requestType);
        FindResult<Field> fields = requestBean.findFieldsMatching(AField.that().isNotStatic().isNotNative());
        log.trace("found " + fields.toList().size() + " fields");
        for (Field f : fields) {
            ReflectedField field = ReflectedField.from(f);
            if (field.hasAnnotation(reflectedAnnotationIgnore)) {
                log("ignoring field:" + f.getName());
                continue;
            }
            MatcherPropertyModel property = new MatcherPropertyModel(model, f.getName(), f.getGenericType().getTypeName());

            String getterName = BeanNameUtil.toGetterName(field.getName(), field.getType());
            String getter = getterName + "()";
            if (!requestBean.hasMethodMatching(AMethod.with().name(getterName).numArgs(0))) {
                if (!field.isPublic()) {
                    //can't access field, lets skip
                	continue;
                }
                getter = field.getName();// direct field access
            }
            property.setPropertyGetter(getter);
            
            model.addField(property);
        }
    }

    private void extractFields(MatcherModel model, JType pojoType) {
        // call request builder methods for each field/method exposed
        FindResult<JField> fields = pojoType.findFieldsMatching(AJField.with().modifier(AJModifier.that().isNotStatic().isNotNative()));
        log("found " + fields.toList().size() + " fields");
        for (JField field: fields) {
            if (field.getAnnotations().contains(sourceAnnotationIgnore)) {
                log("ignoring field:" + field.getName());
                continue;
            }
            MatcherPropertyModel property = new MatcherPropertyModel(model, field.getName(), field.getFullTypeName());
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
            property.setPropertyGetter(getter);
            
            model.addField(property);
        }
    }

    private void addGeneratedMarkers(SourceTemplate template) {
    	genInfo.addGeneratedMarkers(template);
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