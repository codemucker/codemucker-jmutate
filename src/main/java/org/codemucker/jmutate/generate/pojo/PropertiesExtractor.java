package org.codemucker.jmutate.generate.pojo;

import static org.codemucker.jmatch.Logical.and;
import static org.codemucker.jmatch.Logical.or;

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
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.matcher.AField;
import org.codemucker.jfind.matcher.AMethod;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ResourceLoader;
import org.codemucker.jmutate.ast.AnnotationsProvider;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.codemucker.jmutate.ast.matcher.AJModifier;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.bean.NotAProperty;
import org.codemucker.jpattern.bean.Property;
import org.codemucker.lang.BeanNameUtil;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.google.inject.Inject;

public class PropertiesExtractor {
	private static final Logger LOG = LogManager.getLogger(PropertiesExtractor.class);

	private final Matcher<Annotation> reflectedAnnotationIgnore = AnAnnotation
			.with().fullName(AString.matchingAntPattern("*.Ignore"));
	private final Matcher<JAnnotation> sourceAnnotationIgnore = AJAnnotation
			.with().fullName(AString.matchingAntPattern("*.Ignore"));

	private final Matcher<JMethod> getterMatcher = and(
			AJMethod.with().numArgs(0).isNotVoidReturn(),
			or(AJMethod.with().annotation(Property.class), 
			   AJMethod.with()
					.name(AString.matchingAntPattern("get?* || is?* || has?*"))
					.isPublic()));
	
	private final Matcher<JMethod> setterMatcher = and(
			AJMethod.with().numArgs(1),
			or(AJMethod.with().annotation(Property.class), 
			   AJMethod.with()
					.name(AString.matchingAntPattern("set?*"))
					.isPublic()
					.isVoidReturn()));

	private final Matcher<Method> getterMatcherCompiled = and(
			AMethod.with().numArgs(0).isNotVoidReturn(),
			or(AMethod.with().annotation(Property.class), 
			   AMethod.with()
					.name(AString.matchingAntPattern("get?* || is?* || has?*"))
					.isPublic()));
	
	private final Matcher<Method> setterMatcherCompiled = and(
			AMethod.with().numArgs(1),
			or(AMethod.with().annotation(Property.class), 
			   AMethod.with()
					.name(AString.matchingAntPattern("set?*"))
					.isPublic()
					.isVoidReturn()));

	private final ResourceLoader resourceLoader;
	private final JAstParser parser;

	private final boolean includeSuperClass;
	private final boolean includeCompiledClasses;
	private final boolean includeFields;
	private final boolean includeSetters;
	private final boolean includeGetters;

	private final Matcher<String> propertyNameMatcher;

	public PropertiesExtractor(ResourceLoader resourceLoader,
			JAstParser parser, boolean includeSuperClass,
			boolean includeCompiledClasses, Matcher<String> propertyNameMatcher, boolean includeFields, boolean includeGetters, boolean includeSetters) {
		super();
		this.resourceLoader = resourceLoader;
		this.parser = parser;
		this.includeSuperClass = includeSuperClass;
		this.includeCompiledClasses = includeCompiledClasses;
		this.propertyNameMatcher = propertyNameMatcher;
		
		this.includeFields = includeFields;
		this.includeGetters = includeGetters;
		this.includeSetters = includeSetters;
	}

	public PojoModel extractProperties(JType pojoType) {
		// call request builder methods for each field/method exposed

		PojoModel parentModel = null;
		if (includeSuperClass) {
			String superType = pojoType.getSuperTypeFullName();

			if (superType != null && !Object.class.getName().equals(superType)) {
				RootResource r = resourceLoader
						.getResourceOrNullFromClassName(superType);
				if (r != null) {
					JSourceFile source = JSourceFile.fromResource(r, parser);
					JType parent = source.findTypesMatching(
							AJType.with().fullName(superType)).getFirstOrNull();
					parentModel = extractProperties(parent);
				} else {
					if (includeCompiledClasses) {
						Class<?> k = resourceLoader.loadClassOrNull(superType);
						if (k != null) {
							parentModel = extractProperties(k);
						}
					}
				}
			}
		}

		PojoModel model = new PojoModel(parentModel);

		if(includeFields){
			extractFields(pojoType, model);
		}
		if(includeGetters){
			extractGetters(pojoType, model);
		}
		if(includeSetters){
			extractSetters(pojoType, model);
		}
		return model;
	}

