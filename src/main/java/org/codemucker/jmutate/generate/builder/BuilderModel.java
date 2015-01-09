package org.codemucker.jmutate.generate.builder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.generate.GenerateBuilder;
import org.codemucker.lang.ClassNameUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Holds the details about an individual pojo matcher
 */
public class BuilderModel {   

	public final String pojoPkg;
	public final String pojoTypeSimpleRaw;
	public final String pojoTypeSimple;
	public final String pojoTypeFull;
	public final String pojoTypeRaw;
	public final String pojoTypeGenericPart;
	
	public final String builderTypeSimple;
	public final String builderTypeSimpleRaw;
	public final String buildMethodName;
	public final boolean markGenerated;
	public final boolean generateStaticBuilderMethod;
    
    final Set<String> staticBuilderMethodNames;
    
    final Map<String, PropertyModel> properties = new LinkedHashMap<>();
	
    public BuilderModel(JType pojoType,GenerateBuilder options) {
    	this.pojoTypeFull = pojoType.getFullGenericName();
    	this.pojoTypeRaw = NameUtil.removeGenericOrArrayPart(pojoTypeFull);
    	this.pojoTypeGenericPart = Strings.nullToEmpty(NameUtil.extractGenericPartOrNull(pojoTypeFull));
    	
    	this.pojoTypeSimpleRaw = pojoType.getSimpleName();
    	this.pojoTypeSimple = pojoType.getSimpleName() + pojoTypeGenericPart;
    	
    	this.pojoPkg = ClassNameUtil.extractPkgPartOrNull(pojoTypeFull);
    	this.builderTypeSimpleRaw = "Builder";
    	this.builderTypeSimple = "Builder" + pojoTypeGenericPart;
        
        this.staticBuilderMethodNames = Sets.newHashSet(options.builderMethodNames());
        if(staticBuilderMethodNames.size()==0){
        	staticBuilderMethodNames.add("with");
        }
        this.buildMethodName = options.buildMethodName();
        this.markGenerated = options.markGenerated();
        this.generateStaticBuilderMethod = options.generateStaticBuilderMethod();
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