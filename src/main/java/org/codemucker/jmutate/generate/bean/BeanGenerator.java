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
import org.codemucker.jpattern.generate.GenerateBean;
import org.codemucker.jpattern.generate.DisableGenerators;

import com.google.inject.Inject;

/**
 * Generates property getters/setters for a bean, along with various bean
 * bindings if required
 */
public class BeanGenerator extends AbstractBeanGenerator<GenerateBean> {

	private static final Logger LOG = LogManager.getLogger(BeanGenerator.class);

	/**
	 * Used to select a reproducible starting prime when generating a hash code. Rather than pick a random one at each generation causing churn in the code, pick out of this array in a deterministic way based
	 * on bean name
	 */
	private static final int[] FIRST_PRIMES = new int[]{31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151,157,163,167,173,179,181,191,193,197,199,211,223,227,229,233,239,241,251,257,263,269,271,277,281,283,293,307,311,313,317,331,337,347,349,353,359,367,373,379,383,389,397,401,409,419,421,431,433,439,443,449,457,461,463,467,479,487,491,499,503,509,521,523,541,547,557,563,569,571,577,587,593,599,601,607,613,617,619,631,641,643,647,653,659,661,673,677,683,691,701,709,719,727,733,739,743,751,757,761,769,773,787,797,809,811,821,823,827,829,839,853,857,859,863,877,881,883,887,907,911,919,929,937,941,947,953,967,971,977,983,991,997,1009,1013};

