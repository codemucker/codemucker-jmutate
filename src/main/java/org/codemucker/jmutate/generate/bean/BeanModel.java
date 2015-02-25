package org.codemucker.jmutate.generate.bean;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.GenerateBean;

public class BeanModel {   

	public final boolean markGenerated;
	public final boolean markCtorArgsAsProperties;
	public final JAccess fieldAccess;
	public final boolean generateHashCodeEquals;
	public final boolean generateCloneMethod;
	public final boolean makeReadonly;
	public final boolean generateStaticPropertyNameFields;
	public final boolean generateNoArgCtor;
	public final boolean generateAllArgCtor;
	public final boolean generateToString;
	public final boolean generateAddRemoveMethodsForIndexedProperties;
	public final boolean inheritSuperClassProperties;
	public final String  propertyChangeSupportFieldName = "_propertyChangeSupport";
	public final String  vetoableChangeSupportFieldName = "_vetoableSupport";
	
	public final boolean bindable;
	public final boolean vetoable;
	
	
	public final TypeInfo type;
    final Map<String, BeanPropertyModel> properties = new LinkedHashMap<>();
	
    public BeanModel(JType pojoType,GenerateBean options) {
    	this.type = TypeInfo.newFromFullNameAndTypeBounds(pojoType.getFullGenericName(), pojoType.getTypeBoundsExpressionOrNull());

    	this.markGenerated = options.markGenerated();
    	this.markCtorArgsAsProperties = options.markCtorArgsAsProperties();
    	this.fieldAccess = toJAccess(options.fieldAccess());
    	this.generateHashCodeEquals = options.generateHashCodeAndEqualsMethod();
    	this.generateAddRemoveMethodsForIndexedProperties = options.generateAddRemoveMethodsForIndexProperties();
    	this.generateToString = options.generateToString();
    	this.makeReadonly = options.readonlyProperties();
    	this.generateStaticPropertyNameFields = options.generateStaticPropertyNameFields();
    	this.generateNoArgCtor = options.generateNoArgCtor();
    	this.generateAllArgCtor = options.generateAllArgCtor();
    	this.generateCloneMethod = options.generateCloneMethod();
    	this.inheritSuperClassProperties = options.inheritSuperClassProperties();
    	this.bindable = options.bindable();
    	this.vetoable = options.vetoable();
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

    void addField(BeanPropertyModel field){
        if (hasNamedField(field.propertyName)) {
            throw new JMutateException("More than one property with the same name '%s' on %s", field.propertyName, type.fullName);
        }
        properties.put(field.propertyName, field);
    }
    
    boolean hasNamedField(String name){
        return properties.containsKey(name);
    }
    
    BeanPropertyModel getNamedField(String name){
        return properties.get(name);
    }
    
    Collection<BeanPropertyModel> getFields(){
        return properties.values();
    }
}