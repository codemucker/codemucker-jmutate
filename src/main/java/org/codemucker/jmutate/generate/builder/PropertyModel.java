package org.codemucker.jmutate.generate.builder;

import org.codemucker.jmutate.util.NameUtil;

public class PropertyModel {
	public final BuilderModel pojoModel;
	public final String propertyName;
    String propertyGetter;
    public final String propertyType;
    public final boolean isPrimitive;
    
    /**
     * The object version of the property type if a primitive, else just the same as the property type
     */
    String propertyTypeAsObject;
    
    PropertyModel(BuilderModel parent, String fieldName, String propertyType) {
        this.pojoModel = parent;
        this.propertyName = fieldName;
        this.propertyType = NameUtil.compiledNameToSourceName(propertyType);
        this.propertyTypeAsObject = NameUtil.compiledNameToSourceName(NameUtil.primitiveToObjectType(propertyType));
        this.isPrimitive = NameUtil.isPrimitive(propertyType);
    }  
}