	private void extractFields(JType pojoType, PojoModel model) {
		List<JField> fields = pojoType.findFieldsMatching(AJField.with().modifier(AJModifier.that().isNotStatic().isNotNative())).toList();
		LOG.debug("found " + fields.size() + " potential source fields");
		int count = 0;
		for (JField field : fields) {
			String propertyName = field.getName();

			if (!isInclude(field, field.getName(), field.getAccess())) {
				LOG.debug("ignoring source field:" + field.getName());
				continue;
			}

			// TODO:add per property bind/veto override support. Use TriState?
			PojoProperty property = new PojoProperty(model, propertyName,
					field.getFullTypeName());

			property.setFieldName(field.getName());
			property.setFinalField(field.isFinal());

			model.addProperty(property);
			count++;
		}
		LOG.debug("added " + count + " source fields");
	}

	private void extractGetters(JType pojoType, PojoModel model) {
		List<JMethod> getters = pojoType
				.findMethodsMatching(getterMatcher).toList();
		int count = 0;
		LOG.debug("found " + getters.size() + " potential source getters");
		for (JMethod getter : getters) {
			String name = getName(getter);
			if (!isInclude(getter, name, getter.getModifiers().asAccess())) {
				LOG.debug("ignoring source getter:" + getter.getName());
				continue;
			}
			PojoProperty p = model.getProperty(name);
			if (p == null) {
				p = new PojoProperty(model, name,
						getter.getReturnTypeFullName());
				model.addProperty(p);
			}
			p.setPropertyGetterName(getter.getName());
			count++;
		}
		LOG.debug("added " + count + " source getters");
	}
	
	private void extractSetters(JType pojoType, PojoModel model) {
		List<JMethod> setters = pojoType.findMethodsMatching(setterMatcher).toList();
		LOG.debug("found " + setters.size() + " potential source setters");
		int count = 0;
		for (JMethod setter : setters) {
			String name = getName(setter);
			if (!isInclude(setter, name, setter.getModifiers().asAccess())) {
				LOG.debug("ignoring source setter:" + setter.getName());
				continue;
			}

			PojoProperty p = model.getProperty(name);
			if (p == null) {
				SingleVariableDeclaration arg = setter.getParameters()
						.iterator().next();
				String pType = NameUtil.resolveQualifiedName(arg.getType());
				p = new PojoProperty(model, name, pType);
				model.addProperty(p);
			}
			p.setPropertyGetterName(setter.getName());
			count++;
		}
		LOG.debug("added " + count + " source setters");
	}

	private PojoModel extractProperties(Class<?> requestType) {

		PojoModel parentModel = null;
		if (includeSuperClass) {
			Class<?> parent = requestType.getSuperclass();
			if (parent != null && parent != Object.class) {
				parentModel = extractProperties(requestType);
			}
		}
		PojoModel model = new PojoModel(parentModel);

		ReflectedClass pojoType = ReflectedClass.from(requestType);
		extractFields(pojoType, model);
		extractGetters(pojoType, model);
		extractSetters(pojoType, model);
		return model;
	}

