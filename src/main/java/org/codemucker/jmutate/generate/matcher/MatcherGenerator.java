package org.codemucker.jmutate.generate.matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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
import org.codemucker.jmutate.ClashStrategy;
import org.codemucker.jmutate.ClashStrategyResolver;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateInfo;
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
import org.codemucker.jmutate.generate.AbstractGenerator;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.IsGenerated;
import org.codemucker.lang.BeanNameUtil;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.google.inject.Inject;

/**
 * Generates the matchers for pojos
 */
public class MatcherGenerator extends AbstractGenerator<GenerateMatchers> {

    private final Logger log = LogManager.getLogger(MatcherGenerator.class);

    private static final String CODE_GEN_INFO_CLASS_PKG = "org.codemucker.jmutate.generate.matcher.generated";
    private static final String CODE_GEN_INFO_CLASS_NAME = "CodeGeneration";
    private static final String CODE_GEN_INFO_CLASS_FULLNAME = CODE_GEN_INFO_CLASS_PKG + "." + CODE_GEN_INFO_CLASS_NAME;
    
    static final String VOWELS_UPPER = "AEIOU";
    
    private static final String THIS_GEN_NAME = MatcherGenerator.class.getSimpleName();
	
    private final Matcher<Annotation> reflectedAnnotationIgnore = AnAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    private final Matcher<JAnnotation> sourceAnnotationIgnore = AJAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    
    private final JMutateContext ctxt;
    private ClashStrategyResolver methodClashResolver;
    
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
    }

	@Override
	public void generate(JType optionsDeclaredInNode, GenerateMatchers options) {
		ClashStrategy methodClashDefaultStrategy = getOr(options.clashStrategy(),ClashStrategy.SKIP);
		methodClashResolver = new OnlyReplaceMyManagedMethodsResolver(methodClashDefaultStrategy);
		
		PojoScanner scanner = new PojoScanner(ctxt,optionsDeclaredInNode, options);
		AllMatchersModel models = createModel(optionsDeclaredInNode,options, scanner);
		generateMatchers(optionsDeclaredInNode, options, models);
	}

	private static <T> T getOr(T val, T defaultVal) {
        if (val == null) {
            return defaultVal;
        }
        return val;
    }
	private AllMatchersModel createModel(JType optionsDeclaredInNode,GenerateMatchers options, PojoScanner pojoScanner) {
		AllMatchersModel models = new AllMatchersModel(optionsDeclaredInNode, options);
        if(options.scanSources() ){
            FindResult<JType> requestTypes = pojoScanner.scanSources();
            // add the appropriate methods and types for each request bean
            for (JType requestType : requestTypes) {
                MatcherModel requestModel = new MatcherModel(models, requestType);
                extractFields(requestModel, requestType);
                models.addMatcher(requestModel);
            }
        }
        
        if(options.scanDependencies() && options.pojoDependencies().length > 0){
            FindResult<Class<?>> requestTypes = pojoScanner.scanForReflectedClasses();
            // add the appropriate methods and types for each request bean
            for (Class<?> requestType : requestTypes) {
                MatcherModel requestModel = new MatcherModel(models, requestType);
                extractFields(requestModel, requestType);
                models.addMatcher(requestModel);
            }
        }
        
        log.info("found " + models.matchers.size() + " matchers to generate ");
		return models;
	}

	private void generateMatchers(JType optionsDeclaredInNode,GenerateMatchers options, AllMatchersModel models) {
		createGenInfoIfNotExists();

		for (MatcherModel model : models.matchers) {
			JSourceFile source = newOrExistingMatcherSourceFile(model);
			JType matcher = source.getMainType();
			SourceTemplate template = ctxt.newSourceTemplate().var("selfType", model.matcherTypeSimple);

			// custom user builder factory methods
			for (String name : model.staticBuilderMethodNames) {
				addMethod(matcher,template.child().pl("public static ${selfType} " + name + " (){ return with(); }").asMethodNodeSnippet());
			}

			// standard builder factory method
			addMethod(matcher,template.child().pl("public static ${selfType} with(){ return new ${selfType}(); }").asMethodNodeSnippet());

			for (PropertyModel property : model.properties.values()) {
				//add default equals matchers for known types
				String equalMatcherSnippet = MATCHER_BY_TYPE.get(property.propertyTypeAsObject);
				if (equalMatcherSnippet != null) {
					SourceTemplate equalsMethod = template
						.child()
						.var("p.name", property.propertyName)
						.var("p.type", property.propertyTypeAsObject)
						.var("matcher", equalMatcherSnippet)
						
						.pl("public ${selfType} ${p.name}(final ${p.type} val){")
						.pl("		${p.name}(${matcher}(val)); ")
						.pl("		return this;")
						.pl("}");
					addMethod(matcher, equalsMethod.asMethodNodeSnippet());
				}
				//add the matcher method
				SourceTemplate matcherMethod = template
					.child()
					.var("p.name", property.propertyName)
					.var("p.type", property.propertyTypeAsObject)
					.var("p.type_raw", NameUtil.removeGenericPart(property.propertyTypeAsObject))
					.var("matcher", equalMatcherSnippet)
					
					.pl("public ${selfType} ${p.name}(final org.codemucker.jmatch.Matcher<? super ${p.type}> matcher){")
					.pl("		matchProperty('${p.name}',${p.type_raw}.class, matcher); ")
					.pl("		return this;")
					.pl("}")
					.singleToDoubleQuotes();
					
				addMethod(matcher, matcherMethod.asMethodNodeSnippet());
			}
			
			writeToDiskIfChanged(source);
		}
	}

    private JSourceFile newOrExistingMatcherSourceFile(MatcherModel model) {
    	log.debug("checking for source file for " + model.matcherTypeFull + "");
    	
    	String path = model.matcherTypeFull.replace('.', '/') + ".java";
    	RootResource sourceFile = ctxt.getDefaultGenerationRoot().getResource(path);
    	JSourceFile  source;
    	if(sourceFile.exists()){
    		log.debug("matcher source file " + path + " exists, loading");
    		source = JSourceFile.fromResource(sourceFile, ctxt.getParser());    
    	} else {
    		log.debug("creating new matcher source file " + path + "");
    		SourceTemplate t = ctxt.newSourceTemplate().pl("package " + model.pkg + ";").pl("");
            addGeneratedMarkers(t);
            t.pl("public class " + model.matcherTypeSimple + " extends  " + PropertyMatcher.class.getName() + "<" + model.pojoTypeFull + ">{}");
            source = t.asSourceFileSnippet();
    	}
    	
    	//add default ctor
        SourceTemplate ctor= ctxt.newSourceTemplate();
        ctor.pl("public " + model.matcherTypeSimple + "(){super(" + model.pojoTypeFull + ".class);}");
        addMethod(source.getMainType(),ctor.asConstructorNodeSnippet());
    	
    	return source;
    }

	private void addMethod(JType matcher, MethodDeclaration m) {
		AST ast = m.getAST();
		NormalAnnotation a = ast.newNormalAnnotation();
		a.setTypeName(ast.newName(IsGenerated.class.getName()));
		QualifiedName gen = ast.newQualifiedName(ast.newName(CODE_GEN_INFO_CLASS_FULLNAME), ast.newSimpleName(THIS_GEN_NAME));
		addValue(a,"generator",gen);
		//addValue(a,"sha1","1234");
		m.modifiers().add(0,a);
		
		ctxt
			.obtain(InsertMethodTransform.class)
			.clashStrategy(methodClashResolver)
			.target(matcher)
			.method(m)
			
			.transform();
	}

	private static void addValue(NormalAnnotation a,String name,String value){	
		StringLiteral lit = a.getAST().newStringLiteral();
		lit.setLiteralValue(value);
		addValue(a, name, lit);
	}
	
	private static void addValue(NormalAnnotation a,String name, Expression value){	
		MemberValuePair existing = null;
		for (Object v : a.values()) {
			if (v instanceof MemberValuePair) {
				MemberValuePair p = (MemberValuePair) v;
				if (name.equals(p.getName())) {
					existing = p;
					break;
				}
			}
		}
		if (existing != null) {
			existing.delete();
		}
		
		AST ast = a.getAST();
		MemberValuePair pair = ast.newMemberValuePair();
		pair.setName(ast.newSimpleName(name));
		
		pair.setValue(value);
		a.values().add(pair);
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

    private void extractFields(MatcherModel model, JType pojoType) {
        // call request builder methods for each field/method exposed
        FindResult<JField> fields = pojoType.findFieldsMatching(AJField.with().modifiers(AJModifier.that().isNotStatic().isNotNative()));
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

    private void addGeneratedMarkers(SourceTemplate template) {
        String genInfo = CODE_GEN_INFO_CLASS_FULLNAME + "." + THIS_GEN_NAME;
        template.var("generator", genInfo);
        template.pl("@" + javax.annotation.Generated.class.getName() + "(${generator})");
        template.pl("@" + IsGenerated.class.getName() + "(generator=${generator})");
    }

    private void createGenInfoIfNotExists() {
        RootResource resource = ctxt.getDefaultGenerationRoot().getResource(CODE_GEN_INFO_CLASS_FULLNAME + ".java");
        JSourceFile source;
        if (resource.exists()) {
        	source = JSourceFile.fromResource(resource, ctxt.getParser());
        } else {
           source = ctxt.newSourceTemplate()
                .var("pkg", CODE_GEN_INFO_CLASS_PKG)
                .var("className", CODE_GEN_INFO_CLASS_NAME)
                .pl("package ${pkg};")
                .pl("public class ${className} {}")
                .asSourceFileSnippet();
        }
        JField field = ctxt.newSourceTemplate()
                .pl("public static final String " + THIS_GEN_NAME + "=\"" + JMutateInfo.all + " " + getClass().getName() + "\";")
                .asJFieldSnippet();

        ctxt.obtain(InsertFieldTransform.class).target(source.getMainType()).field(field).clashStrategy(ClashStrategy.REPLACE).transform();

        writeToDiskIfChanged(source);
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
	
			JAnnotation anon = JMethod.from(existingNode).getAnnotations().find(AJAnnotation.with().fullName(IsGenerated.class)).getFirstOrNull();
			
			if(anon != null){
				String generator = anon.getValueForAttribute("generator", null);
				String hash = anon.getValueForAttribute("sha1", null);
				
				//TODO:match qualified name
				if(generator != null && generator.endsWith(THIS_GEN_NAME)){
					return ClashStrategy.REPLACE;
				}
			}
			return fallbackStrategy;
		}
		
	}

}