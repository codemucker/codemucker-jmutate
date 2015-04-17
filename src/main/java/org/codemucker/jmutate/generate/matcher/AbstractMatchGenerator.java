package org.codemucker.jmutate.generate.matcher;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ClashStrategyResolver;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.generate.AbstractCodeGenerator;
import org.codemucker.jmutate.generate.CodeGenMetaGenerator;
import org.codemucker.jmutate.generate.ReplaceOnlyOwnedNodesResolver;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModel;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public abstract class AbstractMatchGenerator<T extends Annotation,TOptions extends AbstractMatcherModel<T>> extends AbstractCodeGenerator<T>  {

	private static final Logger LOG = LogManager.getLogger(AbstractMatchGenerator.class);	
    static final String VOWELS_UPPER = "AEIOU";

	private static Map<String, String> EQUAL_TO_MATCHERS_BY_TYPE = new HashMap<String, String>();
	private  Map<String, String> customEqualToMatchersByType = new HashMap<String, String>();

	static {
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.String", "org.codemucker.jmatch.AString.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.Integer", "org.codemucker.jmatch.AnInt.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.Boolean", "org.codemucker.jmatch.ABool.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.Short", "org.codemucker.jmatch.AnInt.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.Character", "org.codemucker.jmatch.AChar.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.Float", "org.codemucker.jmatch.AFloat.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.Double", "org.codemucker.jmatch.ADouble.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.Long", "org.codemucker.jmatch.ALong.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.lang.Byte", "org.codemucker.jmatch.AByte.equalTo");
		EQUAL_TO_MATCHERS_BY_TYPE.put("java.util.Date", "org.codemucker.jmatch.ADate.equalTo");
	}

    protected final Matcher<Annotation> reflectedAnnotationIgnore = AnAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    protected final Matcher<JAnnotation> sourceAnnotationIgnore = AJAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    
    protected final JMutateContext ctxt;
    protected ClashStrategyResolver methodClashResolver;

	protected final CodeGenMetaGenerator generatorMeta;

	private final Class<TOptions> optionsClass;
	private final Class<T> annotationType;
    
	private TOptions options;
	
    protected AbstractMatchGenerator(JMutateContext ctxt,Class<T> annotationType,Class<TOptions> optionsClass) {
        this.ctxt = ctxt;
        this.generatorMeta = new CodeGenMetaGenerator(ctxt,getClass());
        this.annotationType = annotationType;
        this.optionsClass = optionsClass;
    }
    
    @Override
	public final void generate(JType declaredInType, SmartConfig config) {
    	this.options = config.mapFromTo(annotationType,optionsClass);
    	if(!options.enabled){
			LOG.info("generator annotation marked as disabled, not running generation");
			return;
		}
    	methodClashResolver = new ReplaceOnlyOwnedNodesResolver(generatorMeta,options.clashStrategy);
		
    	generate(declaredInType,config,options);
    }
	
	protected abstract void generate(JType optionsDeclaredInNode, SmartConfig config,TOptions options);

	/**
	 * Add a custom snippet which will be used in generating match method.
	 * 
	 * @param forTypeFullName
	 * @param snippet
	 */
	protected void addEqualToMatcherSnippet(String forTypeFullName, String snippet){
		customEqualToMatchersByType.put(forTypeFullName, snippet);
	}
	
    public TypeModel toMatcherType(TypeModel pojoType){
    	String className = pojoType.getSimpleNameRaw();
    	
    	if (VOWELS_UPPER.indexOf(className.charAt(0)) != -1) {
			className = "An" + className;
		} else {
			className = "A" + className;
		}
		if (pojoType.getPkg() != null) {
			className = pojoType.getPkg() + "." + className;
		}
		//TODO:add type bounds!
		return new TypeModel(className, null);
    }
    

	protected void generateDefaultConstructor(JType matcher, TypeModel forType) {
		//add default ctor
        SourceTemplate ctor= ctxt.newSourceTemplate();
        ctor.pl("public " + matcher.getSimpleName() + "(){super(" + forType.getFullName() + ".class);}");
        addMethod(matcher,ctor.asConstructorNodeSnippet());
	}
    
    /**
     * Assumes typ subclasses PropertyMatcher
     * @param options
     * @param model
     * @param matcher
     */
	protected void generateMatcher(TOptions options,PojoModel model,JType matcher){
		SourceTemplate baseTemplate = ctxt.newSourceTemplate().var("selfType", matcher.getSimpleName());
		// custom user builder factory methods
		for (String name : options.staticBuilderMethodNames) {
			if(name != "with"){
				addMethod(matcher,baseTemplate.child().pl("public static ${selfType} " + name + " (){ return with(); }").asMethodNodeSnippet());
			}
		}

		// standard builder factory method
		addMethod(matcher,baseTemplate.child().pl("public static ${selfType} with(){ return new ${selfType}(); }").asMethodNodeSnippet());

		for (PropertyModel property : model.getAllProperties()) {
			//add default equals matchers for known types
			TypeModel propertyType = property.getType();
			
			String equalMatcherSnippet = getEqualToSnippetForTypeOrNull(propertyType.getObjectTypeFullName());
			if (equalMatcherSnippet != null) {
				SourceTemplate equalsMethod = baseTemplate
					.child()
					.var("p.name", property.getName())
					.var("p.type", propertyType.getFullName())
					.var("matcher", equalMatcherSnippet)
					
					.pl("public ${selfType} ${p.name}(final ${p.type} val){")
					.pl("		${p.name}(${matcher}(val)); ")
					.pl("		return this;")
					.pl("}");
				addMethod(matcher, equalsMethod.asMethodNodeSnippet());
			}
			//TODO:add automatic converter methods
			//e.g. long -> date; boolean->isX,isNotX; obj->isNull,isNotNull, String->isBlank,isEmpty,isNull
			
//			boolean isPrimitiveObject = NameUtil.isValueType(property.getType().getObjectTypeFullName());
//			boolean isPrimitive= NameUtil.isPrimitive((property.getType().getFullName()));
//			
			//add the matcher method
			
			SourceTemplate matcherMethod = baseTemplate
				.child()
				.var("p.name", property.getName())
				.var("p.type", propertyType.getObjectTypeFullName())
				.var("p.type_raw", propertyType.getObjectTypeFullNameRaw())
				.var("matcher", equalMatcherSnippet);
			
			if(propertyType.isPrimitive() || propertyType.isPrimitive() || propertyType.isString()){
				matcherMethod.pl("public ${selfType} ${p.name}(final org.codemucker.jmatch.Matcher<${p.type}> matcher){");
			} else {
				matcherMethod.pl("public ${selfType} ${p.name}(final org.codemucker.jmatch.Matcher<? super ${p.type}> matcher){");
				
			}
			matcherMethod.pl("		matchProperty('${p.name}',${p.type_raw}.class, matcher); ")
				.pl("		return this;")
				.pl("}")
				.singleToDoubleQuotes();
				
			addMethod(matcher, matcherMethod.asMethodNodeSnippet());
		}	
	}
	

	protected String getEqualToSnippetForTypeOrNull(TypeModel type){
		return getEqualToSnippetForTypeOrNull(type.getFullName());
	}

	
	protected String getEqualToSnippetForTypeOrNull(String fullTypeName){
		String snippet = customEqualToMatchersByType.get(fullTypeName);
		if(snippet==null){
			snippet = EQUAL_TO_MATCHERS_BY_TYPE.get(fullTypeName);
		}
		return snippet;
	}

	protected void addGeneratedMarkers(SourceTemplate template) {
    	generatorMeta.addGeneratedMarkers(template);
    }

    protected void writeToDiskIfChanged(JSourceFile source) {
        if (source != null) {
            cleanupImports(source.getAstNode());
            source = source.asMutator(ctxt).writeModificationsToDisk();
        }
    }

	protected void addMethod(JType matcher, MethodDeclaration m) {
		if(this.options.markGenerated){
			generatorMeta.addGeneratedMarkers(m);
		}
		ctxt
			.obtain(InsertMethodTransform.class)
			.clashStrategy(methodClashResolver)
			.target(matcher)
			.method(m)
			
			.transform();
	}

    protected void cleanupImports(ASTNode node) {
        ctxt.obtain(CleanImportsTransform.class)
            .addMissingImports(true)
            .nodeToClean(node)
            .transform();
    }



}

