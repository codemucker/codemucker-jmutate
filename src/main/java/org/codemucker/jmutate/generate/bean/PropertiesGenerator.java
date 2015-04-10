package org.codemucker.jmutate.generate.bean;

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
import org.codemucker.jpattern.bean.NotAProperty;
import org.codemucker.jpattern.bean.Property;
import org.codemucker.jpattern.generate.DontGenerate;
import org.codemucker.jpattern.generate.GenerateProperties;

import com.google.inject.Inject;

/**
 * Generates property getters/setters for a bean, along with various bean
 * bindings if required
 */
public class PropertiesGenerator extends AbstractBeanGenerator<GenerateProperties> {

	private static final Logger LOG = LogManager.getLogger(PropertiesGenerator.class);

	@Inject
	public PropertiesGenerator(JMutateContext ctxt) {
		super(ctxt);
	}

	@Override
	protected void generate(JType bean,BeanModel model) {
		generateNoArgCtor(bean, model);
		generateAllArgCtor(bean, model);
		generateStaticPropertyNames(bean, model);
		
		for (BeanPropertyModel property : model.getProperties()) {
			if(property.hasField()){
				LOG.debug("processing property:'" + property.getPropertyName() + "'");
				generateFieldModifiers(bean, model, property);	
				generateGetter(bean, model, property);
				generateSetter(bean, model, property);
				generateCollectionAddRemove(bean, model, property);
				generateMapAddRemove(bean, model, property);
			}
		}
		
		generatePropertyChangeSupport(bean, model);
		generateVetoableChangeSupport(bean, model);
	}

	private void generateNoArgCtor(JType bean, BeanModel model) {
		if(model.options.isGenerateNoArgCtor() && !model.hasDirectFinalProperties()){
			
			LOG.debug("generating no arg constructor");
			JMethod ctor = getCtxt()
				.newSourceTemplate()
				.pl("public " + model.options.getType().getSimpleNameRaw() + "(){}")
				.asConstructorSnippet();
			addMethod(bean, ctor.getAstNode(), model.options.isMarkGenerated());
		}
	}
	
	private void generateAllArgCtor(JType beanType, BeanModel model) {
		if(model.options.isGenerateAllArgCtor()){
			SourceTemplate beanCtor = getCtxt()
					.newSourceTemplate()
					.var("b.name", model.options.getType().getSimpleNameRaw())
					.pl("private ${b.name} (");
			
			boolean comma = false;
			//args
			for (BeanPropertyModel property : model.getProperties()) {
				if(!property.hasField()){
					continue;
				}
				if(comma){
					beanCtor.p(",");
				}
				if(model.options.isMarkCtorArgsAsProperties()){
					beanCtor.p("@" + Property.class.getName() + "(name=\"" + property.getPropertyName() + "\") ");
				}
				beanCtor.p(property.getType().getFullName() + " " + property.getPropertyName());
				comma = true;
			}
			
			beanCtor.pl("){");
			//field assignments
			for (BeanPropertyModel property : model.getProperties()) {
				if(property.isFromSuperClass()){
					beanCtor.pl(property.getPropertySetterName() + "(" + property.getPropertyName() + ");");
				} else {
					beanCtor.pl("this." + property.getPropertyName() + "=" + property.getPropertyName() + ";");
				}
			}
			beanCtor.pl("}");
			addMethod(beanType, beanCtor.asConstructorNodeSnippet(),model.options.isMarkGenerated());
		}
	}

	private void generateStaticPropertyNames(JType bean, BeanModel model) {
		//static property names
		if(model.options.isGenerateStaticPropertyNameFields()){
			for (BeanPropertyModel property : model.getProperties()) {
				JField staticField = getCtxt()
					.newSourceTemplate()
					.pl("public static final String PROP_" + property.getPropertyName().toUpperCase() + " = \"" + property.getPropertyName() +"\";")
					.asJFieldSnippet();
				addField(bean, staticField.getAstNode(), model.options.isMarkGenerated());
			}	
		}
	}

