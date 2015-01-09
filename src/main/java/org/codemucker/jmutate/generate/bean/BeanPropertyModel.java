package org.codemucker.jmutate.generate.bean;

public class BeanPropertyModel extends AbstractPropertyModel {

	public final BeanModel pojoModel;

	public final boolean generateGetter;
	public final boolean generateSetter;

	BeanPropertyModel(BeanModel parent, String fieldName, String propertyType,
			boolean generateSetter, boolean generateGetter) {
		super(fieldName, propertyType);
		this.pojoModel = parent;

		this.generateGetter = generateGetter;
		this.generateSetter = generateSetter;

	}
}