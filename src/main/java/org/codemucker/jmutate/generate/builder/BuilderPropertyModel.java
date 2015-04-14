package org.codemucker.jmutate.generate.builder;

import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModel;

public class BuilderPropertyModel {

	private final BuilderModel pojoModel;
	private final boolean fromSuperClass;

	private final PropertyModel property;

	BuilderPropertyModel(BuilderModel parent, PropertyModel property,boolean superClass) {
		this.property = property;
		this.pojoModel = parent;
		this.fromSuperClass = superClass;
	}

	public BuilderModel getPojoModel() {
		return pojoModel;
	}

	public boolean hasGetter() {
		return property.hasGetter();
	}

	public boolean hasSetter() {
		return property.hasSetter();
	}

	public String getFieldName() {
		return property.getFieldName();
	}
	
	public boolean isFromSuperClass() {
		return fromSuperClass;
	}
	
	public String getPropertyName() {
		return property.getName();
	}

	public String getPropertyNameSingular() {
		return property.getNameSingular();
	}

	public String getPropertyConcreteType() {
		return property.getConcreteType();
	}

	public boolean isReadOnly() {
		return !isWriteable();
	}
	
	public boolean isWriteable() {
		if(fromSuperClass){
			return property.hasSetter();
		}
		return property.hasField() || property.hasSetter();
	}

	public boolean hasField() {
		return property.hasField();
	}

	public boolean isFinalField() {
		return property.isFinalField();
	}

	public TypeModel getType() {
		return property.getType();
	}

	public String getPropertyGetterName() {
		return property.getGetterName();
	}

	public String getPropertySetterName() {
		return property.getSetterName();
	}

	public String getPropertyAddName() {
		return property.getAddName();
	}

	public String getPropertyRemoveName() {
		return property.getRemoveName();
	}


}