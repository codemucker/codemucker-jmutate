package org.codemucker.jmutate.generate.bean;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.GenerateBean;
import org.codemucker.lang.ClassNameUtil;

/**
 * Holds the details about an individual pojo matcher
 */
public class BeanModel {   

	public final String pojoPkg;
	public final String pojoTypeSimple;
	public final String pojoTypeFull;
	public final boolean markGenerated;
	public final JAccess fieldAccess;
	public final boolean generateHashCodeEquals;
	public final boolean makeReadonly;
	public final boolean generateStaticPropertyNameFields;
	public final boolean generateNoArgCtor;
    
    final Map<String, PropertyModel> properties = new LinkedHashMap<>();
	
    public BeanModel(JType pojoType,GenerateBean options) {
    	this.pojoTypeSimple = pojoType.getSimpleName();
    	this.pojoTypeFull = pojoType.getFullName();
    	this.pojoPkg = ClassNameUtil.extractPkgPartOrNull(pojoTypeFull);    
    	this.markGenerated = options.markGenerated();
    	this.fieldAccess = toJAccess(options.fieldAccess());
    	this.generateHashCodeEquals = options.generateHashCodeAndEqualsMethod();
    	this.makeReadonly = options.readonlyProperties();
    	this.generateStaticPropertyNameFields = options.generateStaticPropertyNameFields();
    	this.generateNoArgCtor = options.generateNoArgCtor();
    }
    
	private static JAccess toJAccess(Access access) {
		switch (access) {
		case PACKAGE:
			return JAccess.PACKAGE;
		case PRIVATE:
			return JAccess.PRIVATE;
		case PROTECTED:
			return JAccess.PROTECTED;
		case PUBLIC:
			return JAccess.PUBLIC;
		default:
			throw new IllegalArgumentException("unknown value:" + access.name());
		}
	}

    void addField(PropertyModel field){
        if (hasNamedField(field.propertyName)) {
            throw new JMutateException("More than one property with the same name '%s' on %s", field.propertyName, pojoTypeFull);
        }
        properties.put(field.propertyName, field);
    }
    
    boolean hasNamedField(String name){
        return properties.containsKey(name);
    }
    
    PropertyModel getNamedField(String name){
        return properties.get(name);
    }
    
    Collection<PropertyModel> getFields(){
        return properties.values();
    }
}