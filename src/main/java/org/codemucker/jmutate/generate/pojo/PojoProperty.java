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
    
	public final PojoModel definedIn;
	
    /**
     * The object version of the property type if a primitive, else just the same as the property type
     */
    
    public PojoProperty(PojoModel definedIn, String fieldName, String propertyType) {
        
    	propertyType = NameUtil.compiledNameToSourceName(propertyType);
    	
    	this.definedIn = definedIn;
    	this.propertyName = fieldName;
    	this.propertyNameSingular = PluralToSingularConverter.INSTANCE.pluralToSingle(fieldName);
        
    	this.type = TypeInfo.newFromFullNameAndTypeBounds(propertyType, null);
    	this.propertyGetterName = BeanNameUtil.toGetterName(fieldName, NameUtil.isBoolean(propertyType));
        this.propertySetterName = BeanNameUtil.toSetterName(fieldName);
        this.propertyConcreteType = IndexedTypeRegistry.INSTANCE.getConcreteTypeFor(type.getFullNameRaw());
        this.propertyAddName = type.isIndexed()?BeanNameUtil.addPrefixName("add",propertyNameSingular):null;
        this.propertyRemoveName = type.isIndexed()?BeanNameUtil.addPrefixName("remove",propertyNameSingular):null;
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
		return finalField || propertySetterName == null;
	}

	public boolean hasField() {
		return fieldName != null;
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

	@Override
	public String toString() {
		return "PojoProperty [propertyName=" + propertyName
				+ ", propertyNameSingular=" + propertyNameSingular
				+ ", propertyConcreteType=" + propertyConcreteType
				+ ", fieldName=" + fieldName + ", finalField=" + finalField
				+ ", propertyGetterName=" + propertyGetterName
				+ ", propertySetterName=" + propertySetterName
				+ ", propertyAddName=" + propertyAddName
				+ ", propertyRemoveName=" + propertyRemoveName + ", definedIn="
				+ definedIn + ", type=" + type + "]";
	}
    
	

}