package org.codemucker.jmutate.generate.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.FindResult;
import org.codemucker.jfind.ReflectedClass;
import org.codemucker.jfind.ReflectedField;
import org.codemucker.jfind.matcher.AField;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.expression.ExpressionParser;
import org.codemucker.jmatch.expression.StringMatcherBuilderCallback;
import org.codemucker.jmutate.ClashStrategyResolver;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.ast.matcher.AJModifier;
import org.codemucker.jmutate.generate.AbstractCodeGenerator;
import org.codemucker.jmutate.generate.CodeGenMetaGenerator;
import org.codemucker.jmutate.transform.CleanImportsTransform;
import org.codemucker.jmutate.transform.InsertFieldTransform;
import org.codemucker.jmutate.transform.InsertMethodTransform;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateBean;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Generates property getters/setters for a bean, along with various bean
 * bindings if required
 */
public class Generator extends AbstractCodeGenerator<GenerateBean> {

	private final Logger log = LogManager.getLogger(Generator.class);

	private final CodeGenMetaGenerator generatorMeta;
	private final Matcher<Annotation> reflectedAnnotationIgnore = AnAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));
	private final Matcher<JAnnotation> sourceAnnotationIgnore = AJAnnotation.with().fullName(AString.matchingAntPattern("*.Ignore"));

	private final JMutateContext ctxt;
	private ClashStrategyResolver methodClashResolver;

	@Inject
	public Generator(JMutateContext ctxt) {
		this.ctxt = ctxt;
		this.generatorMeta = new CodeGenMetaGenerator(ctxt, getClass());
	}

	@Override
	public void generate(JType optionsDeclaredInNode, GenerateBean options) {
		ClashStrategy methodClashDefaultStrategy = getOr(options.clashStrategy(), ClashStrategy.SKIP);
		methodClashResolver = new OnlyReplaceMyManagedMethodsResolver(methodClashDefaultStrategy);
		Matcher<String> fieldMatcher = fieldMatcher(options.fieldNames());
		
		BeanModel model = new BeanModel(optionsDeclaredInNode,options);
		extractFields(model, optionsDeclaredInNode, fieldMatcher);
		log.debug("found " + model.properties.size() + " bean properties for "  + model.pojoTypeFull);
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
		SourceTemplate baseTemplate = ctxt.newSourceTemplate();
		boolean markGenerated = model.markGenerated;
		
		//static property names
		if(model.generateStaticPropertyNameFields){
			for (PropertyModel property : model.properties.values()) {
				JField staticField = baseTemplate
					.child()
					.pl("public static final String PROP_" + property.propertyName.toUpperCase() + " = \"" + property.propertyName +"\";")
					.asJFieldSnippet();
				addField(bean, staticField.getAstNode(), markGenerated);
			}	
		}
		
		//getters/setters
		for (PropertyModel property : model.properties.values()) {
			JField field = bean.findFieldsMatching(AJField.with().name(property.propertyName)).getFirst();
			if(!field.getAccess().equals(model.fieldAccess)){
				field.getJModifiers().setAccess(model.fieldAccess);
			}
			
			if(property.propertyGetterName != null){
				SourceTemplate getter = baseTemplate
					.child()
					.var("p.name", property.propertyName)
					.var("p.getterName", property.propertyGetterName)
					.var("p.type", property.propertyTypeAsObject)
					.var("p.type_raw", NameUtil.removeGenericPart(property.propertyTypeAsObject))
					
					.pl("public ${p.type} ${p.getterName}(){")
					.pl("		return ${p.name};")
					.pl("}");
					
				addMethod(bean, getter.asMethodNodeSnippet(),markGenerated);
			}
			
			if(property.propertySetterName != null){
				SourceTemplate setter = baseTemplate
						.child()
						.var("p.name", property.propertyName)
						.var("p.setterName", property.propertySetterName)
						.var("p.type", property.propertyTypeAsObject)
						.var("p.type_raw", NameUtil.removeGenericPart(property.propertyTypeAsObject))
						
						.pl("public void ${p.setterName}(final ${p.type} val){")
						.pl("		this.${p.name} = val;")
						.pl("}");
						
					addMethod(bean, setter.asMethodNodeSnippet(),markGenerated);
			}
		}
		
		if(model.generateHashCodeEquals){
			//equals
			{
				SourceTemplate equals = baseTemplate
						.child()
						.var("b.name", model.pojoTypeSimple)
						.pl("@java.lang.Override")
						.pl("public boolean equals(final Object obj){")
						.pl("if (this == obj) return true;")
						.pl("if (!super.equals(obj) || getClass() != obj.getClass()) return false;")
						.pl("${b.name} other = (${b.name}) obj;");
			
			
				for (PropertyModel property : model.properties.values()) {
					SourceTemplate  t = equals
						.child()
						.var("p.name",property.propertyName);
					
					if(property.isPrimitive && !property.isString){
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
				equals.pl("	return true;");
				equals.pl("}");
				
	
				addMethod(bean, equals.asMethodNodeSnippet(),markGenerated);
			}
			//hashcode
			{
				SourceTemplate hashcode = baseTemplate
						.child()
						.var("b.name", model.pojoTypeSimple)
						.pl("@java.lang.Override")
						.pl("public int hashCode(){")
						.pl("final int prime = 31;") //TODO:pick a random prime
						.pl("int result = super.hashCode();");
				
				for (PropertyModel property : model.properties.values()) {
					SourceTemplate  t = hashcode
						.child()
						.var("p.name",property.propertyName);
					if(property.isPrimitive && !property.isString){
						//from the book 'Effective Java'
						if("boolean".equals(property.propertyType)){
							t.pl("result = prime * result + (${p.name} ? 1:0);");
						} else if("long".equals(property.propertyType)){
							t.pl("result = prime * result + (int) (${p.name} ^ (${p.name} >>> 32));");
						} else if("float".equals(property.propertyType)){
							t.pl("result = prime * result + java.lang.Float.floatToIntBits(${p.name});");
						} else if("double".equals(property.propertyType)){
							t.pl("result = prime * result + java.lang.Double.doubleToLongBits(${p.name});");
						} else  {
							t.pl("result = prime * result + ${p.name}.hashCode();");			
						}
					} else {
						t.pl("result = prime * result + ((${p.name} == null) ? 0 : ${p.name}.hashCode());");
					}
					hashcode.add(t);
				}
				hashcode.pl("	return result;");
				hashcode.pl("}");
				
				addMethod(bean, hashcode.asMethodNodeSnippet(),markGenerated);
			}
			
		}
		
		writeToDiskIfChanged(source);
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
			
			PropertyModel property = new PropertyModel(model, f.getName(), f.getGenericType().getTypeName(),generateSetter,generateGetter);
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
			
			PropertyModel property = new PropertyModel(model, field.getName(),field.getFullTypeName(),generateSetter,generateGetter);
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