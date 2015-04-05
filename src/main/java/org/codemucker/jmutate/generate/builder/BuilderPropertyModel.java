package org.codemucker.jmutate.generate.builder;

import org.codemucker.jmutate.generate.bean.AbstractPropertyModel;

public class BuilderPropertyModel extends AbstractPropertyModel {

	private final BuilderModel pojoModel;
	private final boolean fromSuperClass;

	BuilderPropertyModel(BuilderModel parent, String fieldName, String propertyType,boolean superClass) {
		super(fieldName, propertyType);
		this.pojoModel = parent;
		this.fromSuperClass = superClass;
	}

	public BuilderModel getPojoModel() {
		return pojoModel;
	}

	public boolean isFromSuperClass() {
		return fromSuperClass;
	}

}