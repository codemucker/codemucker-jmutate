package org.codemucker.jmutate.generate.builder;

import org.codemucker.jmutate.generate.bean.AbstractPropertyModel;

public class BuilderPropertyModel extends AbstractPropertyModel {

	public final BuilderModel pojoModel;
	public final boolean fromSuperClass;

	BuilderPropertyModel(BuilderModel parent, String fieldName, String propertyType,boolean superClass) {
		super(fieldName, propertyType);
		this.pojoModel = parent;
		this.fromSuperClass = superClass;
	}

}