package org.codemucker.jmutate.generate.bean;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JField;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJField;
import org.codemucker.jmutate.generate.ModelUtils;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.bean.PropertiesGenerator.PropertiesOptions;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModel;
import org.codemucker.jpattern.bean.NotAProperty;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.GenerateProperties;

import com.google.inject.Inject;

/**
 * Generates property getters/setters for a bean, along with various bean
 * bindings if required
 */
public class PropertiesGenerator extends
		AbstractBeanGenerator<GenerateProperties, PropertiesOptions> {

	private static final Logger LOG = LogManager
			.getLogger(PropertiesGenerator.class);

	private JType bean;
	private PojoModel model;
	private PropertiesOptions options;

	@Inject
	public PropertiesGenerator(JMutateContext ctxt) {
		super(ctxt, GenerateProperties.class);
	}

	@Override
	protected void generate(JType bean, SmartConfig config, PojoModel model,
			PropertiesOptions options) {
		this.options = options;
		this.model = model;
		this.bean = bean;

		if (options.isEnabled()) {
			augmentModel();
			doGenerate();
		}
	}

	private void augmentModel() {
		for (PropertyModel p : model.getAllProperties()) {
			// TODO:add option to generate setter/getter individually? Or just
			// leave as and markup existing methods
			PropertyExtension ext = new PropertyExtension();
			ext.vetoable = options.vetoable;
			ext.bindable = options.bindable;
			ext.setterReturnType = options.chainedSetters ? options.getType().getFullNameRaw() : "void";
			ext.generateGetter = options.generateGetters;
			ext.generateSetter = options.generateSetters && !options.makeFinal && (p.hasField() && !p.isFinalField());
			ext.copyOnGet = options.copyOnGet;
			ext.copyOnSet = options.copyOnSet;

			if (p.getGetterName() == null && options.generateGetters) {
				p.setGetterName(p.getCalculatedGetterName());
			}

			if (p.getSetterName() == null && options.generateSetters) {
				p.setSetterName(p.getCalculatedSetterName());
			}
			if (p.getAddName() == null && options.generateSetters && options.generateAddRemoveMethods) {
				p.setAddName(p.getCalculatedAddName());
			}
			if (p.getRemoveName() == null && options.generateSetters && options.generateAddRemoveMethods) {
				p.setRemoveName(p.getCalculatedAddName());
			}
			if (p.getStaticPropertyNameFieldName() == null && options.generateStaticPropertyNameFields) { p.setStaticPropertyNameFieldName("PROP_" + p.getName().toUpperCase());
			}
			if (p.getType().isCollection() || p.getType().isMap()) {
				ext.makeCopyExpression = "new " + p.getConcreteType() + p.getType().getGenericPartOrEmpty();
			}

			ext.vetoPropertyNameAccessor = p.getStaticPropertyNameFieldName() == null ? ("\""
					+ p.getName() + "\"")
					: p.getStaticPropertyNameFieldName();

			p.set(PropertyExtension.class, ext);
		}
	}

	private void doGenerate() {
		generateNoArgCtor();
		generateStaticPropertyNames();

		for (PropertyModel property : model.getAllProperties()) {
			if (property.hasField()) {
				LOG.debug("processing property:'" + property.getName() + "'");
				generateFieldModifiers(property);
				generateGetter(property);
				generateSetter(property);
				generateCollectionAddRemove(property);
				generateMapAddRemove(property);
			}
		}

		generatePropertyChangeSupport();
		generateVetoableChangeSupport();
	}

	private void generateNoArgCtor() {
		if (options.generateNoArgCtor && !model.hasDirectFinalProperties()) {

			LOG.debug("generating no arg constructor");
			JMethod ctor = getContext()
					.newSourceTemplate()
					.pl("public " + options.getType().getSimpleNameRaw()
							+ "(){}").asConstructorSnippet();
			addMethod(bean, ctor.getAstNode(), options.isMarkGenerated());
		}
	}

	private void generateStaticPropertyNames() {
		// static property names
		if (options.generateStaticPropertyNameFields) {
			for (PropertyModel property : model.getAllProperties()) {
				JField staticField = getContext()
						.newSourceTemplate()
						.pl("public static final String "
								+ property.getStaticPropertyNameFieldName()
								+ " = \"" + property.getName() + "\";")
						.asJFieldSnippet();
				addField(bean, staticField.getAstNode(),
						options.isMarkGenerated());
			}
		}
	}

	private void generatePropertyChangeSupport() {
		if (options.bindable) {
			// TODO:check parent beans for existing property support
			SourceTemplate t = newSourceTemplate().var("change.name",
					options.propertyChangeSupportFieldName);

			SourceTemplate changeSupportField = t
					.child()
					.pl("@" + NotAProperty.class.getName())
					.pl("private final java.beans.PropertyChangeSupport ${change.name} = new java.beans.PropertyChangeSupport(this);");

			SourceTemplate addListener = t
					.child()
					.pl("public void addPropertyChangeListener(java.beans.PropertyChangeListener listener){")
					.pl("	this.${change.name}.addPropertyChangeListener(listener);")
					.pl("}");

			SourceTemplate addNamedListener = t
					.child()
					.pl("public void addPropertyChangeListener(String propertyName,java.beans.PropertyChangeListener listener){")
					.pl("	this.${change.name}.addPropertyChangeListener(propertyName,listener);")
					.pl("}");

			SourceTemplate removeListener = t
					.child()
					.pl("public void removePropertyChangeListener(java.beans.PropertyChangeListener listener){")
					.pl("	this.${change.name}.removePropertyChangeListener(listener);")
					.pl("}");

			addField(bean, changeSupportField.asFieldNodeSnippet(),
					options.isMarkGenerated());
			addMethod(bean, addListener.asMethodNodeSnippet(),
					options.isMarkGenerated());
			addMethod(bean, addNamedListener.asMethodNodeSnippet(),
					options.isMarkGenerated());
			addMethod(bean, removeListener.asMethodNodeSnippet(),
					options.isMarkGenerated());
		}
	}

	private void generateVetoableChangeSupport() {
		if (options.vetoable) {
			// TODO:check parent beans for existing property support
			SourceTemplate t = newSourceTemplate().var("veto.name",
					options.vetoableChangeSupportFieldName);

			SourceTemplate changeSupportField = t
					.child()
					.pl("@" + NotAProperty.class.getName())
					.pl("private final java.beans.VetoableChangeSupport ${veto.name} = new java.beans.VetoableChangeSupport(this);");

			SourceTemplate addListener = t
					.child()
					.pl("public void addVetoableChangeListener(java.beans.VetoableChangeListener listener){")
					.pl("	this.${veto.name}.addVetoableChangeListener(listener);")
					.pl("}");

			SourceTemplate addNamedListener = t
					.child()
					.pl("public void addVetoableChangeListener(String propertyName, java.beans.VetoableChangeListener listener){")
					.pl("	this.${veto.name}.addVetoableChangeListener(propertyName,listener);")
					.pl("}");

			SourceTemplate removeListener = t
					.child()
					.pl("public void removeVetoableChangeListener(java.beans.VetoableChangeListener listener){")
					.pl("	this.${veto.name}.removeVetoableChangeListener(listener);")
					.pl("}");

			addField(bean, changeSupportField.asFieldNodeSnippet(),
					options.isMarkGenerated());
			addMethod(bean, addListener.asMethodNodeSnippet(),
					options.isMarkGenerated());
			addMethod(bean, addNamedListener.asMethodNodeSnippet(),
					options.isMarkGenerated());
			addMethod(bean, removeListener.asMethodNodeSnippet(),
					options.isMarkGenerated());
		}
	}

	private void generateFieldModifiers(PropertyModel property) {
		if (property.isSuperClassProperty() || !property.hasField()) {
			return;
		}
		JField field = bean.findFieldsMatching(
				AJField.with().name(property.getFieldName())).getFirst();

		Access access = options.fieldAccess;
		if (access != Access.DEFAULT) {
			JAccess jaccess = ModelUtils.toJAccess(access);
			LOG.debug("ensuring " + jaccess.name() + " access for field "
					+ property.getFieldName());
			if (!field.getAccess().equals(access)) {
				field.getModifiers().setAccess(jaccess);
			}
		}

		if (options.makeFinal) {
			field.getModifiers().setFinal(true);
		}
	}

	private void generateGetter(PropertyModel property) {
		// getter
		PropertyExtension pExtension = property
				.getOrFail(PropertyExtension.class);
		if (!property.isSuperClassProperty()
				&& property.getGetterName() != null
				&& pExtension.generateGetter) {
			SourceTemplate getter = newSourceTemplate()
					.var("p.fieldName", property.getFieldName())
					.var("p.getterName", property.getGetterName())
					.var("p.type", property.getType().getFullName())
					.var("p.makeCopyExpression", pExtension.makeCopyExpression)

					.pl("public ${p.type} ${p.getterName}(){");
			if (property.getType().isImmutable() || !pExtension.copyOnGet || pExtension.makeCopyExpression == null) {
				getter.pl("		return ${p.fieldName};");
			} else {
				getter.pl("		return ${p.makeCopyExpression}(${p.fieldName});");		
			}
			getter.pl("}");

			addMethod(bean, getter.asMethodNodeSnippet(),
					options.isMarkGenerated());
		}
	}

	private void generateSetter(PropertyModel property) {
		// setter
		PropertyExtension pExtension = property
				.getOrFail(PropertyExtension.class);
		if (!property.isSuperClassProperty()
				&& property.getSetterName() != null
				&& pExtension.generateSetter) {

			SourceTemplate setter = newSourceTemplate()
					.var("p.fieldName", property.getFieldName())
					.var("p.name", property.getName())
					.var("p.vetoName", pExtension.vetoPropertyNameAccessor)
					.var("p.setterName", property.getSetterName())
					.var("p.type", property.getType().getFullName())
					.var("support.bind.name",
							options.propertyChangeSupportFieldName)
					.var("support.veto.name",
							options.vetoableChangeSupportFieldName)
					.var("return.type", pExtension.setterReturnType)
					.var("p.makeCopyExpression", pExtension.makeCopyExpression)
					.var("return", options.chainedSetters?"return this":"return")
						
					.p("public ${return.type} ${p.setterName}(final ${p.type} val)");

			if (pExtension.vetoable) {
				setter.p(" throws java.beans.PropertyVetoException ");
			}
			setter.pl("{");
			CharSequence vetoString = "";

			if (pExtension.vetoable) {
				vetoString = setter
						.child()
						.pl("   this.${support.veto.name}.fireVetoableChange(${p.vetoName},this.${p.fieldName},val);")
						.interpolateTemplate();
			}
			if (pExtension.bindable) {
				setter.pl("	${p.type} oldVal = this.${p.fieldName};").p(vetoString);
				if (property.getType().isImmutable() || !pExtension.copyOnSet || pExtension.makeCopyExpression == null) {
					setter.pl("	this.${p.fieldName} = val;");	
				} else {
					setter.pl("	this.${p.fieldName} = ${p.makeCopyExpression}(val);");
				}
				setter.pl("   this.${support.bind.name}.firePropertyChange(${p.vetoName},oldVal,val);");
			} else {
				setter.p(vetoString).pl("		this.${p.fieldName} = val;");
			}
			if (options.chainedSetters) {
				setter.pl("${return};");
			}
			setter.pl("}");

			addMethod(bean, setter.asMethodNodeSnippet(),options.isMarkGenerated());
		}
	}

	private void generateMapAddRemove(PropertyModel property) {
		PropertyExtension pExtension = property
				.getOrFail(PropertyExtension.class);
		if (!property.isSuperClassProperty() && !property.isReadOnly()
				&& property.hasField() && property.getType().isKeyed()
				&& options.generateAddRemoveMethods) {
			String keyType = wildcardToObject(property.getType().getIndexedKeyTypeNameOrNull());
			String valueType = wildcardToObject(property.getType().getIndexedValueTypeNameOrNull());

			SourceTemplate add = newSourceTemplate()
					.var("p.fieldName", property.getFieldName())
					.var("p.name", property.getName())
					.var("p.vetoName", pExtension.vetoPropertyNameAccessor)
					.var("p.addName", property.getAddName())
					.var("p.type", property.getType().getFullName())
					.var("p.newType", property.getConcreteType())
					.var("p.genericPart",property.getType().getGenericPartOrEmpty())
					.var("p.keyType",keyType)
					.var("p.valueType",valueType)
					.var("return.type", pExtension.setterReturnType)
					.var("return", options.chainedSetters?"return this":"return")
						
					.pl("public ${return.type} ${p.addName}(final ${p.keyType} key,final ${p.valueType} val){");

			if (!property.isFinalField()) {
				add.pl("	if(this.${p.fieldName} == null){ this.${p.fieldName} = new ${p.newType}${p.genericPart}(); }");
			}
			add.pl("	this.${p.fieldName}.put(key, val);");

			if (options.chainedSetters) {
				add.pl("${return};");
			}
			add.pl("}");

			addMethod(bean, add.asMethodNodeSnippet(),
					options.isMarkGenerated());

			SourceTemplate remove = newSourceTemplate()
					.var("p.fieldName", property.getFieldName())
					.var("p.name", property.getName())
					.var("p.removeName", property.getRemoveName())
					.var("p.type", property.getType().getFullName())
					.var("p.newType", property.getConcreteType())
					.var("p.keyType", keyType)
					.var("return.type", pExtension.setterReturnType)
					.var("return", options.chainedSetters?"return this":"return")
						
					.pl("public ${return.type} ${p.removeName}(final ${p.keyType} key){")
					.pl("	if(this.${p.fieldName} != null){ ")
					.pl("		this.${p.fieldName}.remove(key);")
					.pl("	}");

			if (options.chainedSetters) {
				remove.pl("${return};");
			}
			remove.pl("}");

			addMethod(bean, remove.asMethodNodeSnippet(),
					options.isMarkGenerated());
		}
	}

	private void generateCollectionAddRemove(PropertyModel property) {
		PropertyExtension pExtension = property.getOrFail(PropertyExtension.class);
		
		String valueType = wildcardToObject(property.getType().getIndexedValueTypeNameOrNull());

		if (!property.isSuperClassProperty() && !property.isReadOnly()
				&& property.hasField() && property.getType().isCollection()
				&& options.generateAddRemoveMethods) {
			{
				if ((pExtension.vetoable || pExtension.bindable)
						&& !property.getType().isList()) {
					throw new JMutateException(
							"Property "
									+ bean.getFullName()
									+ "."
									+ property.getName()
									+ " is marked as vetoable or bindable and it's a collection. "
									+ "It must be a list, map, or not an indexed property (we must be able to get the index of the current element). Instead have a collection. "
									+ "Mark the property as not vetoable or bindable, or don't generate add/remove methods.");
				}
				SourceTemplate add = newSourceTemplate()
						.var("p.fieldName", property.getFieldName())
						.var("p.name", property.getName())
						.var("p.vetoName", pExtension.vetoPropertyNameAccessor)
						.var("p.addName", property.getAddName())
						.var("p.type", property.getType().getFullName())
						.var("p.newType", property.getConcreteType())
						.var("p.genericPart",property.getType().getGenericPartOrEmpty())
						.var("p.valueType",valueType)
						.var("return", options.chainedSetters?"return this":"return")
						.var("support.bind.name",options.propertyChangeSupportFieldName)
						.var("support.veto.name",options.vetoableChangeSupportFieldName)
						.var("return.type", pExtension.setterReturnType)

						.p("public ${return.type} ${p.addName}(final ${p.valueType} val)");
				if (pExtension.vetoable) {
					add.p(" throws java.beans.PropertyVetoException ");
				}
				add.pl("{");

				if (!property.isFinalField()) {
					add.pl(" 	if(this.${p.fieldName} == null){ ")
							.pl("		this.${p.fieldName} = new ${p.newType}${p.genericPart}(); ");

					if (options.chainedSetters) {
						add.pl("	return this;");
					}
					add.pl("}");
				}

				CharSequence vetoString = "";

				if (pExtension.vetoable) {
					vetoString = add
							.child()
							.pl("   this.${support.veto.name}.fireVetoableChange(${p.vetoName},this.${p.fieldName},val);")
							.interpolateTemplate();
				}
				if (pExtension.bindable) {
					add.pl("	${p.type} oldVal = this.${p.fieldName};")
							.p(vetoString)
							.pl("	this.${p.fieldName}.add(val);")
							.pl("   this.${support.bind.name}.fireIndexedPropertyChange(${p.vetoName},this.${p.fieldName}.size()-1,oldVal,val);");
				} else {
					add.p(vetoString).pl("		this.${p.fieldName}.add(val);");
				}
				if (options.chainedSetters) {
					add.pl("${return};");
				}
				add.pl("}");

				addMethod(bean, add.asMethodNodeSnippet(),
						options.isMarkGenerated());
			}
			{
				SourceTemplate remove = newSourceTemplate()
						.var("p.fieldName", property.getFieldName())
						.var("p.name", property.getName())
						.var("p.vetoName", pExtension.vetoPropertyNameAccessor)
						.var("p.removeName", property.getRemoveName())
						.var("p.type", property.getType().getFullName())
						.var("p.newType", property.getConcreteType())
						.var("p.valueType", valueType)
						.var("return", options.chainedSetters?"return this":"return")
						.var("support.bind.name",options.propertyChangeSupportFieldName)
						.var("support.veto.name",options.vetoableChangeSupportFieldName)
						.var("return.type", pExtension.setterReturnType)
			
						.p("public ${return.type} ${p.removeName}(final ${p.valueType} val)");

				if (pExtension.vetoable) {
					remove.p(" throws java.beans.PropertyVetoException ");
				}
				remove.pl("{");
				remove.pl("	if(this.${p.fieldName} == null){ ${return}; }");

				CharSequence vetoString = "";
				CharSequence indexString = "";
				if (pExtension.vetoable || pExtension.bindable) {
					indexString = remove
							.child()
							.pl("int index = this.${p.fieldName}.indexOf(val);")
							.pl("if( index < 0 ){ ${return}; }")
							.interpolateTemplate();
				}
				if (pExtension.vetoable) {
					vetoString = remove
							.child()
							.pl("this.${support.veto.name}.fireVetoableChange(${p.vetoName},val,null);")
							.interpolateTemplate();
				}
				if (pExtension.bindable) {
					remove.p(indexString)
							.p(vetoString)
							.pl("this.${p.fieldName}.remove(val);")
							.pl("this.${support.bind.name}.fireIndexedPropertyChange(${p.vetoName},index,val,null);");
				} else {
					remove.p(indexString).p(vetoString)
							.pl("this.${p.fieldName}.remove(val);");
				}
				if (options.chainedSetters) {
					remove.pl("${return};");
				}
				remove.pl("}");

				addMethod(bean, remove.asMethodNodeSnippet(),
						options.isMarkGenerated());
			}
		}
	}
	
	private static String wildcardToObject(String s){
		return s == null?null:("?".equals(s)?"java.lang.Object":s);
	}

	@Override
	protected PropertiesOptions createOptionsFrom(Configuration config,
			JType type) {
		return new PropertiesOptions(config, type);
	}

	/**
	 * Extracted from the annotations
	 */
	public static class PropertiesOptions extends
			AbstractBeanOptions<GenerateProperties> {

		public Access fieldAccess;
		public String propertyChangeSupportFieldName = "_propertyChangeSupport";
		public String vetoableChangeSupportFieldName = "_vetoableSupport";
		public String setterPrefix;
		public boolean generateGetters;
		public boolean makeFinal;
		public boolean generateSetters;
		public boolean generateStaticPropertyNameFields;
		public boolean generateNoArgCtor;
		public boolean generateAddRemoveMethods;
		public boolean bindable;
		public boolean vetoable;
		public boolean chainedSetters;
		public boolean copyOnSet;
		public boolean copyOnGet;

		public PropertiesOptions(Configuration config, JType pojoType) {
			super(config, GenerateProperties.class, pojoType);
		}

	}

	/**
	 * Used to enhance property models with additional information
	 */
	private static class PropertyExtension {
		boolean generateSetter;
		boolean generateGetter;
		String setterReturnType;

		boolean vetoable;
		boolean bindable;
		boolean copyOnSet;
		boolean copyOnGet;

		String vetoPropertyNameAccessor;
		String makeCopyExpression;
	}
}