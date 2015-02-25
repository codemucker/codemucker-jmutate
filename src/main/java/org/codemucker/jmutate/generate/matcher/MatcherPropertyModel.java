package org.codemucker.jmutate.generate.matcher;

import org.codemucker.jmutate.util.NameUtil;

public class MatcherPropertyModel {
	public final MatcherModel pojoModel;
	public final String propertyName;
    String propertyGetter;
    public final String propertyType;
    public final boolean isPrimitive;
    
    /**
     * The object version of the property type if a primitive, else just the same as the property type
     */
    String propertyTypeAsObject;
    
    MatcherPropertyModel(MatcherModel parent, String fieldName, String propertyType) {
        this.pojoModel = parent;
        this.propertyName = fieldName;
        this.propertyType = NameUtil.compiledNameToSourceName(propertyType);
        this.propertyTypeAsObject = NameUtil.compiledNameToSourceName(NameUtil.primitiveToObjectType(propertyType));
        this.isPrimitive = NameUtil.isPrimitive(propertyType);
    }  
}