	@Inject
	public BeanGenerator(JMutateContext ctxt) {
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
		
		generateToString(bean, model);
		generateEquals(bean, model);
		generateHashCode(bean, model);
		generateClone(bean, model);
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

	private void generateClone(JType bean, BeanModel model) {
		if(model.options.isGenerateCloneMethod() && !bean.isAbstract()){
			LOG.debug("adding method 'newInstanceOf'");

			SourceTemplate clone = newSourceTemplate()
				.var("b.type", model.options.getType().getSimpleName())
				.var("b.genericPart", model.options.getType().getGenericPartOrEmpty())
				.var("b.typeBounds", model.options.getType().getTypeBoundsOrEmpty())
				
				
				.pl("public static ${b.typeBounds} ${b.type} newInstanceOf(${b.type} bean){")
				.pl("if(bean == null){ return null;}")
				.pl("final ${b.type} clone = new ${b.type}();");
			
			for (BeanPropertyModel property : model.getProperties()) {
				SourceTemplate t = clone
					.child()
					.var("p.name",property.getPropertyName())
					.var("p.getter",property.getPropertyGetterName())
					.var("p.setter",property.getPropertySetterName())
					.var("p.type",property.getType().getFullName())
					.var("p.concreteType",property.getPropertyConcreteType())
					.var("p.rawType",property.getType().getFullNameRaw())
					.var("p.genericPart",property.getType().getGenericPartOrEmpty())
					
					;
				if(property.getType().isPrimitive()){
					if(property.isFromSuperClass()){
						t.pl("	clone.${p.setter}(bean.${p.getter}());");			
					} else {
						t.pl("	clone.${p.name} = bean.${p.name};");
					}
				} else if(property.getType().isArray()){
					if(property.isFromSuperClass()){
						t.pl("	if(bean.${p.getter}() == null){");
						t.pl("		clone.${p.setter}(null);");
						t.pl("	} else {");
						t.pl("		${p.rawType}[] src = bean.${p.getter}();");
						t.pl("		${p.rawType}[] copy = new ${p.rawType}[src.length];");
						t.pl("		System.arraycopy(src,0,copy,0,src.length);");
						t.pl("		clone.${p.setter}(copy);");
						t.pl("	}");
						
					} else {
						t.pl("	if(bean.${p.name} == null){");
						t.pl("		clone.${p.name} = null;");
						t.pl("	} else {");
						t.pl("		clone.${p.name} = new ${p.rawType}[bean.${p.name}.length];");
						t.pl("		System.arraycopy(bean.${p.name},0,clone.${p.name},0,bean.${p.name}.length);");
						t.pl("	}");
					}
				} else if(property.getType().isIndexed()){
					if(property.isFromSuperClass()){
						//TODO:add a safe copy util in here. codemucker.lang?
						t.pl("	clone.${p.setter}(bean.${p.getter}()} == null?null:new ${p.concreteType}${p.genericPart}(bean.${p.getter}());");
					} else {
						t.pl("	clone.${p.name} = bean.${p.name} == null?null:new ${p.concreteType}${p.genericPart}(bean.${p.name});");
					}
				} else {
			//		if(hasClassGotMethod(property.propertyTypeRaw, AString.matchingAntPattern("*newInstanceOf"))){
					//	t.pl("	clone.${p.name} = bean.${p.name} == null?null:${p.rawType}.newInstanceOf(bean.${p.name});");
				//	} else {
					if(property.isFromSuperClass()){
						t.pl("	clone.${p.setter}(bean.${p.getter}());");
					} else {
						t.pl("	clone.${p.name} = bean.${p.name};");
					}
					//}
				}
						
				clone.add(t);
			}
			clone.pl("return clone;");
		
			clone.pl("}");
			
			addMethod(bean, clone.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}

	private void generateHashCode(JType bean, BeanModel model) {
		if(model.options.isGenerateHashCodeMethod() && !model.getProperties().isEmpty()){
			int startingPrime = pickStartingPrimeForClass(model.options.getType().getFullName());
			SourceTemplate hashcode = newSourceTemplate()
				.var("prime", startingPrime)
				.pl("@java.lang.Override")
				.pl("public int hashCode(){");
				
			if(model.getProperties().isEmpty()){
				hashcode.pl("return super.hashCode();");
			} else {
				hashcode.pl("final int prime = ${prime};");
				hashcode.pl("int result = super.hashCode();");
				for (BeanPropertyModel property : model.getProperties()) {
					SourceTemplate t = hashcode
						.child()
						.var("p.accessor",property.getInternalAccessor());
					
					if(property.getType().isPrimitive() && !property.getType().isString()){
						//from the book 'Effective Java'
						if(property.getType().is("boolean")){
							t.pl("result = prime * result + (${p.accessor} ? 1:0);");
						} else if(property.getType().is("byte") || property.getType().is("char") || property.getType().is("int")){
							t.pl("result = prime * result + ${p.accessor};");
						} else if(property.getType().is("long")){
							t.pl("result = prime * result + (int) (${p.accessor} ^ (${p.accessor} >>> 32));");
						} else if(property.getType().is("float")){
							t.pl("result = prime * result + java.lang.Float.floatToIntBits(${p.accessor});");
						} else if(property.getType().is("double")){
							t.pl("result = prime * result + java.lang.Double.doubleToLongBits(${p.accessor});");
						} else  {
							t.pl("result = prime * result + ${p.accessor}.hashCode();");			
						}
					} else {
						t.pl("result = prime * result + ((${p.accessor} == null) ? 0 : ${p.accessor}.hashCode());");
					}
					hashcode.add(t);
				}
				hashcode.pl("return result;");
			}
			
			hashcode.pl("}");
			
			addMethod(bean, hashcode.asMethodNodeSnippet(),model.options.isMarkGenerated());
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

	
	private void generateEquals(JType bean, BeanModel model) {
		if(model.options.isGenerateEqualsMethod() && !model.getProperties().isEmpty()){
			
			SourceTemplate equals = newSourceTemplate()
					.var("b.type", model.options.getType().getSimpleName())
					.pl("@java.lang.Override")
					.pl("public boolean equals(final Object obj){")
					.pl("if (this == obj) return true;")
					.pl("if (!super.equals(obj) || getClass() != obj.getClass()) return false;");
			
			if(!model.getProperties().isEmpty()){
				equals.pl("${b.type} other = (${b.type}) obj;");
				for (BeanPropertyModel property : model.getProperties()) {
					if(!((property.isFromSuperClass() && property.hasGetter()) || (!property.isFromSuperClass() && property.hasField()))){
						continue;
					}
					SourceTemplate  t = equals
						.child()
						.var("p.accessor",property.getInternalAccessor());
					
					if(property.getType().isPrimitive() && !property.getType().isString()){
						t.pl("if (${p.accessor} != other.${p.accessor}) return false;");
					} else {
						t.pl("if(${p.accessor} == null) {")
						.pl("	if (other.${p.accessor} != null)")
						.pl("		return false;")
						.pl("} else if (!${p.accessor}.equals(other.${p.accessor}))")
						.pl("	return false;");
					}
					equals.add(t);
				}
			}
			equals.pl("	return true;");
			equals.pl("}");
			

			addMethod(bean, equals.asMethodNodeSnippet(),model.options.isMarkGenerated());
		}
	}

	private void generateToString(JType bean, BeanModel model) {
		if(model.options.isGenerateToString()){
			StringBuilder sb = new StringBuilder();
			sb.append("\" [");
			if(model.getProperties().isEmpty()){
				sb.append("]\"");
			} else {
				boolean comma = false;
				for (BeanPropertyModel property : model.getProperties()) {
					if(!(property.hasField() || property.hasGetter())){
						continue;
					}
					if(comma){
						sb.append(" + \",");
					}
					sb.append(property.getPropertyName() ).append("=\" + ");
					sb.append(property.getInternalAccessor());
					comma = true;
				}
				sb.append(" + \"]\"");
			}
			
			SourceTemplate toString = newSourceTemplate()
				.pl("@java.lang.Override")
				.pl("public String toString(){")
				.pl("return this.getClass().getName() + \"@\" + System.identityHashCode(this) + " + sb.toString() + ";")
				.pl("}");
			addMethod(bean, toString.asMethodNodeSnippet(),model.options.isMarkGenerated());
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

	@Override
	protected GenerateBean getAnnotation() {
		return Defaults.class.getAnnotation(GenerateBean.class);
	}
	
	@DisableGenerators
	@GenerateBean
	private static class Defaults {}
}