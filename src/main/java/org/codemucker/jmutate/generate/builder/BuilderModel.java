package org.codemucker.jmutate.generate.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.model.ModelObject;
import org.codemucker.jmutate.generate.model.TypeModel;
import org.codemucker.jpattern.generate.ClashStrategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BuilderModel extends ModelObject {   

    private static final Logger LOG = LogManager.getLogger(BuilderModel.class);
    
	private static final Comparator<BuilderPropertyModel> COMPARE_BY_NAME = new Comparator<BuilderPropertyModel>() {
		@Override
		public int compare(BuilderPropertyModel left, BuilderPropertyModel right) {
			return left.getPropertyName().compareTo(right.getPropertyName());
		}
	};
	
	private final ClashStrategy clashStrategy;
	private final String fieldNames;
	
	private final TypeModel pojoType;
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

    public BuilderModel(JType pojo,BuilderOptions options) {
    	
    	this.pojoType = TypeModel.newFromFullNameAndTypeBounds(pojo.getFullGenericName(), pojo.getTypeBoundsExpressionOrNull());
    	this.isGeneric = getPojoType().isGeneric();
    	
    	this.builderTypeSimpleRaw = pojo.isAbstract()?"AbstractBuilder":"Builder";
    	this.builderTypeSimple = getBuilderTypeSimpleRaw() + getPojoType().getGenericPartOrEmpty();
    	
        this.staticBuilderMethodNames = Sets.newHashSet(toList(options.getBuilderCreateMethodNames()));
        if(getStaticBuilderMethodNames().size()==0){
        	getStaticBuilderMethodNames().add("with");
        }
        this.buildPojoMethodName = options.getBuildMethodName();
        this.markGenerated = options.isMarkGenerated();
        this.markCtorArgsAsProperties = options.isMarkCtorArgsAsProperties();
        this.supportSubclassing = options.isSupportSubclassing();
        this.generateStaticBuilderMethod = options.isGenerateStaticBuilderCreateMethod();
        this.generateAddRemoveMethodsForIndexedProperties = options.isGenerateAddRemoveMethodsForIndexProperties();
        this.generateCopyBeanMethod = options.isGenerateCreateFromBean();
        
        this.inheritSuperBeanBuilder = options.isInheritSuperBeanBuilder();
        this.inheritSuperBeanProperties = options.isInheritSuperBeanProperties();
        this.generateStaticBuilderMethodOnBuilder = options.isGenerateStaticBuilderCreateMethodOnBuilder();
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
		
		this.clashStrategy = options.getClashStrategy();
		this.fieldNames = options.getFieldNames();
    }
    
	private static List<String> toList(String s) {
		if (s == null) {
			return new ArrayList<>();
		}
		String[] parts = s.split(",");
		List<String> list = new ArrayList<>(parts.length);
		for (String part : parts) {
			list.add(part);
		}
		return list;
	}

    void addProperty(BuilderPropertyModel property){
        if (hasPropertyNamed(property.getPropertyName())) {
            throw new JMutateException("More than one property with the same name '%s' on %s", property.getPropertyName(), getPojoType().getFullName());
        }
        if(LOG.isDebugEnabled()){
			LOG.debug("adding property '" + property.getPropertyName() + "', " + property);
		}
		
        properties.put(property.getPropertyName(), property);
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

	public TypeModel getPojoType() {
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

	public ClashStrategy getClashStrategy() {
		return clashStrategy;
	}

	public String getFieldNames() {
		return fieldNames;
	}
}