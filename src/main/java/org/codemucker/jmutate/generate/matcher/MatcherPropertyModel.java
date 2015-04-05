package org.codemucker.jmutate.generate.matcher;

import org.codemucker.jmutate.util.NameUtil;

public class MatcherPropertyModel {
	private final MatcherModel pojoModel;
	private final String propertyName;
    private String propertyGetter;
    private final String propertyType;
    private final boolean isPrimitive;
    
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

	public MatcherModel getPojoModel() {
		return pojoModel;
	}

	public String getPropertyName() {
		return propertyName;
	}

	String getPropertyGetter() {
		return propertyGetter;
	}

	void setPropertyGetter(String propertyGetter) {
		this.propertyGetter = propertyGetter;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}  
}