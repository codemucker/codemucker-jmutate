package org.codemucker.jmutate.generate.matcher;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.ClassNameUtil;

/**
 * Holds the details about an individual pojo matcher
 */
public class MatcherModel {   
	
	private final String pojoTypeFull;
	private final String matcherTypeFull;
	private final String matcherTypeSimple;
    private final String pkg;
    private final boolean keepInSync;
	private final boolean markGenerated;
	private final Set<String> staticBuilderMethodNames;
	
	
    private final Map<String, MatcherPropertyModel> properties = new LinkedHashMap<>();
    
    public MatcherModel(AllMatchersModel parent, Class<?> pojoType) {
    	this.pojoTypeFull = NameUtil.compiledNameToSourceName(pojoType);
    	this.pkg = extractPkgName(parent,pojoType.getName());
    	this.matcherTypeFull = toMatcherName(getPkg(),NameUtil.compiledNameToSourceName(pojoType));
        this.matcherTypeSimple = ClassNameUtil.extractSimpleClassNamePart(getMatcherTypeFull());
 
        this.keepInSync = parent.isKeepInSync();
        this.markGenerated = parent.isMarkGenerated();
        this.staticBuilderMethodNames = parent.getStaticBuilderMethodNames();
 
    }
    
    public MatcherModel(AllMatchersModel parent, JType pojoType) {
    	this.pojoTypeFull = pojoType.getFullName();
    	this.pkg = extractPkgName(parent,getPojoTypeFull());
    	this.matcherTypeFull = toMatcherName(getPkg(),pojoType.getFullName());
        this.matcherTypeSimple = ClassNameUtil.extractSimpleClassNamePart(getMatcherTypeFull());
        
        this.keepInSync = parent.isKeepInSync();
        this.markGenerated = parent.isMarkGenerated();
        this.staticBuilderMethodNames = parent.getStaticBuilderMethodNames();
 
    }
    
    private String extractPkgName(AllMatchersModel parent,String pojoFullName){
    	return parent.getDefaultPackage()!=null?parent.getDefaultPackage():ClassNameUtil.extractPkgPartOrNull(pojoFullName);
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
    
    void addField(MatcherPropertyModel field){
        if (hasNamedField(field.getPropertyName())) {
            throw new JMutateException("More than one property with the same param name '%s' on %s", field.getPropertyName(), getMatcherTypeFull());
        }
        properties.put(field.getPropertyName(), field);
    }
    
    boolean hasNamedField(String name){
        return properties.containsKey(name);
    }
    
    MatcherPropertyModel getNamedField(String name){
        return properties.get(name);
    }
    
    Collection<MatcherPropertyModel> getFields(){
        return properties.values();
    }

	public String getPojoTypeFull() {
		return pojoTypeFull;
	}

	public String getMatcherTypeFull() {
		return matcherTypeFull;
	}

	public String getMatcherTypeSimple() {
		return matcherTypeSimple;
	}

	public String getPkg() {
		return pkg;
	}
	
	public boolean isKeepInSync() {
		return keepInSync;
	}

	public boolean isMarkGenerated() {
		return markGenerated;
	}

	public Set<String> getStaticBuilderMethodNames() {
		return staticBuilderMethodNames;
	}

	Map<String, MatcherPropertyModel> getProperties() {
		return properties;
	}
}