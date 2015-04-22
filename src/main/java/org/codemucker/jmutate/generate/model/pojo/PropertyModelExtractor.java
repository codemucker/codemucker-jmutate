package org.codemucker.jmutate.generate.model.pojo;

import static org.codemucker.jmatch.Logical.all;
import static org.codemucker.jmatch.Logical.any;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.AbstractReflectedObject;
import org.codemucker.jfind.ReflectedClass;
import org.codemucker.jfind.ReflectedField;
import org.codemucker.jfind.ReflectedMethod;
import org.codemucker.jfind.matcher.AField;
import org.codemucker.jfind.matcher.AMethod;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.AnnotationsProvider;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJModifier;
import org.codemucker.jmutate.generate.model.AbstractCachingModelExtractor;
import org.codemucker.jmutate.generate.model.AbstractModelExtractor;
import org.codemucker.jmutate.generate.model.MethodModel;
import org.codemucker.jmutate.generate.model.ModelExtractor;
import org.codemucker.jmutate.generate.model.ModelRegistry;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.bean.NotAProperty;
import org.codemucker.jpattern.bean.Property;
import org.codemucker.lang.BeanNameUtil;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.google.inject.Inject;

/**
 * I find source and compiled properties
 */
public class PropertyModelExtractor extends AbstractModelExtractor<PojoModel> {
	
	private static final Logger LOG = LogManager.getLogger(PropertyModelExtractor.class);

	private static final Matcher<Annotation> annotationIgnoreCompiled = 
			any(AnAnnotation.with().fullName(AString.matchingExpression("*.Ignore")),
				AnAnnotation.with().fullName(NotAProperty.class));
	
	private static final Matcher<JAnnotation> annotationIgnoreSource =
			any(AJAnnotation.with().fullName(AString.matchingExpression("*.Ignore")),
				AJAnnotation.with().fullName(NotAProperty.class));

	private static final Matcher<JMethod> getterMatcherSource = 
			all(
				AJMethod.with().numArgs(0).isNotVoidReturn(),
				any(AJMethod.with().annotation(Property.class), 
				    AJMethod.with()
						.name(AString.matchingExpression("get?*"))
						.isPublic(),
					AJMethod.with()
						.name(AString.matchingExpression("is?*||has?*"))
						.isPublic()
						.returnType("boolean")));
	
	private static final Matcher<Method> getterMatcherCompiled = 
			all(
				AMethod.with().numArgs(0).isNotVoidReturn(),
				any(AMethod.with().annotation(Property.class),
					AMethod.with()
						.name(AString.matchingExpression("get?*"))
						.isPublic(),
					AMethod.with()
						.name(AString.matchingExpression("is?*||has?*"))
						.isPublic()
						.returnType("boolean")));
	
	private static final Matcher<JMethod> setterMatcherSource = 
			all(
				AJMethod.with().numArgs(1),
				any(AJMethod.with().annotation(Property.class), 
					AJMethod.with()
						.name(AString.matchingExpression("set?*"))
						.isPublic()
						.isVoidReturn()));

	private static final Matcher<Method> setterMatcherCompiled = 
			all(
				AMethod.with().numArgs(1),
				any(AMethod.with().annotation(Property.class), 
					AMethod.with()
						.name(AString.matchingExpression("set?*"))
						.isPublic()
						.isVoidReturn()));
	
	private static final CloneMethodExtractor cloneMethodExtractor = new CloneMethodExtractor();
	
	private static final Matcher<String> ignoreNames = any(AString.startingWith("_"),AString.startingWith("$"),AString.equalToAny("hashCode","toString", "equals", "clone"));
	private final SourceLoader sourceLoader;

	private final boolean includeSuperClass;
	private final boolean includeCompiledClasses;
	private final boolean includeFields;
	private final boolean includeSetters;
	private final boolean includeGetters;
	private final boolean includeMagicMethods = true;

	private final Matcher<String> propertyNameMatcher;

