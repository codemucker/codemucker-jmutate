package org.codemucker.jmutate.generate.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.ReflectedClass;
import org.codemucker.jfind.ReflectedField;
import org.codemucker.jfind.matcher.AField;
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
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateBean;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Generates property getters/setters for a bean, along with various bean
 * bindings if required
 */
public class BeanGenerator extends AbstractCodeGenerator<GenerateBean> {

	private final Logger log = LogManager.getLogger(BeanGenerator.class);

	private static final int[] FIRST_PRIMES = new int[]{31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151,157,163,167,173,179,181,191,193,197,199,211,223,227,229,233,239,241,251,257,263,269,271,277,281,283,293,307,311,313,317,331,337,347,349,353,359,367,373,379,383,389,397,401,409,419,421,431,433,439,443,449,457,461,463,467,479,487,491,499,503,509,521,523,541,547,557,563,569,571,577,587,593,599,601,607,613,617,619,631,641,643,647,653,659,661,673,677,683,691,701,709,719,727,733,739,743,751,757,761,769,773,787,797,809,811,821,823,827,829,839,853,857,859,863,877,881,883,887,907,911,919,929,937,941,947,953,967,971,977,983,991,997,1009,1013};
	
	private final CodeGenMetaGenerator generatorMeta;
	private final Matcher<Annotation> reflectedAnnotationIgnore = AnAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
	private final Matcher<JAnnotation> sourceAnnotationIgnore = AJAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));

	private final JMutateContext ctxt;
	private ClashStrategyResolver methodClashResolver;

	@Inject
	public BeanGenerator(JMutateContext ctxt) {
		this.ctxt = ctxt;
		this.generatorMeta = new CodeGenMetaGenerator(ctxt, getClass());
	}

	@Override
	public void generate(JType optionsDeclaredInNode, GenerateBean options) {
		if(optionsDeclaredInNode.isInterface()){
			log.warn("the " + GenerateBean.class.getName() + " generation annotation on an interface is not supported");
			return;
		}
		ClashStrategy methodClashDefaultStrategy = getOr(options.clashStrategy(), ClashStrategy.SKIP);
		methodClashResolver = new OnlyReplaceMyManagedMethodsResolver(methodClashDefaultStrategy);
		Matcher<String> fieldMatcher = fieldMatcher(options.fieldNames());
		
		BeanModel model = new BeanModel(optionsDeclaredInNode,options);
		extractFields(model, optionsDeclaredInNode, fieldMatcher);
		log.debug("found " + model.properties.size() + " bean properties for "  + model.type.fullNameRaw);
		// TODO:enable builder creation for 3rd party compiled classes
		generateBeanProperties(optionsDeclaredInNode, model);
	}
	
	private Matcher<String> fieldMatcher(String s){
		if(Strings.isNullOrEmpty(s)){
			return AString.equalToAnything();
		}
		return ExpressionParser.parse(s, new StringMatcherBuilderCallback());
	}

	private static <T> T getOr(T val, T defaultVal) {
		if (val == null) {
			return defaultVal;
		}
		return val;	
	}

	private void generateBeanProperties(JType bean,BeanModel model) {
		JSourceFile source = bean.getSource();
		
		generateNoArgCtor(bean, model);
		generateStaticPropertyNames(bean, model);
		
		for (BeanPropertyModel property : model.properties.values()) {
			generateFieldAccess(bean, model, property);			
			generateGetter(bean, model, property);
			generateSetter(bean, model, property);
			generateCollectionAddRemove(bean, model, property);
			generateMapAddRemove(bean, model, property);
		}
		
		generateToString(bean, model);
		generateEquals(bean, model);
		generateHashCode(bean, model);
		generateClone(bean, model);
		
		writeToDiskIfChanged(source);
	}

	private void generateNoArgCtor(JType bean, BeanModel model) {
		if(model.generateNoArgCtor){
			JMethod ctor = ctxt
				.newSourceTemplate()
				.pl("public " + model.type.simpleNameRaw + "(){}")
				.asConstructorSnippet();
			addMethod(bean, ctor.getAstNode(), model.markGenerated);
		}
	}

	private void generateStaticPropertyNames(JType bean, BeanModel model) {
		//static property names
		if(model.generateStaticPropertyNameFields){
			for (BeanPropertyModel property : model.properties.values()) {
				JField staticField = ctxt
					.newSourceTemplate()
					.pl("public static final String PROP_" + property.propertyName.toUpperCase() + " = \"" + property.propertyName +"\";")
					.asJFieldSnippet();
				addField(bean, staticField.getAstNode(), model.markGenerated);
			}	
		}
	}

	private void generateClone(JType bean, BeanModel model) {
		if(model.generateCloneMethod && !bean.isAbstract()){
			SourceTemplate clone = ctxt
				.newSourceTemplate()
				.var("b.type", model.type.simpleName)
				.var("b.genericPart", model.type.genericPartOrEmpty)
				
				.pl("public static ${b.genericPart} ${b.type} newInstanceOf(${b.type} bean){")
				.pl("if(bean == null){ return null;}")
				.pl("final ${b.type} clone = new ${b.type}();");
			
			for (BeanPropertyModel property : model.properties.values()) {
				SourceTemplate t = clone
					.child()
					.var("p.name",property.propertyName)
					.var("p.type",property.type.fullName)
					.var("p.concreteType",property.propertyConcreteType)
					.var("p.rawType",property.type.fullNameRaw)
					.var("p.genericPart",property.type.genericPartOrEmpty)
					
					;
				if(property.type.isPrimitive){
					t.pl("	clone.${p.name} = bean.${p.name};");
				} else if(property.type.isArray){
					t.pl("	if(bean.${p.name} == null){");
					t.pl("		clone.${p.name} = null;");
					t.pl("	} else {");
					t.pl("		clone.${p.name} = new ${p.rawType}[bean.${p.name}].length;");
					t.pl("		System.arraycopy(bean.${p.name},0,clone.${p.name},0,bean.${p.name}.length);");
					t.pl("	}");				
				} else if(property.type.isIndexed){
					t.pl("	clone.${p.name} = bean.${p.name} == null?null:new ${p.concreteType}${p.genericPart}(bean.${p.name});");
				} else {
			//		if(hasClassGotMethod(property.propertyTypeRaw, AString.matchingAntPattern("*newInstanceOf"))){
					//	t.pl("	clone.${p.name} = bean.${p.name} == null?null:${p.rawType}.newInstanceOf(bean.${p.name});");
				//	} else {
						t.pl("	clone.${p.name} = bean.${p.name};");
					//}
				}
						
				clone.add(t);
			}
			clone.pl("return clone;");
		
			clone.pl("}");
			
			addMethod(bean, clone.asMethodNodeSnippet(),model.markGenerated);
		}
	}

	private boolean hasClassGotMethod(String fullClassName,Matcher<String> methodMatch){
		if(ctxt.getResourceLoader().canLoadClassOrSource(fullClassName)){
			if(!fullClassName.startsWith("java.") &&! fullClassName.startsWith("sun.") && ! fullClassName.startsWith("javax.") && !fullClassName.contains("$")){
				JType type = JType.fromClassNameOrNull(fullClassName, ctxt);
				if( type != null){
					return type.hasMethodMatching(AJMethod.with().nameAndArgSignature(methodMatch));
				}
			}
			try {
				Class<?> klass = ctxt.getResourceLoader().loadClass(fullClassName);
				for(Method m : klass.getMethods()){
					if( methodMatch.matches(m.toGenericString())){
						return true;
					}
				}
				
			} catch (ClassNotFoundException e) {
				//ignore
			}
		}
		return false;
	}
	
	private void generateHashCode(JType bean, BeanModel model) {
		if(model.generateHashCodeEquals && !model.properties.isEmpty()){
			int startingPrime = pickStartingPrimeForClass(model.type.fullName);
			SourceTemplate hashcode = ctxt
				.newSourceTemplate()
				.var("prime", startingPrime)
				.pl("@java.lang.Override")
				.pl("public int hashCode(){");
				
			if(model.properties.isEmpty()){
				hashcode.pl("return super.hashCode();");
			} else {
				hashcode.pl("final int prime = ${prime};");
				hashcode.pl("int result = super.hashCode();");
				for (BeanPropertyModel property : model.properties.values()) {
					SourceTemplate t = hashcode
						.child()
						.var("p.name",property.propertyName);
					if(property.type.isPrimitive && !property.type.isString){
						//from the book 'Effective Java'
						if(property.type.is("boolean")){
							t.pl("result = prime * result + (${p.name} ? 1:0);");
						} else if(property.type.is("byte") || property.type.is("char") || property.type.is("int")){
							t.pl("result = prime * result + ${p.name};");
						} else if(property.type.is("long")){
							t.pl("result = prime * result + (int) (${p.name} ^ (${p.name} >>> 32));");
						} else if(property.type.is("float")){
							t.pl("result = prime * result + java.lang.Float.floatToIntBits(${p.name});");
						} else if(property.type.is("double")){
							t.pl("result = prime * result + java.lang.Double.doubleToLongBits(${p.name});");
						} else  {
							t.pl("result = prime * result + ${p.name}.hashCode();");			
						}
					} else {
						t.pl("result = prime * result + ((${p.name} == null) ? 0 : ${p.name}.hashCode());");
					}
					hashcode.add(t);
				}
				hashcode.pl("return result;");
			}
			
			hashcode.pl("}");
			
			addMethod(bean, hashcode.asMethodNodeSnippet(),model.markGenerated);
		}
	}

	private void generateEquals(JType bean, BeanModel model) {
		if(model.generateHashCodeEquals && !model.properties.isEmpty()){
			
			SourceTemplate equals = ctxt
					.newSourceTemplate()
					.var("b.type", model.type.simpleName)
					.pl("@java.lang.Override")
					.pl("public boolean equals(final Object obj){")
					.pl("if (this == obj) return true;")
					.pl("if (!super.equals(obj) || getClass() != obj.getClass()) return false;");
			
			if(!model.properties.isEmpty()){
				equals.pl("${b.type} other = (${b.type}) obj;");
				for (BeanPropertyModel property : model.properties.values()) {
					SourceTemplate  t = equals
						.child()
						.var("p.name",property.propertyName);
					
					if(property.type.isPrimitive && !property.type.isString){
						t.pl("if (${p.name} != other.${p.name}) return false;");
					} else {
						t.pl("if(${p.name} == null) {")
						.pl("	if (other.${p.name} != null)")
						.pl("		return false;")
						.pl("} else if (!${p.name}.equals(other.${p.name}))")
						.pl("	return false;");
					}
					equals.add(t);
				}
			}
			equals.pl("	return true;");
			equals.pl("}");
			

			addMethod(bean, equals.asMethodNodeSnippet(),model.markGenerated);
		}
	}

	private void generateToString(JType bean, BeanModel model) {
		if(model.generateToString){
			StringBuilder sb = new StringBuilder();
			String label = model.type.simpleNameRaw;
			JType t = bean;
			while(!t.isTopLevelClass()){
				t = t.getParentJType();
				label = t.getSimpleName() + "." + label;
			}
			sb.append("\" [");
			if(model.properties.isEmpty()){
				sb.append("]\"");
			} else {
				boolean comma = false;
				for (BeanPropertyModel property : model.properties.values()) {
					if(comma){
						sb.append(" + \",");
					}
					sb.append(property.propertyName ).append("=\" + ").append(property.propertyName);
					comma = true;
				}
				sb.append(" + \"]\"");
			}
			
			SourceTemplate toString = ctxt
				.newSourceTemplate()
				.var("label", label)
				.pl("@java.lang.Override")
				.pl("public String toString(){")
				.pl("return \"${label}@\" + System.identityHashCode(this) + " + sb.toString() + ";")
				.pl("}");
			addMethod(bean, toString.asMethodNodeSnippet(),model.markGenerated);
		}
	}

	private void generateFieldAccess(JType bean, BeanModel model,
			BeanPropertyModel property) {
		JField field = bean.findFieldsMatching(AJField.with().name(property.propertyName)).getFirst();
		if(!field.getAccess().equals(model.fieldAccess)){
			field.getJModifiers().setAccess(model.fieldAccess);
		}
	}

	private void generateMapAddRemove(JType bean, BeanModel model,BeanPropertyModel property) {
		if(property.type.isMap && model.generateAddRemoveMethodsForIndexedProperties){
			SourceTemplate add = ctxt
				.newSourceTemplate()
				.var("p.name", property.propertyName)
				.var("p.addName", property.propertyAddName)
				.var("p.type", property.type.objectTypeFullName)
				.var("p.newType", property.propertyConcreteType)
				.var("p.genericPart", property.type.genericPartOrEmpty)
				.var("p.keyType", property.type.indexedKeyTypeNameOrNull)
				.var("p.valueType", property.type.indexedValueTypeNameOrNull)
					
				.pl("public void ${p.addName}(final ${p.keyType} key,final ${p.valueType} val){")
				.pl("	if(this.${p.name} == null){ this.${p.name} = new ${p.newType}${p.genericPart}(); }")
				.pl("	this.${p.name}.put(key, val);")
				.pl("}");
				
			addMethod(bean, add.asMethodNodeSnippet(),model.markGenerated);
			
			SourceTemplate remove = ctxt
				.newSourceTemplate()
				.var("p.name", property.propertyName)
				.var("p.removeName", property.propertyRemoveName)
				.var("p.type", property.type.objectTypeFullName)
				.var("p.newType", property.propertyConcreteType)
				.var("p.keyType", property.type.indexedKeyTypeNameOrNull)
				
				.pl("public void ${p.removeName}(final ${p.keyType} key){")
				.pl("	if(this.${p.name} != null){ ")
				.pl("		this.${p.name}.remove(key);")
				.pl("	}")
				
				.pl("}");
				
			addMethod(bean, remove.asMethodNodeSnippet(),model.markGenerated);
		}
	}

	private void generateCollectionAddRemove(JType bean, BeanModel model,BeanPropertyModel property) {
		if(property.type.isCollection && model.generateAddRemoveMethodsForIndexedProperties){
			SourceTemplate add = ctxt
				.newSourceTemplate()
				.var("p.name", property.propertyName)
				.var("p.addName", property.propertyAddName)
				.var("p.type", property.type.objectTypeFullName)
				.var("p.newType", property.propertyConcreteType)
				.var("p.genericPart", property.type.genericPartOrEmpty)
				.var("p.valueType", property.type.indexedValueTypeNameOrNull)
				
				.pl("public void ${p.addName}(final ${p.valueType} val){")
				.pl("	if(this.${p.name} == null){ ")
				.pl("		this.${p.name} = new ${p.newType}${p.genericPart}(); ")
				.pl("	}")	
				.pl("	this.${p.name}.add(val);")
				.pl("}");
				
			addMethod(bean, add.asMethodNodeSnippet(),model.markGenerated);
			
			SourceTemplate remove = ctxt
				.newSourceTemplate()
				.var("p.name", property.propertyName)
				.var("p.removeName", property.propertyRemoveName)
				.var("p.type", property.type.objectTypeFullName)
				.var("p.newType", property.propertyConcreteType)
				.var("p.valueType", property.type.indexedValueTypeNameOrNull)
				
				.pl("public void ${p.removeName}(final ${p.valueType} val){")
				.pl("	if(this.${p.name} != null){ ")
				.pl("		this.${p.name}.remove(val);")
				.pl("	}")
				.pl("}");
				
			addMethod(bean, remove.asMethodNodeSnippet(),model.markGenerated);
		}
	}

	private void generateSetter(JType bean, BeanModel model,BeanPropertyModel property) {
		//setter
		if(property.propertySetterName != null && property.generateSetter){
			SourceTemplate setter = ctxt
				.newSourceTemplate()
				.var("p.name", property.propertyName)
				.var("p.setterName", property.propertySetterName)
				.var("p.type", property.type.objectTypeFullName)
				
				.pl("public void ${p.setterName}(final ${p.type} val){")
				.pl("		this.${p.name} = val;")
				.pl("}");
				
			addMethod(bean, setter.asMethodNodeSnippet(),model.markGenerated);
		}
	}

	private void generateGetter(JType bean, BeanModel model, BeanPropertyModel property) {
		//getter
		if(property.propertyGetterName != null && property.generateGetter){
			SourceTemplate getter = ctxt
				.newSourceTemplate()
				.var("p.name", property.propertyName)
				.var("p.getterName", property.propertyGetterName)
				.var("p.type", property.type.objectTypeFullName)
				
				.pl("public ${p.type} ${p.getterName}(){")
				.pl("		return ${p.name};")
				.pl("}");
				
			addMethod(bean, getter.asMethodNodeSnippet(),model.markGenerated);
		}
	}
	
	//picks a repeatable but randomish prime for the given type
	private int pickStartingPrimeForClass(String fullName){			
		int hash = fullName.hashCode();
		int index;
		if(hash < 0){
			hash = -hash;
		}
		index = hash % FIRST_PRIMES.length;
		return FIRST_PRIMES[index];
	}

	private void addField(JType type, FieldDeclaration f, boolean markGenerated) {
		if(markGenerated){
			generatorMeta.addGeneratedMarkers(f);
		}
		ctxt.obtain(InsertFieldTransform.class)
				.clashStrategy(ClashStrategy.REPLACE)
				.target(type)
				.field(f)
				.transform();
	}
	
	private void addMethod(JType type, MethodDeclaration m, boolean markGenerated) {
		if(markGenerated){
			generatorMeta.addGeneratedMarkers(m);
		}
		ctxt.obtain(InsertMethodTransform.class)
				.clashStrategy(methodClashResolver)
				.target(type)
				.method(m)
				.transform();
	}

	private void extractFields(BeanModel model, Class<?> requestType) {
		ReflectedClass requestBean = ReflectedClass.from(requestType);
		FindResult<Field> fields = requestBean.findFieldsMatching(AField.that().isNotStatic().isNotNative());
		log.trace("found " + fields.toList().size() + " fields");
		for (Field f : fields) {
			ReflectedField field = ReflectedField.from(f);
			if (field.hasAnnotation(reflectedAnnotationIgnore)) {
				log("ignoring field:" + f.getName());
				continue;
			}
			boolean generateGetter = true;//!field.getJModifiers().isFinal();
			boolean generateSetter = !field.isFinal();
			
			BeanPropertyModel property = new BeanPropertyModel(model, f.getName(), f.getGenericType().getTypeName(),generateSetter,generateGetter);
			model.addField(property);
		}
	}

	private void extractFields(BeanModel model, JType pojoType, Matcher<String> fieldMatcher) {
		// call request builder methods for each field/method exposed
		FindResult<JField> fields = pojoType.findFieldsMatching(AJField.with().modifiers(AJModifier.that().isNotStatic().isNotNative()).name(fieldMatcher));
		log("found " + fields.toList().size() + " fields");
		
		for (JField field : fields) {
			if (field.getAnnotations().contains(sourceAnnotationIgnore)) {
				log("ignoring field:" + field.getName());
				continue;
			}
			boolean generateGetter = true;//!field.getJModifiers().isFinal();
			boolean generateSetter = !(model.makeReadonly || field.getJModifiers().isFinal());
			
			BeanPropertyModel property = new BeanPropertyModel(model, field.getName(),field.getFullTypeName(),generateSetter,generateGetter);
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
		ctxt.obtain(CleanImportsTransform.class).addMissingImports(true)
				.nodeToClean(node).transform();
	}

	private void log(String msg) {
		log.debug(msg);
	}

	private class OnlyReplaceMyManagedMethodsResolver implements
			ClashStrategyResolver {

		private final ClashStrategy fallbackStrategy;

		public OnlyReplaceMyManagedMethodsResolver(
				ClashStrategy fallbackStrategy) {
			super();
			this.fallbackStrategy = fallbackStrategy;
		}

		@Override
		public ClashStrategy resolveClash(ASTNode existingNode, ASTNode newNode) {
			if (generatorMeta.isManagedByThis(JMethod.from(existingNode).getAnnotations())) {
				return ClashStrategy.REPLACE;
			}
			return fallbackStrategy;
		}

	}

}