	private void generatePropertyChangeSupport(JType bean, BeanModel model) {
		if(model.options.isBindable()){
			//TODO:check parent beans for existing property support
			SourceTemplate t = newSourceTemplate()
				.var("change.name", model.options.getPropertyChangeSupportFieldName());
		
			SourceTemplate changeSupportField = t.child()
				.pl("@" + NotAProperty.class.getName())
				.pl("private final java.beans.PropertyChangeSupport ${change.name} = new java.beans.PropertyChangeSupport(this);");
		
			SourceTemplate addListener = t.child()
				.pl("public void addPropertyChangeListener(java.beans.PropertyChangeListener listener){")
				.pl("	this.${change.name}.addPropertyChangeListener(listener);")
				.pl("}");
		
			SourceTemplate addNamedListener = t.child()
				.pl("public void addPropertyChangeListener(String propertyName,java.beans.PropertyChangeListener listener){")
				.pl("	this.${change.name}.addPropertyChangeListener(propertyName,listener);")
				.pl("}");
			
			SourceTemplate removeListener = t.child()
				.pl("public void removePropertyChangeListener(java.beans.PropertyChangeListener listener){")
				.pl("	this.${change.name}.removePropertyChangeListener(listener);")
				.pl("}");

			addField(bean, changeSupportField.asFieldNodeSnippet(), model.options.isMarkGenerated());
			addMethod(bean, addListener.asMethodNodeSnippet(),model.options.isMarkGenerated());
			addMethod(bean, addNamedListener.asMethodNodeSnippet(),model.options.isMarkGenerated());
			addMethod(bean, removeListener.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}

	private void generateVetoableChangeSupport(JType bean, BeanModel model) {
		if(model.options.isVetoable()){
			//TODO:check parent beans for existing property support
			SourceTemplate t = newSourceTemplate()
				.var("veto.name", model.options.getVetoableChangeSupportFieldName());
		
			SourceTemplate changeSupportField = t.child()
				.pl("@" + NotAProperty.class.getName())
				.pl("private final java.beans.VetoableChangeSupport ${veto.name} = new java.beans.VetoableChangeSupport(this);");
		
			SourceTemplate addListener = t.child()
				.pl("public void addVetoableChangeListener(java.beans.VetoableChangeListener listener){")
				.pl("	this.${veto.name}.addVetoableChangeListener(listener);")
				.pl("}");
		
			SourceTemplate addNamedListener = t.child()
				.pl("public void addVetoableChangeListener(String propertyName, java.beans.VetoableChangeListener listener){")
				.pl("	this.${veto.name}.addVetoableChangeListener(propertyName,listener);")
				.pl("}");
			
			SourceTemplate removeListener = t.child()
				.pl("public void removeVetoableChangeListener(java.beans.VetoableChangeListener listener){")
				.pl("	this.${veto.name}.removeVetoableChangeListener(listener);")
				.pl("}");

			addField(bean, changeSupportField.asFieldNodeSnippet(), model.options.isMarkGenerated());
			addMethod(bean, addListener.asMethodNodeSnippet(),model.options.isMarkGenerated());
			addMethod(bean, addNamedListener.asMethodNodeSnippet(),model.options.isMarkGenerated());
			addMethod(bean, removeListener.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}

	
	private void generateFieldModifiers(JType bean, BeanModel model,BeanPropertyModel property) {
		if(property.isFromSuperClass() || !property.hasField()){
			return;
		}
		JAccess access = model.options.getFieldAccess();
		
		LOG.debug("ensuring " + access.name() + " access for field " + property.getFieldName());
		JField field = bean.findFieldsMatching(AJField.with().name(property.getFieldName())).getFirst();
		if(!field.getAccess().equals(access)){
			field.getJModifiers().setAccess(model.options.getFieldAccess());
		}
		
		if(model.options.isMakeReadonly() && (access == JAccess.PUBLIC ||access == JAccess.PACKAGE)){
			field.getJModifiers().setFinal(true);
		}
		
	}

	private void generateSetter(JType bean, BeanModel model,BeanPropertyModel property) {
		//setter
		if(!property.isFromSuperClass() && property.getPropertySetterName() != null && property.isGenerateSetter()){
			SourceTemplate setter = newSourceTemplate()
				.var("p.fieldName", property.getFieldName())
				.var("p.name", property.getPropertyName())
				.var("p.setterName", property.getPropertySetterName())
				.var("p.type", property.getType().getObjectTypeFullName())
				.var("support.bind.name", model.options.getPropertyChangeSupportFieldName())
				.var("support.veto.name", model.options.getVetoableChangeSupportFieldName())
				
				.p("public void ${p.setterName}(final ${p.type} val)");
			
				if(property.isVetoable()){
					setter.p(" throws java.beans.PropertyVetoException ");
				}
				setter.pl("{");
				CharSequence vetoString = "";
				
				if(property.isVetoable()){
					vetoString = setter.child()
					.pl("   this.${support.veto.name}.fireVetoableChange(\"${p.name}\",this.${p.fieldName},val);")
					.interpolateTemplate();
				}
				if(property.isBindable()){
					setter
						.pl("	${p.type} oldVal = this.${p.fieldName};")
						.p(vetoString)
						.pl("	this.${p.fieldName} = val;")
						.pl("   this.${support.bind.name}.firePropertyChange(\"${p.name}\",oldVal,val);");
				} else {
					setter
						.p(vetoString)
						.pl("		this.${p.fieldName} = val;");
				}
				setter.pl("}");
				
			addMethod(bean, setter.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}
	
	private void generateMapAddRemove(JType bean, BeanModel model,BeanPropertyModel property) {
		if(!property.isFromSuperClass() && !property.isReadOnly() && property.hasField() && property.getType().isKeyed() && model.options.isGenerateAddRemoveMethodsForIndexedProperties()){
			SourceTemplate add = newSourceTemplate()
				.var("p.fieldName", property.getFieldName())
				.var("p.name", property.getPropertyName())
				.var("p.addName", property.getPropertyAddName())
				.var("p.type", property.getType().getObjectTypeFullName())
				.var("p.newType", property.getPropertyConcreteType())
				.var("p.genericPart", property.getType().getGenericPartOrEmpty())
				.var("p.keyType", property.getType().getIndexedKeyTypeNameOrNull())
				.var("p.valueType", property.getType().getIndexedValueTypeNameOrNull())
					
				.pl("public void ${p.addName}(final ${p.keyType} key,final ${p.valueType} val){");
			
			if(!property.isFinalField()){
				add.pl("	if(this.${p.fieldName} == null){ this.${p.fieldName} = new ${p.newType}${p.genericPart}(); }");
			}
			add
				.pl("	this.${p.fieldName}.put(key, val);")
				.pl("}");
				
			addMethod(bean, add.asMethodNodeSnippet(),model.options.isMarkGenerated());
			
			SourceTemplate remove = newSourceTemplate()
				.var("p.fieldName", property.getFieldName())
				.var("p.name", property.getPropertyName())
				.var("p.removeName", property.getPropertyRemoveName())
				.var("p.type", property.getType().getObjectTypeFullName())
				.var("p.newType", property.getPropertyConcreteType())
				.var("p.keyType", property.getType().getIndexedKeyTypeNameOrNull())
				
				.pl("public void ${p.removeName}(final ${p.keyType} key){")
				.pl("	if(this.${p.fieldName} != null){ ")
				.pl("		this.${p.fieldName}.remove(key);")
				.pl("	}")
				
				.pl("}");
				
			addMethod(bean, remove.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}

	private void generateCollectionAddRemove(JType bean, BeanModel model,BeanPropertyModel property) {
		if(!property.isFromSuperClass() && !property.isReadOnly() && property.hasField() && property.getType().isCollection() && model.options.isGenerateAddRemoveMethodsForIndexedProperties()){
		{
			if((property.isVetoable() || property.isBindable()) && !property.getType().isList()){
				throw new JMutateException("Property " + bean.getFullName() + "." + property.getPropertyName() + " is marked as vetoable or bindable and it's a collection. "
						+ "It must be a list, map, or not an indexed property (we must be able to get the index of the current element). Instead have a collection. "
						+ "Mark the property as not vetoable or bindable, or don't generate add/remove methods.");
			}
				SourceTemplate add = newSourceTemplate()
					.var("p.fieldName", property.getFieldName())
					.var("p.name", property.getPropertyName())
					.var("p.addName", property.getPropertyAddName())
					.var("p.type", property.getType().getObjectTypeFullName())
					.var("p.newType", property.getPropertyConcreteType())
					.var("p.genericPart", property.getType().getGenericPartOrEmpty())
					.var("p.valueType", property.getType().getIndexedValueTypeNameOrNull())
					.var("support.bind.name", model.options.getPropertyChangeSupportFieldName())
					.var("support.veto.name", model.options.getVetoableChangeSupportFieldName())
					
					.p("public void ${p.addName}(final ${p.valueType} val)");
					if(property.isVetoable()){
						add.p(" throws java.beans.PropertyVetoException ");
					}
					add.pl("{");
						
					if(!property.isFinalField()){
						add
						.pl(" 	if(this.${p.fieldName} == null){ ")
						.pl("		this.${p.fieldName} = new ${p.newType}${p.genericPart}(); ")
						.pl("	}");	
					}
				
				CharSequence vetoString = "";
				
				if(property.isVetoable()){
					vetoString = add.child()
					.pl("   this.${support.veto.name}.fireIndexedVetoableChange(\"${p.name}\",this.${p.fieldName}.size(),this.${p.fieldName},val);")
					.interpolateTemplate();
				}
				if(property.isBindable()){
					add
						.pl("	${p.type} oldVal = this.${p.fieldName};")
						.p(vetoString)
						.pl("	this.${p.fieldName}.add(val);")
						.pl("   this.${support.bind.name}.fireIndexedPropertyChange(\"${p.name}\",this.${p.fieldName}.size()-1,oldVal,val);");
				} else {
					add
						.p(vetoString)
						.pl("		this.${p.fieldName}.add(val);");
				}
				add.pl("}");
					
				addMethod(bean, add.asMethodNodeSnippet(),model.options.isMarkGenerated());
			}
			{
				SourceTemplate remove = newSourceTemplate()
					.var("p.fieldName", property.getFieldName())
					.var("p.name", property.getPropertyName())
					.var("p.removeName", property.getPropertyRemoveName())
					.var("p.type", property.getType().getObjectTypeFullName())
					.var("p.newType", property.getPropertyConcreteType())
					.var("p.valueType", property.getType().getIndexedValueTypeNameOrNull())
					.var("support.bind.name", model.options.getPropertyChangeSupportFieldName())
					.var("support.veto.name", model.options.getVetoableChangeSupportFieldName())
					
					.p("public void ${p.removeName}(final ${p.valueType} val)");
				
					if(property.isVetoable()){
						remove.p(" throws java.beans.PropertyVetoException ");
					}
					remove.pl("{");
					remove.pl("	if(this.${p.fieldName} != null){ ");
				
				CharSequence vetoString = "";
				CharSequence indexString = "";
				if(property.isVetoable() || property.isBindable()){
					indexString = remove.child()
							.pl("int index = this.${p.fieldName}.indexOf(val);")
							.pl("if( index < 0 ){ return; }")
							.interpolateTemplate();
				}
				if(property.isVetoable()){
					vetoString = remove.child()
					.pl("   this.${support.veto.name}.fireIndexedVetoableChange(\"${p.name}\",index,val,null);")
					.interpolateTemplate();
				}
				if(property.isBindable()){
					remove
						.p(indexString)
						.p(vetoString)
						.pl("	this.${p.fieldName}.remove(val);")
						.pl("   this.${support.bind.name}.fireIndexedPropertyChange(\"${p.name}\",index,val,null);");
				} else {
					remove
						.p(indexString)
						.p(vetoString)
						.pl("	this.${p.fieldName}.remove(val);");
				}
				remove.pl("	}");
				remove.pl("}");
				
				addMethod(bean, remove.asMethodNodeSnippet(),model.options.isMarkGenerated());
			}
		}
	}

	private void generateGetter(JType bean, BeanModel model, BeanPropertyModel property) {
		//getter
		if(!property.isFromSuperClass() && property.getPropertyGetterName() != null && property.isGenerateGetter()){
			SourceTemplate getter = newSourceTemplate()
				.var("p.fieldName", property.getFieldName())
				.var("p.getterName", property.getPropertyGetterName())
				.var("p.type", property.getType().getObjectTypeFullName())
				
				.pl("public ${p.type} ${p.getterName}(){")
				.pl("		return ${p.fieldName};")
				.pl("}");
				
			addMethod(bean, getter.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}

	@Override
	protected GenerateProperties getAnnotation() {
		return Defaults.class.getAnnotation(GenerateProperties.class);
	}
	
	@DontGenerate
	@GenerateProperties
	private static class Defaults {}
}