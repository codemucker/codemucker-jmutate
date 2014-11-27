package org.codemucker.jmutate.generate.matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.cqrs.Path;
import org.codemucker.cqrs.PathExpression;
import org.codemucker.cqrs.PathExpression.Var;
import org.codemucker.cqrs.client.gwt.AbstractCqrsGwtClient;
import org.codemucker.cqrs.client.gwt.GenerateCqrsGwtClient;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.ReflectedAnnotation;
import org.codemucker.jfind.ReflectedClass;
import org.codemucker.jfind.ReflectedField;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.matcher.AField;
import org.codemucker.jfind.matcher.AMethod;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ClashStrategy;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.JMutateInfo;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.JTypeMutator;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJModifier;
import org.codemucker.jmutate.generate.AbstractGenerator;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.codemucker.jmutate.transform.InsertTypeTransform;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.IsGenerated;
import org.codemucker.lang.BeanNameUtil;
import org.codemucker.lang.ClassNameUtil;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Generates the matchers for the given pojos
 */
public class MatcherGenerator extends AbstractGenerator<GenerateMatchers> {

    private final Logger log = LogManager.getLogger(MatcherGenerator.class);

    private static final String CODE_GEN_INFO_CLASS_PKG = "org.codemucker.jmutate.generate.matcher.generated";
    private static final String CODE_GEN_INFO_CLASS_NAME = "CodeGeneration";
    
