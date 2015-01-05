package org.codemucker.jmutate.generate.builder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.generate.GenerateBuilder;
import org.codemucker.lang.ClassNameUtil;

import com.google.common.collect.Sets;

/**
 * Holds the details about an individual pojo matcher
 */
public class BuilderModel {   

	public final String pojoPkg;
	public final String pojoTypeSimple;
	public final String pojoTypeFull;
	public final String builderTypeSimple;
	public final String buildMethodName;
	public final boolean markGenerated;
	
    final Set<String> staticBuilderMethodNames;
    
    final Map<String, PropertyModel> properties = new LinkedHashMap<>();
    
    public BuilderModel(JType pojoType,GenerateBuilder options) {
    	this.pojoTypeSimple = pojoType.getSimpleName();
    	this.pojoTypeFull = pojoType.getFullName();
    	this.pojoPkg = ClassNameUtil.extractPkgPartOrNull(pojoTypeFull);
        this.builderTypeSimple = "Builder";
        this.staticBuilderMethodNames = Sets.newHashSet(options.builderMethodNames());
        if(staticBuilderMethodNames.size()==0){
        	staticBuilderMethodNames.add("with");
        }
        this.buildMethodName = options.buildMethodName();
        this.markGenerated = options.markGenerated();
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