	private PropertyModelExtractor(SourceLoader sourceLoader,boolean includeSuperClass,
			boolean includeCompiledClasses, Matcher<String> propertyNameMatcher, boolean includeFields, boolean includeGetters, boolean includeSetters) {
		super(sourceLoader);
		this.sourceLoader = sourceLoader;
		this.includeSuperClass = includeSuperClass;
		this.includeCompiledClasses = includeCompiledClasses;
		this.propertyNameMatcher = propertyNameMatcher;
		
		this.includeFields = includeFields;
		this.includeGetters = includeGetters;
		this.includeSetters = includeSetters;
	}


	@Override
	public Class<PojoModel> getModelClass() {
		return PojoModel.class;
	}

	@Override
	public PojoModel extractModelFromClass(JType pojoType) {
		return extractModelFromClass(pojoType, 0);
	}
	
	private PojoModel extractModelFromClass(JType pojoType, int level) {
		PojoModel parentModel = null;
		if (includeSuperClass) {
			String superTypeFullName = pojoType.getSuperTypeFullName();
			if(superTypeFullName != null){
				JType superType = pojoType.getSuperTypeOrNull();
				if (superType != null) {
					parentModel = extractModelFromClass(superType, level+1);
				} else {
					if (includeCompiledClasses) {
						Class<?> k = sourceLoader.loadClassOrNull(superTypeFullName);
						if (k != null) {
							parentModel = extractModelFromClass(k);
						}
					}
				}
			}
		}

		PojoModel model = new PojoModel(new TypeModel(pojoType.getFullGenericName(), pojoType.getTypeBoundsExpressionOrNull()),level,parentModel);

		if(includeFields){
			extractFields(pojoType, model);
		}
		if(includeGetters){
			extractGetters(pojoType, model);
		}
		if(includeSetters){
			extractSetters(pojoType, model);
		}
		
		if(includeMagicMethods){
			extractMagicMethods(pojoType, model);
		}
		
		return model;
	}

	private void extractFields(JType pojoType, PojoModel model) {
		List<JField> fields = pojoType.findFieldsMatching(AJField.with().modifier(AJModifier.that().isNotStatic().isNotNative())).toList();
		LOG.debug("found " + fields.size() + " potential source fields");
		int count = 0;
		for (JField f : fields) {
			String propertyName = getName(f);
			if (!isInclude(f, propertyName)) {
				LOG.debug("ignoring source field:" + f.getName());
				continue;
			}

			PropertyModel property = new PropertyModel(model, propertyName,f.getFullTypeName());

			property.setFieldName(f.getName());
			property.setFinalField(f.isFinal());

			model.addProperty(property);
			count++;
		}
		LOG.debug("added " + count + " source fields");
	}

	private void extractGetters(JType pojoType, PojoModel model) {
		List<JMethod> getters = pojoType
				.findMethodsMatching(getterMatcherSource).toList();
		int count = 0;
		LOG.debug("found " + getters.size() + " potential source getters");
		for (JMethod getter : getters) {
			String propertyName = getName(getter);
			if (!isInclude(getter, propertyName)) {
				LOG.debug("ignoring source getter:" + getter.getName());
				continue;
			}
			PropertyModel p = model.getProperty(propertyName);
			if (p == null) {
				p = new PropertyModel(model, propertyName,getter.getReturnTypeFullName());
				model.addProperty(p);
			}
			p.setGetterName(getter.getName());
			count++;
		}
		LOG.debug("added " + count + " source getters");
	}
	
	private void extractSetters(JType pojoType, PojoModel model) {
		List<JMethod> setters = pojoType.findMethodsMatching(setterMatcherSource).toList();
		LOG.debug("found " + setters.size() + " potential source setters");
		int count = 0;
		for (JMethod setter : setters) {
			String propertyName = getName(setter);
			if (!isInclude(setter, propertyName)) {
				LOG.debug("ignoring source setter:" + setter.getName());
				continue;
			}

			PropertyModel p = model.getProperty(propertyName);
			if (p == null) {
				SingleVariableDeclaration arg = setter.getParameters().iterator().next();
				String pType = NameUtil.resolveQualifiedName(arg.getType());
				p = new PropertyModel(model, propertyName, pType);
				model.addProperty(p);
			}
			p.setSetterName(setter.getName());
			count++;
		}
		LOG.debug("added " + count + " source setters");
	}

