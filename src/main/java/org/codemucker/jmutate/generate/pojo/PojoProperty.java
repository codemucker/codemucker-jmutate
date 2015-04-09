package org.codemucker.jmutate.generate.pojo;

import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jmutate.generate.util.IndexedTypeRegistry;
import org.codemucker.jmutate.generate.util.PluralToSingularConverter;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.BeanNameUtil;

public class PojoProperty {
	private final String propertyName;
	private final String propertyNameSingular;
	private String propertyConcreteType;
	private String fieldName;
	private boolean finalField;
	
	private final TypeInfo type;
	
	private String propertyGetterName;
	private String propertySetterName;
    private String propertyAddName;
    private String propertyRemoveName;

	private String calculatedPropertyGetterName;
	private String calculatedPropertySetterName;
    private String calculatedPropertyAddName;
    private String calculatedPropertyRemoveName;
    
	public final PojoModel definedIn;
	
    /**
     * The object version of the property type if a primitive, else just the same as the property type
     */
    
    public PojoProperty(PojoModel definedIn, String propertyName, String propertyType) {
        
    	propertyType = NameUtil.compiledNameToSourceName(propertyType);
    	
    	this.definedIn = definedIn;
    	this.propertyName = propertyName;
    	this.propertyNameSingular = PluralToSingularConverter.INSTANCE.pluralToSingle(propertyName);
        
    	this.type = TypeInfo.newFromFullNameAndTypeBounds(propertyType, null);
    	this.propertyGetterName = null;// = BeanNameUtil.toGetterName(propertyName, NameUtil.isBoolean(propertyType));
        this.propertySetterName = null;//BeanNameUtil.toSetterName(propertyName);
        this.propertyAddName = null;// type.isIndexed()?BeanNameUtil.addPrefixName("add",propertyNameSingular):null;
        this.propertyRemoveName = null;//type.isIndexed()?BeanNameUtil.addPrefixName("remove",propertyNameSingular):null;
        this.propertyConcreteType = IndexedTypeRegistry.INSTANCE.getConcreteTypeFor(type.getFullNameRaw());
        
        recalculate();
    } 
    
    private void recalculate(){
    	this.calculatedPropertyGetterName = BeanNameUtil.toGetterName(propertyName, NameUtil.isBoolean(type.getFullName()));
        this.calculatedPropertySetterName = BeanNameUtil.toSetterName(propertyName);
        this.calculatedPropertyAddName = type.isIndexed()?BeanNameUtil.addPrefixName("add",propertyNameSingular):null;
        this.calculatedPropertyRemoveName = type.isIndexed()?BeanNameUtil.addPrefixName("remove",propertyNameSingular):null;
    }

    public String getPropertyName() {
		return propertyName;
	}

	public String getPropertyNameSingular() {
		return propertyNameSingular;
	}

	public String getPropertyConcreteType() {
		return propertyConcreteType;
	}

	public void setPropertyConcreteType(String propertyConcreteType) {
		this.propertyConcreteType = propertyConcreteType;
	}

	public boolean isReadOnly() {
		return isFinalField() || !(hasField() || hasSetter());
	}

	public boolean hasField() {
		return fieldName != null;
	}

	public boolean hasGetter() {
		return propertyGetterName != null;
	}

	public boolean hasSetter() {
		return propertySetterName != null;
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

	public TypeInfo getType() {
		return type;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getPropertyGetterName() {
		return propertyGetterName;
	}

	public void setPropertyGetterName(String propertyGetterName) {
		this.propertyGetterName = propertyGetterName;
	}

	public String getPropertySetterName() {
		return propertySetterName;
	}

	public void setPropertySetterName(String propertySetterName) {
		this.propertySetterName = propertySetterName;
	}

	public String getPropertyAddName() {
		return propertyAddName;
	}

	public void setPropertyAddName(String propertyAddName) {
		this.propertyAddName = propertyAddName;
	}

	public String getPropertyRemoveName() {
		return propertyRemoveName;
	}

	public void setPropertyRemoveName(String propertyRemoveName) {
		this.propertyRemoveName = propertyRemoveName;
	}
	
	public String getCalculatedPropertyGetterName() {
		return calculatedPropertyGetterName;
	}

	public String getCalculatedPropertySetterName() {
		return calculatedPropertySetterName;
	}

	public String getCalculatedPropertyAddName() {
		return calculatedPropertyAddName;
	}

	public String getCalculatedPropertyRemoveName() {
		return calculatedPropertyRemoveName;
	}

	@Override
	public String toString() {
		return "PojoProperty [propertyName=" + propertyName
				+ ", propertyNameSingular=" + propertyNameSingular
				+ ", propertyConcreteType=" + propertyConcreteType
				+ ", fieldName=" + fieldName + ", finalField=" + finalField
				+ ", type=" + type + ", propertyGetterName="
				+ propertyGetterName + ", propertySetterName="
				+ propertySetterName + ", propertyAddName=" + propertyAddName
				+ ", propertyRemoveName=" + propertyRemoveName
				+ ", calculatedPropertyGetterName="
				+ calculatedPropertyGetterName
				+ ", calculatedPropertySetterName="
				+ calculatedPropertySetterName + ", calculatedPropertyAddName="
				+ calculatedPropertyAddName + ", calculatedPropertyRemoveName="
				+ calculatedPropertyRemoveName + "]";
	}


	

}