	private void extractFields(ReflectedClass pojoType, PojoModel model) {
		List<Field> fields = pojoType.findFieldsMatching(AField.that().isNotStatic().isNotNative()).toList();
		LOG.trace("found " + fields.size() + " potential compiled fields");
		int count = 0;
		for (Field f : fields) {
			ReflectedField field = ReflectedField.from(f);
			if (!isInclude(field, field.getName())) {
				LOG.debug("ignoring compiled field:" + f.getName());
				continue;
			}

			PojoProperty property = new PojoProperty(model, f.getName(), f
					.getGenericType().getTypeName());

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
			String name = getName(m);
			ReflectedMethod getter = ReflectedMethod.from(m);
			
			if (!isInclude(getter, name)) {
				LOG.debug("ignoring compiled getter:" + getter.getName());
				continue;
			}
			PojoProperty p = model.getProperty(name);
			if (p == null) {
				p = new PojoProperty(model, name, getter.getUnderlying()
						.getReturnType().getName());
				model.addProperty(p);
			}
			p.setPropertyGetterName(getter.getName());
			count++;
		}
		LOG.trace("added " + count + " compiled getters");
	}

	private void extractSetters(ReflectedClass pojoType, PojoModel model) {
		List<Method> setters = pojoType.findMethodsMatching(setterMatcherCompiled).toList();
		LOG.trace("found " + setters.size() + " potential compiled setters");
		int count = 0;
		for (Method m : setters) {
			String name = getName(m);
			ReflectedMethod setter = ReflectedMethod.from(m);
			if (!isInclude(setter, name)) {
				LOG.debug("ignoring compiled setter:" + setter.getName());
				continue;
			}

			PojoProperty p = model.getProperty(name);
			if (p == null) {
				Class<?> argType = setter.getUnderlying().getParameterTypes()[0];
				p = new PojoProperty(model, name, argType.getName());
				model.addProperty(p);
			}
			p.setPropertyGetterName(setter.getName());
			count++;
		}
		LOG.trace("added " + count + " compiled setters");
	}

	private boolean isInclude(AnnotationsProvider provider, String name,
			JAccess access) {
		if (provider.getAnnotations().contains(sourceAnnotationIgnore)
				|| provider.getAnnotations().contains(NotAProperty.class)
				|| name.startsWith("_") || name.startsWith("$")) {
			return false;
		}
		if (propertyNameMatcher != null && !propertyNameMatcher.matches(name)) {
			return false;
		}
		return true;
	}

	private boolean isInclude(AbstractReflectedObject obj, String name) {
		if (obj.hasAnnotation(reflectedAnnotationIgnore)
				|| obj.hasAnnotation(NotAProperty.class)
				|| name.startsWith("_") || name.startsWith("$")) {
			return false;
		}
		if (propertyNameMatcher != null && !propertyNameMatcher.matches(name)) {
			return false;
		}
		return true;
	}

	
	private String getName(JMethod m){
		JAnnotation anon = m.getAnnotations().getOrNull(Property.class);
		if(anon != null){
			return anon.getAttributeValue("name").toString();
		}
		return BeanNameUtil.stripPrefix(m.getName());
	}

	private String getName(Method m){
		Property anon = m.getAnnotation(Property.class);
		if(anon != null){
			return anon.name();
		}
		return BeanNameUtil.stripPrefix(m.getName());
	}

	public static Builder with(ResourceLoader resourceLoader, JAstParser parser) {
		return new Builder(resourceLoader, parser);
	}

	public static class Builder {

		private ResourceLoader resourceLoader;
		private JAstParser parser;

		private boolean includeSuperClass = true;
		private boolean includeCompiledClasses = true;
		private boolean includeFields = true;
		private boolean includeSetters = true;
		private boolean includeGetters = true;
		
		private Matcher<String> propertyNameMatcher;

		@Inject
		public Builder(ResourceLoader resourceLoader, JAstParser parser) {
			this.resourceLoader = resourceLoader;
			this.parser = parser;
		}

		public PropertiesExtractor build() {
			return new PropertiesExtractor(resourceLoader, parser,
					includeSuperClass, includeCompiledClasses,
					propertyNameMatcher, includeFields, includeGetters, includeSetters);
		}

		public Builder resourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
			return this;
		}

		public Builder parser(JAstParser parser) {
			this.parser = parser;
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