	@Override
	public PojoModel extractModelFromClass(Class<?> pojoClass) {
		return extractModelFromClass(pojoClass,0);
	}
	
	private PojoModel extractModelFromClass(Class<?> pojoClass, int level) {

		PojoModel parentModel = null;
		if (includeSuperClass) {
			Class<?> parent = pojoClass.getSuperclass();
			if (parent != null && parent != Object.class) {
				parentModel = extractModelFromClass(pojoClass, level+1);
			}
		}
		PojoModel model = new PojoModel(new TypeModel(NameUtil.compiledNameToSourceName(pojoClass),null),level,parentModel);

		ReflectedClass pojoType = ReflectedClass.from(pojoClass);
		if (includeFields) {
			extractFields(pojoType, model);
		}
		if (includeGetters) {
			extractGetters(pojoType, model);
		}
		if (includeSetters) {
			extractSetters(pojoType, model);
		}
		if(includeMagicMethods){
			extractMagicMethods(pojoType, model);
		}
		return model;
	}

	private void extractFields(ReflectedClass pojoType, PojoModel model) {
		List<Field> fields = pojoType.findFieldsMatching(AField.that().isNotStatic().isNotNative()).toList();
		LOG.trace("found " + fields.size() + " potential compiled fields");
		int count = 0;
		for (Field f : fields) {
			ReflectedField field = ReflectedField.from(f);
			String propertyName = getName(f);
			
			if (!isInclude(field, propertyName)) {
				LOG.debug("ignoring compiled field:" + f.getName());
				continue;
			}
			PropertyModel property = new PropertyModel(model, propertyName, f.getGenericType().getTypeName());
			property.setFieldName(field.getName());
			property.setFinalField(field.isFinal());

			model.addProperty(property);
			count++;
		}
		LOG.debug("added " + count + " compiled fields");
	}

	private void extractGetters(ReflectedClass pojoType, PojoModel model) {
		List<Method> getters = pojoType.findMethodsMatching(getterMatcherCompiled).toList();
		LOG.trace("found " + getters.size() + " potential compiled getters");
		int count = 0;
		for (Method m : getters) {
			String propertyName = getName(m);
			ReflectedMethod getter = ReflectedMethod.from(m);
			
			if (!isInclude(getter, propertyName)) {
				LOG.debug("ignoring compiled getter:" + getter.getName());
				continue;
			}
			PropertyModel p = model.getProperty(propertyName);
			if (p == null) {
				p = new PropertyModel(model, propertyName, getter.getUnderlying().getReturnType().getName());
				model.addProperty(p);
			}
			p.setGetterName(getter.getName());
			count++;
		}
		LOG.trace("added " + count + " compiled getters");
	}

	private void extractSetters(ReflectedClass pojoType, PojoModel model) {
		List<Method> setters = pojoType.findMethodsMatching(setterMatcherCompiled).toList();
		LOG.trace("found " + setters.size() + " potential compiled setters");
		int count = 0;
		for (Method m : setters) {
			String propertyName = getName(m);
			ReflectedMethod setter = ReflectedMethod.from(m);
			if (!isInclude(setter, propertyName)) {
				LOG.debug("ignoring compiled setter:" + setter.getName());
				continue;
			}

			PropertyModel p = model.getProperty(propertyName);
			if (p == null) {
				Class<?> argType = setter.getUnderlying().getParameterTypes()[0];
				p = new PropertyModel(model, propertyName, argType.getName());
				model.addProperty(p);
			}
			p.setSetterName(setter.getName());
			count++;
		}
		LOG.trace("added " + count + " compiled setters");
	}
	

