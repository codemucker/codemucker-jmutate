package org.codemucker.jmutate.generate.builder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.print.DocFlavor.STRING;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jpattern.generate.GenerateBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Holds the details about an individual pojo matcher
 */
public class BuilderModel {   

	public final TypeInfo type;
	public final String builderTypeSimple;
	public final String builderTypeSimpleRaw;
	public final String buildMethodName;
	public final boolean isPojoAbstract;
	public final boolean markGenerated;
	public final boolean generateStaticBuilderMethod;
	public final boolean isGeneric;
    
    final Set<String> staticBuilderMethodNames;
    
    final Map<String, PropertyModel> properties = new LinkedHashMap<>();
	
    public BuilderModel(JType pojoType,GenerateBuilder options) {
    	this.type = TypeInfo.newFromFullNameAndTypeBounds(pojoType.getFullGenericName(), pojoType.getTypeBoundsExpressionOrNull());
    	this.isGeneric = type.isGeneric;
		
    	this.builderTypeSimpleRaw = pojoType.isAbstract()?"AbstractBuilder":"Builder";
    	this.builderTypeSimple = builderTypeSimpleRaw + type.genericPartOrEmpty;
        
        this.staticBuilderMethodNames = Sets.newHashSet(options.builderCreateMethodNames());
        if(staticBuilderMethodNames.size()==0){
        	staticBuilderMethodNames.add("with");
        }
        this.buildMethodName = options.buildMethodName();
        this.markGenerated = options.markGenerated();
        this.generateStaticBuilderMethod = options.generateStaticBuilderCreateMethod();
        this.isPojoAbstract = pojoType.isAbstract();
    }
    
    void addField(PropertyModel field){
        if (hasNamedField(field.propertyName)) {
            throw new JMutateException("More than one property with the same name '%s' on %s", field.propertyName, type.fullName);
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