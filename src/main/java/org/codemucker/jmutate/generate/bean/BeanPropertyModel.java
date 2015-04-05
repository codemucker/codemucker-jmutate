package org.codemucker.jmutate.generate.bean;

public class BeanPropertyModel extends AbstractPropertyModel {

	public final BeanModel pojoModel;

	private final boolean generateGetter;
	private final boolean generateSetter;
	private final boolean bindable;
	private final boolean vetoable;
	private final boolean fromSuperClass;


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
}