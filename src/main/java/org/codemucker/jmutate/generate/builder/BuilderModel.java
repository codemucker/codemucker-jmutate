package org.codemucker.jmutate.generate.builder;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jpattern.generate.GenerateBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BuilderModel {   

	private static final Comparator<BuilderPropertyModel> COMPARE_BY_NAME = new Comparator<BuilderPropertyModel>() {
		@Override
		public int compare(BuilderPropertyModel left, BuilderPropertyModel right) {
			return left.propertyName.compareTo(right.propertyName);
		}
	};
	
	private final TypeInfo pojoType;
	private final String builderTypeSimple;
	private final String builderTypeSimpleRaw;
	private final String builderTypeBoundsOrNull;
	private final String builderSelfAccessor;
	private final String builderSelfType;
	
	private final String buildPojoMethodName;
	
	private final boolean isGeneric;
	
	private final boolean markGenerated;
	private final boolean markCtorArgsAsProperties;
	
	private final boolean generateStaticBuilderMethod;
	private final boolean generateAddRemoveMethodsForIndexedProperties;
	private final boolean generateCopyBeanMethod;
	private final boolean generateStaticBuilderMethodOnBuilder;
	private final boolean inheritSuperBeanBuilder;
	private final boolean inheritSuperBeanProperties;
	private final boolean supportSubclassing;
	
    private final Set<String> staticBuilderMethodNames;
    private final Map<String, BuilderPropertyModel> properties = new LinkedHashMap<>();
	
    public BuilderModel(JType pojo,GenerateBuilder options) {
    	this.pojoType = TypeInfo.newFromFullNameAndTypeBounds(pojo.getFullGenericName(), pojo.getTypeBoundsExpressionOrNull());
    	this.isGeneric = getPojoType().isGeneric();
    	
    	this.builderTypeSimpleRaw = pojo.isAbstract()?"AbstractBuilder":"Builder";
    	this.builderTypeSimple = getBuilderTypeSimpleRaw() + getPojoType().getGenericPartOrEmpty();
    	
        this.staticBuilderMethodNames = Sets.newHashSet(options.builderCreateMethodNames());
        if(getStaticBuilderMethodNames().size()==0){
        	getStaticBuilderMethodNames().add("with");
        }
        this.buildPojoMethodName = options.buildMethodName();
        this.markGenerated = options.markGenerated();
        this.markCtorArgsAsProperties = options.markCtorArgsAsProperties();
        this.supportSubclassing = options.supportSubclassing() || pojo.isAbstract();
        this.generateStaticBuilderMethod = options.generateStaticBuilderCreateMethod() && !isSupportSubclassing();
        this.generateAddRemoveMethodsForIndexedProperties = options.generateAddRemoveMethodsForIndexProperties();
        this.generateCopyBeanMethod = options.generateCreateFromBean();
        
        this.inheritSuperBeanBuilder = options.inheritSuperBeanBuilder();
        this.inheritSuperBeanProperties = options.inheritSuperBeanProperties();
        this.generateStaticBuilderMethodOnBuilder = options.generateStaticBuilderCreateMethodOnBuilder();
		String self = "this";
		String selfType = getBuilderTypeSimple();
		String builderTypeBounds = getPojoType().getTypeBoundsOrNull();
		
		if(isSupportSubclassing()){
			if(getPojoType().getTypeBoundsOrNull()==null){
				selfType = getBuilderTypeSimpleRaw() + "<TSelf>";
				builderTypeBounds = "<TSelf extends " + getBuilderTypeSimpleRaw() + "<TSelf>>";		
			} else {
				String typeBounds = getPojoType().getTypeBoundsOrNull();
				typeBounds = typeBounds.substring(1, typeBounds.length()-1);//remove trailing/leading '<>'
				selfType = getBuilderTypeSimpleRaw() + "<TSelf," + getPojoType().getTypeBoundsNamesOrNull() + ">";
				builderTypeBounds = "<TSelf extends " + getBuilderTypeSimpleRaw() + "<TSelf," + getPojoType().getTypeBoundsNamesOrNull() + ">," + typeBounds+ ">";
			}
			self = "self()";
		}
		this.builderTypeBoundsOrNull = builderTypeBounds;
		this.builderSelfAccessor = self;
		this.builderSelfType = selfType;
    }
    
    void addProperty(BuilderPropertyModel field){
        if (hasPropertyNamed(field.propertyName)) {
            throw new JMutateException("More than one property with the same name '%s' on %s", field.propertyName, getPojoType().getFullName());
        }
        properties.put(field.propertyName, field);
    }
    
    boolean hasPropertyNamed(String name){
        return properties.containsKey(name);
    }
    
    BuilderPropertyModel getNamedProperty(String name){
        return properties.get(name);
    }
    
    List<BuilderPropertyModel> getProperties(){
        List<BuilderPropertyModel> list = Lists.newArrayList(properties.values());
        Collections.sort(list,COMPARE_BY_NAME);
        return list;
    }

	public TypeInfo getPojoType() {
		return pojoType;
	}

	public String getBuilderTypeSimple() {
		return builderTypeSimple;
	}

	public String getBuilderTypeSimpleRaw() {
		return builderTypeSimpleRaw;
	}

	public String getBuilderTypeBoundsOrNull() {
		return builderTypeBoundsOrNull;
	}

	public String getBuilderSelfAccessor() {
		return builderSelfAccessor;
	}

	public String getBuilderSelfType() {
		return builderSelfType;
	}

	public String getBuildPojoMethodName() {
		return buildPojoMethodName;
	}

	public boolean isGeneric() {
		return isGeneric;
	}

	public boolean isMarkGenerated() {
		return markGenerated;
	}

	public boolean isMarkCtorArgsAsProperties() {
		return markCtorArgsAsProperties;
	}

	public boolean isGenerateStaticBuilderMethod() {
		return generateStaticBuilderMethod;
	}

	public boolean isGenerateAddRemoveMethodsForIndexedProperties() {
		return generateAddRemoveMethodsForIndexedProperties;
	}

	public boolean isGenerateCopyBeanMethod() {
		return generateCopyBeanMethod;
	}

	public boolean isGenerateStaticBuilderMethodOnBuilder() {
		return generateStaticBuilderMethodOnBuilder;
	}

	public boolean isInheritSuperBeanBuilder() {
		return inheritSuperBeanBuilder;
	}

	public boolean isInheritSuperBeanProperties() {
		return inheritSuperBeanProperties;
	}

	public boolean isSupportSubclassing() {
		return supportSubclassing;
	}

	Set<String> getStaticBuilderMethodNames() {
		return staticBuilderMethodNames;
	}
}