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

	public boolean isFinalField() {
		return property.isFinalField();
	}

	public TypeInfo getType() {
		return property.getType();
	}

	public String getPropertyGetterName() {
		return property.getPropertyGetterName();
	}

	public String getPropertySetterName() {
		return property.getPropertySetterName();
	}

	public String getPropertyAddName() {
		return property.getPropertyAddName();
	}

	public String getPropertyRemoveName() {
		return property.getPropertyRemoveName();
	}

	@Override
	public String toString() {
		return "BeanPropertyModel [fromSuperClass=" + fromSuperClass
				+ ", generateGetter=" + generateGetter + ", generateSetter="
				+ generateSetter + ", bindable=" + bindable + ", vetoable="
				+ vetoable + ", property=" + property + "]";
	}
	
	

}