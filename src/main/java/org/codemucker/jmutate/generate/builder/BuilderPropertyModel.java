package org.codemucker.jmutate.generate.builder;

import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jmutate.generate.pojo.PojoProperty;

public class BuilderPropertyModel {

	private final BuilderModel pojoModel;
	private final boolean fromSuperClass;

	private final PojoProperty property;


	BuilderPropertyModel(BuilderModel parent, PojoProperty property,boolean superClass) {
		this.property = property;
		this.pojoModel = parent;
		this.fromSuperClass = superClass;
	}

	public BuilderModel getPojoModel() {
		return pojoModel;
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


}