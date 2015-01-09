package org.codemucker.jmutate.generate.builder;

import org.codemucker.jmutate.generate.bean.AbstractPropertyModel;

public class PropertyModel extends AbstractPropertyModel {

	public final BuilderModel pojoModel;

	PropertyModel(BuilderModel parent, String fieldName, String propertyType) {
		super(fieldName, propertyType);
		this.pojoModel = parent;
	}

}