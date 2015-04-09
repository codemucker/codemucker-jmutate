package org.codemucker.jmutate.generate.bean;

import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jmutate.generate.pojo.PojoProperty;

public class BeanPropertyModel {

	public final BeanModel beanModel;

	private final boolean generateGetter;
	private final boolean generateSetter;
	private final boolean bindable;
	private final boolean vetoable;
	private final boolean fromSuperClass;

	private final PojoProperty property;

	BeanPropertyModel(BeanModel parent, PojoProperty property,
			boolean generateSetter, boolean generateGetter, boolean bindable,
			boolean vetoable, boolean fromSuperClass) {

		this.beanModel = parent;

		this.generateGetter = generateGetter;
		this.generateSetter = generateSetter;

		this.vetoable = vetoable;
		this.bindable = bindable;

		this.fromSuperClass = fromSuperClass;

		this.property = property;
	}

	public PojoProperty getProperty() {
		return property;
	}

	public boolean isGenerateGetter() {
		return generateGetter;
	}

	public boolean isGenerateSetter() {
		return generateSetter;
	}

	public boolean isBindable() {
		return bindable;
	}

	public boolean isVetoable() {
		return vetoable;
	}

	public boolean isFromSuperClass() {
		return fromSuperClass;
	}

	public String getPropertyName() {
		return property.getPropertyName();
	}

	public String getPropertyNameSingular() {
		return property.getPropertyNameSingular();
	}

	public String getPropertyConcreteType() {
		return property.getPropertyConcreteType();
	}

	public boolean isReadOnly() {
		return property.isReadOnly();
	}

	public boolean hasField() {
		return property.hasField();
	}

	public String getFieldName() {
		return property.getFieldName();
	}

	public String getInternalAccessor() {
		if (fromSuperClass) {
			return addIfNotNull(getPropertyGetterName(),"()");
		}
		if (hasField()) {
			return getFieldName();
		} else {
			return addIfNotNull(getPropertyGetterName(),"()");
		}
	}
	
	private static String addIfNotNull(String s, String suffix){
		return s==null?null:s+suffix;
	}

	public boolean isFinalField() {
		return property.isFinalField();
	}

	public TypeInfo getType() {
		return property.getType();
	}

	public boolean hasGetter() {
		return property.hasGetter() || generateGetter;
	}

	public boolean hasSetter() {
		return property.hasGetter() || generateSetter;
	}
	
	public String getPropertyGetterName() {
		String name = property.getPropertyGetterName();
		if(name == null){
			if(generateGetter){
				name = property.getCalculatedPropertyGetterName();
			}
		}
		return name;
	}

	public String getPropertySetterName() {
		String name = property.getPropertySetterName();
		if(name == null){
			if(generateSetter){
				name = property.getCalculatedPropertySetterName();
			}
		}
		return name;
	}

	public String getPropertyAddName() {
		String name = property.getPropertyAddName();
		if(name == null){
			if(generateSetter){
				name = property.getCalculatedPropertyAddName();
			}
		}
		return name;
	}

	public String getPropertyRemoveName() {
		String name = property.getPropertyRemoveName();
		if(name == null){
			if(generateSetter){
				name = property.getCalculatedPropertyRemoveName();
			}
		}
		return name;
	}

	@Override
	public String toString() {
		return "BeanPropertyModel [fromSuperClass=" + fromSuperClass
				+ ", generateGetter=" + generateGetter + ", generateSetter="
				+ generateSetter + ", bindable=" + bindable + ", vetoable="
				+ vetoable + ", property=" + property + "]";
	}
	
	

}