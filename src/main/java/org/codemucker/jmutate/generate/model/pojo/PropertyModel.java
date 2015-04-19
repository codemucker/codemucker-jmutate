package org.codemucker.jmutate.generate.model.pojo;

import org.codemucker.jmutate.generate.model.ModelObject;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jmutate.generate.util.IndexedTypeRegistry;
import org.codemucker.jmutate.generate.util.PluralToSingularConverter;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jmutate.util.TypeUtils;
import org.codemucker.lang.BeanNameUtil;

public class PropertyModel extends ModelObject {
	
	private final TypeModel type;
	
	private final String name;
	private final String nameSingular;
	private String concreteType;
	private String fieldName;
	private boolean finalField;
	
	private String getterName;
	private String setterName;
    private String addName;
    private String removeName;

	private String calculatedGetterName;
	private String calculatedSetterName;
    private String calculatedAddName;
    private String calculatedRemoveName;
    /**
     * If a bean has a 'static final String PROP_FOO = "foo"; field, set this to 'PROP_FOO'. 
     */
    private String staticPropertyNameFieldName;
    
    
	public final PojoModel definedIn;
	
	private final int level;
    /**
     * The object version of the property type if a primitive, else just the same as the property type
     */
    
    public PropertyModel(PojoModel definedIn, String propertyName, String propertyType) {
        
    	propertyType = NameUtil.compiledNameToSourceName(propertyType);
    	
    	this.level = definedIn.getLevel();
    	this.definedIn = definedIn;
    	this.name = propertyName;
    	this.nameSingular = PluralToSingularConverter.INSTANCE.pluralToSingle(propertyName);
        
    	this.type = new TypeModel(propertyType, null);
    	this.getterName = null;// = BeanNameUtil.toGetterName(propertyName, NameUtil.isBoolean(propertyType));
        this.setterName = null;//BeanNameUtil.toSetterName(propertyName);
        this.addName = null;// type.isIndexed()?BeanNameUtil.addPrefixName("add",propertyNameSingular):null;
        this.removeName = null;//type.isIndexed()?BeanNameUtil.addPrefixName("remove",propertyNameSingular):null;
        this.concreteType = IndexedTypeRegistry.INSTANCE.getConcreteTypeFor(type.getFullNameRaw());
        
        calculateNames();
    } 
    
    private void calculateNames(){
    	this.calculatedGetterName = BeanNameUtil.toGetterName(name, TypeUtils.isBoolean(type.getFullName()));
        this.calculatedSetterName = BeanNameUtil.toSetterName(name);
        this.calculatedAddName = type.isIndexed()?BeanNameUtil.addPrefixName("add",nameSingular):null;
        this.calculatedRemoveName = type.isIndexed()?BeanNameUtil.addPrefixName("remove",nameSingular):null;
    }
    
    /**
     * Return the way we get this property 
     * @return
     */
	public String getInternalAccessor() {
		if(!hasGetter() && !hasField()){
			return null;
		}
		if (isSuperClassProperty()) {
			return addIfNotNull(getGetterName(),"()");
		}
		if (hasField()) {
			return getFieldName();
		} else {
			return addIfNotNull(getGetterName(),"()");
		}
	}
	
	private static String addIfNotNull(String s, String suffix){
		return s==null?null:s+suffix;
	}
	
    public boolean isSuperClassProperty(){
    	return level > 0;
    }

    public String getName() {
		return name;
	}

	public String getNameSingular() {
		return nameSingular;
	}

	public String getConcreteType() {
		return concreteType;
	}

	public void setConcreteType(String propertyConcreteType) {
		this.concreteType = propertyConcreteType;
	}

	public boolean isReadOnly() {
		return isFinalField() || !(hasField() || hasSetter());
	}

	public boolean hasField() {
		return fieldName != null;
	}

	public boolean hasGetter() {
		return getterName != null;
	}

	public boolean hasSetter() {
		return setterName != null;
	}

	public void setFieldName(String name) {
		this.fieldName = name;
	}

	public boolean isFinalField() {
		return finalField;
	}

	public void setFinalField(boolean finalField) {
		this.finalField = finalField;
	}

	public TypeModel getType() {
		return type;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getGetterName() {
		return getterName;
	}

	public void setGetterName(String propertyGetterName) {
		this.getterName = propertyGetterName;
	}

	public String getSetterName() {
		return setterName;
	}

	public void setSetterName(String propertySetterName) {
		this.setterName = propertySetterName;
	}

	public String getAddName() {
		return addName;
	}

	public void setAddName(String propertyAddName) {
		this.addName = propertyAddName;
	}

	public String getRemoveName() {
		return removeName;
	}

	public void setRemoveName(String propertyRemoveName) {
		this.removeName = propertyRemoveName;
	}
	
	public String getCalculatedGetterName() {
		return calculatedGetterName;
	}

	public String getCalculatedSetterName() {
		return calculatedSetterName;
	}

	public String getCalculatedAddName() {
		return calculatedAddName;
	}

	public String getCalculatedRemoveName() {
		return calculatedRemoveName;
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append("propertyName=").append(name);
		sb.append(", propertyNameSingular=").append(nameSingular);
		sb.append(", propertyConcreteType=").append(concreteType);
		sb.append(", fieldName=").append(fieldName);	
		sb.append(", finalField=" + finalField);
		sb.append(", type=" + type);
		sb.append(", propertyGetterName=").append(getterName);
		sb.append(", propertySetterName=").append(setterName);
		sb.append(", propertyAddName=").append(addName);
		sb.append(", propertyRemoveName=").append(removeName);
		sb.append(", calculatedPropertyGetterName=").append(calculatedGetterName);
		sb.append(", calculatedPropertySetterName=").append(calculatedSetterName);
		sb.append(", calculatedPropertyAddName=").append(calculatedAddName);
		sb.append(", calculatedPropertyRemoveName=").append(calculatedRemoveName);
	}

	public String getStaticPropertyNameFieldName() {
		return staticPropertyNameFieldName;
	}

	public void setStaticPropertyNameFieldName(
			String staticPropertyNameFieldName) {
		this.staticPropertyNameFieldName = staticPropertyNameFieldName;
	}

}