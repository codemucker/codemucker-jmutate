package org.codemucker.jmutate.generate.matcher;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.ClassNameUtil;

import com.google.common.collect.Sets;

/**
 * Holds the details about an individual pojo matcher
 */
public class MatcherModel {   
	
	public final String pojoTypeFull;
	public final String matcherTypeFull;
	public final String matcherTypeSimple;
    public final String pkg;
    
    final Set<String> staticBuilderMethodNames;
    
    final Map<String, PropertyModel> properties = new LinkedHashMap<>();
    
    public MatcherModel(AllMatchersModel parent, Class<?> pojoType) {
    	this.pojoTypeFull = NameUtil.compiledNameToSourceName(pojoType);
    	this.pkg = extractPkgName(parent,pojoType.getName());
    	this.matcherTypeFull = toMatcherName(pkg,NameUtil.compiledNameToSourceName(pojoType));
        this.matcherTypeSimple = ClassNameUtil.extractSimpleClassNamePart(matcherTypeFull);
        this.staticBuilderMethodNames = Sets.newHashSet(parent.options.builderMethodNames());
    }
    
    public MatcherModel(AllMatchersModel parent, JType pojoType) {
    	this.pojoTypeFull = pojoType.getFullName();
    	this.pkg = extractPkgName(parent,pojoTypeFull);
    	this.matcherTypeFull = toMatcherName(pkg,pojoType.getFullName());
        this.matcherTypeSimple = ClassNameUtil.extractSimpleClassNamePart(matcherTypeFull);
        this.staticBuilderMethodNames = Sets.newHashSet(parent.options.builderMethodNames());
    }
    
    private String extractPkgName(AllMatchersModel parent,String pojoFullName){
    	return parent.defaultPackage!=null?parent.defaultPackage:ClassNameUtil.extractPkgPartOrNull(pojoFullName);
    }
    
    private static String toMatcherName(String pkg,String fullPojoName){
    	String className = ClassNameUtil.extractSimpleClassNamePart(fullPojoName);
    	
    	if (MatcherGenerator.VOWELS_UPPER.indexOf(className.charAt(0)) != -1) {
			className = "An" + className;
		} else {
			className = "A" + className;
		}
		if (pkg != null) {
			return pkg + "." + className;
		}
		return className;
    }
    
    void addField(PropertyModel field){
        if (hasNamedField(field.propertyName)) {
            throw new JMutateException("More than one property with the same param name '%s' on %s", field.propertyName, matcherTypeFull);
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