package org.codemucker.jmutate.generate.bean;

public class BeanPropertyModel extends AbstractPropertyModel {

	public final BeanModel pojoModel;

	public final boolean generateGetter;
	public final boolean generateSetter;
	public final boolean bindable;
	public final boolean vetoable;
	public final boolean fromSuperClass;


	BeanPropertyModel(BeanModel parent, String fieldName, String propertyType,
			boolean generateSetter, boolean generateGetter, boolean bindable, boolean vetoable,boolean fromSuperClass) {
		super(fieldName, propertyType);
		this.pojoModel = parent;

		this.generateGetter = generateGetter;
		this.generateSetter = generateSetter;

		this.vetoable = vetoable;
		this.bindable = bindable;
		
		this.fromSuperClass = fromSuperClass;
	}
}