package org.codemucker.jmutate.generate.bean;

import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.BeanNameUtil;

public class PropertyModel {
	public final BeanModel pojoModel;
	public final String propertyName;
	public final String propertyGetterName;
	public final String propertySetterName;
    
    public final String propertyType;
    public final boolean isPrimitive;
    public final boolean isString;
    
    /**
     * The object version of the property type if a primitive, else just the same as the property type
     */
    String propertyTypeAsObject;
    
    PropertyModel(BeanModel parent, String fieldName, String propertyType, boolean generateSetter, boolean generateGetter) {
        this.pojoModel = parent;
        this.propertyName = fieldName;
        this.propertyType = NameUtil.compiledNameToSourceName(propertyType);
        this.propertyTypeAsObject = NameUtil.compiledNameToSourceName(NameUtil.primitiveToObjectType(propertyType));
        this.isPrimitive = NameUtil.isPrimitive(propertyType);
        this.isString = propertyType.equals("String") || propertyType.equals("java.lang.String");  
        this.propertyGetterName = generateGetter?BeanNameUtil.toGetterName(fieldName, NameUtil.isBoolean(propertyType)):null;
        this.propertySetterName = generateSetter?BeanNameUtil.toSetterName(fieldName):null;
    }  
}