    private final Matcher<Annotation> reflectedAnnotationIgnore = AnAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
    private final Matcher<JAnnotation> sourceAnnotationIgnore = AJAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));

    
    private final JMutateContext ctxt;

    @Inject
    public MatcherGenerator(JMutateContext ctxt) {
        this.ctxt = ctxt;
    }

    @Override
    public void generate(JType optionsDeclaredInNode, GenerateMatchers options) {
        PojoScanner requestScanner = new PojoScanner(ctxt,optionsDeclaredInNode, options);
        
        AllPojosModel allPojosModel = new AllPojosModel(optionsDeclaredInNode, options);
        if(options.scanSources() ){
            FindResult<JType> requestTypes = requestScanner.scanSources();
            // add the appropriate methods and types for each request bean
            for (JType requestType : requestTypes) {
                PojoModel requestModel = new PojoModel(allPojosModel, requestType);
                extractFields(requestModel, requestType);
                allPojosModel.addRequest(requestModel);
            }
        }
        
        if(options.scanDependencies() && options.pojoDependencies().length > 0){
            FindResult<Class<?>> requestTypes = requestScanner.scanForReflectedClasses();
            // add the appropriate methods and types for each request bean
            for (Class<?> requestType : requestTypes) {
                PojoModel requestModel = new PojoModel(allPojosModel, requestType);
                extractFields(requestModel, requestType);
                allPojosModel.addRequest(requestModel);
            }
        }
        
        generate(optionsDeclaredInNode, options, allPojosModel);
    }

    private void generate(JType optionsDeclaredInNode, GenerateMatchers options, AllPojosModel serviceModel) {
        createGenInfoIfNotExists();
           
        for(PojoModel requestModel:serviceModel.requests){
        	implementationSource = new pojo macther....
            // ------ add json reader/writer classes
            JType jsonWriter = createJsonWriterType(requestModel);
            addOrReplaceType(implementationMutator, jsonWriter);

            JType jsonReader = createJsonReaderType(requestModel);
            addOrReplaceType(implementationMutator, jsonReader);

            // ------ add request builder create method
            JMethod buildMethod = createBuildMethod(requestModel);
            if (interfaceMutator != null && requestModel.serviceModel.generateInterfaceBuilderMethods) {
                addToInterface(interfaceMutator, buildMethod);
            }
            addOrReplaceMethod(implementationMutator, buildMethod);

            // ------ add async request method
            if (options.generateAsync()) {
                JMethod asyncMethod = createAsyncMethod(requestModel, implementationMutator, options.generateInterface());
                if (interfaceMutator != null) {
                    addToInterface(interfaceMutator, asyncMethod);
                }
                addOrReplaceMethod(implementationMutator, asyncMethod);
            }

            writeToDiskIfChanged(implementationSource);
        }
    }

    private JSourceFile newServiceImplementation(AllPojosModel serviceDetails) {
        SourceTemplate template = ctxt.newSourceTemplate().var("serviceType", serviceDetails.serviceTypeSimple).pl("package " + serviceDetails.pkg + ";").pl("")
                .pl("/* generated from definitions in " + serviceDetails.optionsDeclaredInTypeFull + "*/");

        addGeneratedMarkers(template);
        template.pl("public class ${serviceType} extends " + serviceDetails.baseTypeFull + "{");

        // copy relevant super class constructors
        for (Constructor<?> ctor : serviceDetails.options.serviceBaseClass().getConstructors()) {
            if (!Modifier.isPrivate(ctor.getModifiers())) {
                addGeneratedMarkers(template);
                copySuperConstructor(serviceDetails.serviceTypeSimple, template, ctor);
            }
        }
        template.pl("}");

        return template.asSourceFileSnippet();
    }

    // TODO:move to separate transform class?
    private void copySuperConstructor(String ctorName, SourceTemplate template, Constructor<?> ctor) {
        if (ctor.getAnnotation(Inject.class) != null) {
            template.pl("@" + NameUtil.compiledNameToSourceName(Inject.class));
        }
        if (ctor.getAnnotation(javax.inject.Inject.class) != null) {
            template.pl("@" + NameUtil.compiledNameToSourceName(javax.inject.Inject.class));
        }

        if (Modifier.isPublic(ctor.getModifiers())) {
            template.p("public ");
        } else if (Modifier.isProtected(ctor.getModifiers())) {
            template.p("protected ");
        }
        template.p(ctorName + " (");
        List<String> argNames = new ArrayList<>();
        boolean comma = false;
        for (Class<?> param : ctor.getParameterTypes()) {
            if (comma) {
                template.p(",");
            }
            comma = true;
            // come up with some sensible name
            String argName = createArgNameFromType(param.getName(), argNames);
            argNames.add(argName);
            template.p(NameUtil.compiledNameToSourceName(param) + " " + argName);
        }
        template.pl("){");
        template.p("    super(");
        comma = false;
        for (String argName : argNames) {
            if (comma) {
                template.p(",");
            }
            comma = true;
            template.p(argName);
        }
        template.pl(");");
        template.pl("}");
    }

    /**
     * Try to come up with a sensible arg name from the given type.
     * 
     * Ensure the argName is unique
     * 
     * Converts FooBar - > bar, or Foo ->foo
     */
    private static String createArgNameFromType(String paramType, List<String> argNames) {
        String argName = paramType.toLowerCase();
        for (int i = paramType.length() - 1; i >= 0; i--) {
            char c = paramType.charAt(i);
            if (Character.isUpperCase(c)) {
                argName = paramType.substring(i).toLowerCase();
                break;
            }
        }
        if (argNames.contains(argName)) { // ensure unique name
            int i = 2;
            while (argNames.contains(argName + i)) {
                i++;
            }
            argName += i;
        }
        return argName;
    }

    private JSourceFile newServiceInterface(AllPojosModel serviceModel) {
        SourceTemplate template = ctxt.newSourceTemplate().pl("package " + serviceModel.pkg + ";").pl("")
                .pl("/* generated from definitions in " + serviceModel.optionsDeclaredInTypeFull + "*/");

        addGeneratedMarkers(template);

        template.pl("public interface " + serviceModel.interfaceTypeSimple + "{}");

        return template.asSourceFileSnippet();
    }

    /**
     * Generate the async call
     */
    private JMethod createAsyncMethod(PojoModel requestModel, JTypeMutator serviceInterfaceClass, boolean hasInterface) {
        String methodName = requestModel.isCmd ? "cmd" : "query";

        // build the interface method
        SourceTemplate template = ctxt.newSourceTemplate()
                .var("responseType", requestModel.responseTypeSimple)
                .var("jsonReaderType", requestModel.jsonReaderTypeSimple)
                .var("requestType", requestModel.requestTypeFull).var("requestArg", requestModel.argName)
                .var("methodName", methodName)
                .var("exceptionType", requestModel.serviceModel.errorTypeFull)
                .var("requestHandle", requestModel.asyncRequestHandleTypeFull);

        template.pl("/** Asynchronously invoke the given request */");
        addGeneratedMarkers(template);

        if (hasInterface) {
            template.pl("@Override");
        }
        template
            .pl("public ${requestHandle} ${methodName}(${requestType} ${requestArg},com.google.gwt.core.client.Callback<${responseType}, ${exceptionType}> callback){")
            .pl("   com.github.nmorel.gwtjackson.client.ObjectReader<${responseType}> jsonResponseReader = com.google.gwt.core.client.GWT.create(${jsonReaderType}.class);")
            .pl("   return invokeAsync(${requestArg},buildRequest(${requestArg}),jsonResponseReader,callback);").pl("}");

        return template.asJMethodSnippet();
    }

    /**
     * Generate the method to extract all the info from the request bean
     */
    private JMethod createBuildMethod(PojoModel requestModel){
        // TODO:also generate from source

        log("creating request builder method for type:" + requestModel.requestTypeFull);

        SourceTemplate template = ctxt.newTempSourceTemplate()
                .var("requestType", requestModel.requestTypeFull)
                .var("requestTypeSimple", requestModel.requestTypeSimple)
                .var("requestArg", requestModel.argName).var("builderType", requestModel.serviceModel.builderTypeFull)
                .var("exceptionType", requestModel.serviceModel.errorTypeFull)
                .pl("/** Return a request builder with all the values extracted from the given ${requestArg} */");

        addGeneratedMarkers(template);

        if (requestModel.serviceModel.generateInterface && requestModel.serviceModel.generateInterfaceBuilderMethods) {
            template.pl("@Override");
        }
        template.pl("public ${builderType} buildRequest(${requestType} ${requestArg}){");

        // validator
        if (requestModel.serviceModel.options.validateRequests()) {
            template.pl("checkForValidationErrors(getValidator().validate(${requestArg}),\"${requestTypeSimple}\");");
        }
        // validate() method
        if (requestModel.validateMethodName != null) {
            template.pl(" ${requestArg}." + requestModel.validateMethodName + "();");
        }

        // start building request
        template.pl("  ${builderType} builder = newRequestBuilder();");
        if (requestModel.isCmd) {
            template.pl("  builder.methodPUT();");
        }
        addRequestPath(template, requestModel);

        for (FieldModel fieldModel : requestModel.getFields()) {
            List<String> buildSetters = new ArrayList<>();
            if(fieldModel.isHeaderParam){
                buildSetters.add("setHeaderIfNotNull");
            }
            if(fieldModel.isQueryParam){
                buildSetters.add("setQueryParamIfNotNull");
            }
            if(fieldModel.isBodyParam){
                buildSetters.add("setBodyParamIfNotNull");
            }
            for(String buildSetter:buildSetters){
                template.pl("builder.${buildSetter}(\"${param}\",${requestArg}.${getter});", "param", fieldModel.paramName, "buildSetter", buildSetter, "getter",fieldModel.propertyGetter);
            }
        }
        // json body
        if (requestModel.isCmd) {
            template.pl("com.github.nmorel.gwtjackson.client.ObjectWriter<${requestType}> jsonRequestWriter = com.google.gwt.core.client.GWT.create(" + requestModel.jsonWriterTypeSimple + ".class);");
            template.pl("builder.bodyRaw(jsonRequestWriter.write(${requestArg}));");
        }

        template.pl("  return builder;");
        template.pl("}");

        return template.asJMethodSnippet();
    }

    private void extractFields(PojoModel requestModel, Class<?> requestType) {
        ReflectedClass requestBean = ReflectedClass.from(requestType);
        FindResult<Field> fields = requestBean.findFieldsMatching(AField.that().isNotStatic().isNotTransient().isNotNative());
        log.trace("found " + fields.toList().size() + " fields");
        for (Field f : fields) {
            ReflectedField field = ReflectedField.from(f);
            if (field.hasAnnotation(reflectedAnnotationIgnore)) {
                log("ignoring field:" + f.getName());
                continue;
            }
            FieldModel fieldModel = new FieldModel(requestModel, f.getName());

            String getterName = BeanNameUtil.toGetterName(field.getName(), field.getType());
            String getter = getterName + "()";
            if (!requestBean.hasMethodMatching(AMethod.with().name(getterName).numArgs(0))) {
                if (!field.isPublic()) {
                    //can't access field, lets skip
                	continue;
                }
                getter = field.getName();// direct field access
            }
            fieldModel.propertyGetter = getter;
            
            requestModel.addField(fieldModel);
        }
    }

    private void extractFields(PojoModel request, JType pojoType) {
        // call request builder methods for each field/method exposed
        FindResult<JField> fields = pojoType.findFieldsMatching(AJField.with().modifiers(AJModifier.that().isNotStatic().isNotTransient().isNotNative()));
        log("found " + fields.toList().size() + " fields");
        for (JField field: fields) {
            if (field.getAnnotations().contains(sourceAnnotationIgnore)) {
                log("ignoring field:" + field.getName());
                continue;
            }
            FieldModel fieldModel = new FieldModel(request, field.getName());
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
            fieldModel.propertyGetter = getter;
            
            request.addField(fieldModel);
        }
    }
    private static String getOr(String val, String defaultVal) {
        if (Strings.isNullOrEmpty(val)) {
            return defaultVal;
        }
        return val;
    }

    private void addGeneratedMarkers(SourceTemplate template) {
        String genInfo = CODE_GEN_INFO_CLASS_NAME + "." + getClass().getSimpleName();
        template.var("generator", genInfo);
        template.pl("@" + javax.annotation.Generated.class.getName() + "(${generator})");
        template.pl("@" + IsGenerated.class.getName() + "(generator=${generator})");
    }

    private void createGenInfoIfNotExists() {
        
        RootResource resource = ctxt.getDefaultGenerationRoot().getResource(CODE_GEN_INFO_CLASS_PKG + "." + CODE_GEN_INFO_CLASS_NAME + ".java");
        JSourceFile source;
        if (!resource.exists()) {
            source = ctxt.newSourceTemplate()
                    .var("pkg", CODE_GEN_INFO_CLASS_PKG)
                    .var("className", CODE_GEN_INFO_CLASS_NAME)
                    .pl("package ${pkg};")
                    .pl("public class ${className} {}")
                    .asSourceFileSnippet();
        } else {
            source = JSourceFile.fromResource(resource, ctxt.getParser());
        }
        JField field = ctxt.newSourceTemplate()
                .pl("public static final String " + getClass().getSimpleName() + "=\"" + JMutateInfo.all + " " + getClass().getName() + "\";")
                .asJFieldSnippet();

        ctxt.obtain(InsertFieldTransform.class).target(source.getMainType()).field(field).clashStrategy(ClashStrategy.REPLACE).transform();

        writeToDiskIfChanged(source);
    }

    private void addToInterface(JTypeMutator interfaceType, JMethod m) {
        if (!interfaceType.getJType().isInterface()) {
            throw new JMutateException("expected interface type but instead got " + interfaceType.getJType().getFullName());
        }

        JMethod interfaceMethod = ctxt.newSourceTemplate()
        // .pl("/** Generated method to build an http request */")
                .p(m.toInterfaceMethodSignature()).asJMethodInterfaceSnippet();

        addOrReplaceMethod(interfaceType, interfaceMethod);
    }

    private void addOrReplaceMethod(JTypeMutator targetType, JMethod m) {
        ctxt.obtain(InsertMethodTransform.class).target(targetType.getJType()).method(m.getAstNode()).clashStrategy(ClashStrategy.REPLACE).transform();
    }

    private void addOrReplaceType(JTypeMutator targetType, JType addType) {
        ctxt.obtain(InsertTypeTransform.class).target(targetType.getJType()).setType(addType).clashStrategy(ClashStrategy.REPLACE).transform();
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

    /**
     * Global details about what is being generated
     */
    private static class AllPojosModel {
        final GenerateMatchers options;
        final String pkg;

        private final List<PojoModel> requests = new ArrayList<>();
        
        public AllPojosModel(JType declaredInNode, GenerateMatchers options) {
            this.options = options;
            this.pkg = Strings.emptyToNull(options.matcherPackage());
        }

        void addRequest(PojoModel request){
            this.requests.add(request);
        }
        
    }

    /**
     * Holds the details about an individual request bean
     */
    private static class PojoModel {
        private static final Matcher<Method> validateMethodMatcher = AMethod.with().name("validate").numArgs(0).isPublic();
        private static final Matcher<JMethod> validateMethodMatcherSource = AJMethod.with().name("validate").numArgs(0).isPublic();
        
        final AllPojosModel serviceModel;

        final String argName = "bean";
        final String requestTypeFull;
        final String requestTypeSimple;
        
        final private Map<String, FieldModel> fields = new LinkedHashMap<>();
        
        PojoModel(AllPojosModel parent, Class<?> requestType) {
            this.serviceModel = parent;
            this.requestTypeFull = NameUtil.compiledNameToSourceName(requestType);
            this.requestTypeSimple = requestType.getSimpleName();
        }
        
        PojoModel(AllPojosModel parent, JType requestType) {
            this.serviceModel = parent;
            this.requestTypeFull = requestType.getFullName();
            this.requestTypeSimple = requestType.getSimpleName();
        }
        
        void addField(FieldModel field){
            if (hasNamedField(field.propertyName)) {
                throw new JMutateException("More than one property with the same param name '%s' on %s", field.propertyName, requestTypeFull);
            }
            fields.put(field.propertyName, field);
        }
        
        boolean hasNamedField(String name){
            return fields.containsKey(name);
        }
        
        FieldModel getNamedField(String name){
            return fields.get(name);
        }
        
        Collection<FieldModel> getFields(){
            return fields.values();
        }
    }

    private static class FieldModel {
        final PojoModel pojoModel;
        final String propertyName;
        String propertyGetter;
        
        FieldModel(PojoModel parent, String fieldName) {
            this.pojoModel = parent;
            this.propertyName = fieldName;
        }
    }

}