	private void extractMagicMethods(JType pojoType, PojoModel model) {
		JMethod clone = cloneMethodExtractor.extractCloneMethodOrNull(pojoType);
		if(clone != null){
			model.setCloneMethod(new MethodModel(clone));
		}
	}

	private void extractMagicMethods(ReflectedClass pojoType, PojoModel model) {
		Method clone = cloneMethodExtractor.extractCloneMethodOrNull(pojoType);
		if(clone != null){
			model.setCloneMethod(new MethodModel(clone));
		}
	}

	private boolean isInclude(AnnotationsProvider provider, String name) {
		if (provider.getAnnotations().contains(annotationIgnoreSource) || ignoreNames.matches(name)) {
			return false;
		}
		if (propertyNameMatcher != null && !propertyNameMatcher.matches(name)) {
			return false;
		}
		return true;
	}

	private boolean isInclude(AbstractReflectedObject obj, String name) {
		if (obj.hasAnnotation(annotationIgnoreCompiled) || ignoreNames.matches(name)) {
			return false;
		}
		if (propertyNameMatcher != null && !propertyNameMatcher.matches(name)) {
			return false;
		}
		return true;
	}

	private String getName(JField f){
		String name = getCustomPropertyNameOrNull(f);
		if(name == null){
			name = BeanNameUtil.fieldToPropertyName(f.getName());
		}
		return name;
	}
	
	private String getName(Field f){
		Property anon = f.getAnnotation(Property.class);
		if(anon != null){
			return anon.name();
		}
		return BeanNameUtil.fieldToPropertyName(f.getName());
	}
	
	private String getName(JMethod m){
		String name = getCustomPropertyNameOrNull(m);
		if(name == null){
			name = BeanNameUtil.methodToPropertyName(m.getName());
		}
		return name;
	}

	private String getName(Method m){
		Property anon = m.getAnnotation(Property.class);
		if(anon != null){
			return anon.name();
		}
		return BeanNameUtil.methodToPropertyName(m.getName());
	}

	private String getCustomPropertyNameOrNull(AnnotationsProvider provider){
		JAnnotation anon = provider.getAnnotations().getOrNull(Property.class);
		if(anon != null){
			return anon.getAttributeValue("name").toString();
		}
		return null;
	}

	public static Builder with(SourceLoader sourceLoader) {
		return new Builder(sourceLoader);
	}

	public static class Builder {

		private SourceLoader sourceLoader;

		private boolean includeSuperClass = true;
		private boolean includeCompiledClasses = true;
		private boolean includeFields = true;
		private boolean includeSetters = true;
		private boolean includeGetters = true;
		
		private Matcher<String> propertyNameMatcher;

		@Inject
		public Builder(SourceLoader sourceLoader) {
			this.sourceLoader = sourceLoader;
		}

		public PropertyModelExtractor build() {
			return new PropertyModelExtractor(sourceLoader,
					includeSuperClass, includeCompiledClasses,
					propertyNameMatcher, includeFields, includeGetters, includeSetters);
		}

		@Inject
		public Builder sourceLoader(SourceLoader sourceLoader) {
			this.sourceLoader = sourceLoader;
			return this;
		}

		public Builder includeSuperClass(boolean includeSuperClass) {
			this.includeSuperClass = includeSuperClass;
			return this;
		}

		public Builder includeCompiledClasses(boolean includeCompiledClasses) {
			this.includeCompiledClasses = includeCompiledClasses;
			return this;
		}

		public Builder propertyNameMatching(String expression) {
			propertyNameMatcher(AString.matchingExpression(expression));
			return this;
		}
		
		public Builder propertyNameMatcher(Matcher<String> propertyNameMatcher) {
			this.propertyNameMatcher = propertyNameMatcher;
			return this;
		}
		
		public Builder includeFields(boolean includeFields) {
			this.includeFields = includeFields;
			return this;
		}

		public Builder includeSetters(boolean includeSetters) {
			this.includeSetters = includeSetters;
			return this;
		}

		public Builder includeGetters(boolean includeGetters) {
			this.includeGetters = includeGetters;
			return this;
		}

	}

}
