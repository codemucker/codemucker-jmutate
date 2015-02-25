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
	
	public final TypeInfo pojoType;
	public final String builderTypeSimple;
	public final String builderTypeSimpleRaw;
	public final String builderTypeBoundsOrNull;
	public final String builderSelfAccessor;
	public final String builderSelfType;
	
	public final String buildPojoMethodName;
	
	public final boolean isGeneric;
	
	public final boolean markGenerated;
	public final boolean markCtorArgsAsProperties;
	
	public final boolean generateStaticBuilderMethod;
	public final boolean generateAddRemoveMethodsForIndexedProperties;
	public final boolean generateCopyBeanMethod;
	public final boolean generateStaticBuilderMethodOnBuilder;
	public final boolean inheritSuperBeanBuilder;
	public final boolean inheritSuperBeanProperties;
	public final boolean supportSubclassing;
	
    final Set<String> staticBuilderMethodNames;
    final Map<String, BuilderPropertyModel> properties = new LinkedHashMap<>();
	
    public BuilderModel(JType pojo,GenerateBuilder options) {
    	this.pojoType = TypeInfo.newFromFullNameAndTypeBounds(pojo.getFullGenericName(), pojo.getTypeBoundsExpressionOrNull());
    	this.isGeneric = pojoType.isGeneric;
    	
    	this.builderTypeSimpleRaw = pojo.isAbstract()?"AbstractBuilder":"Builder";
    	this.builderTypeSimple = builderTypeSimpleRaw + pojoType.genericPartOrEmpty;
    	
        this.staticBuilderMethodNames = Sets.newHashSet(options.builderCreateMethodNames());
        if(staticBuilderMethodNames.size()==0){
        	staticBuilderMethodNames.add("with");
        }
        this.buildPojoMethodName = options.buildMethodName();
        this.markGenerated = options.markGenerated();
        this.markCtorArgsAsProperties = options.markCtorArgsAsProperties();
        this.supportSubclassing = options.supportSubclassing() || pojo.isAbstract();
        this.generateStaticBuilderMethod = options.generateStaticBuilderCreateMethod() && !supportSubclassing;
        this.generateAddRemoveMethodsForIndexedProperties = options.generateAddRemoveMethodsForIndexProperties();
        this.generateCopyBeanMethod = options.generateCreateFromBean();
        
        this.inheritSuperBeanBuilder = options.inheritSuperBeanBuilder();
        this.inheritSuperBeanProperties = options.inheritSuperBeanProperties();
        this.generateStaticBuilderMethodOnBuilder = options.generateStaticBuilderCreateMethodOnBuilder();
		String self = "this";
		String selfType = builderTypeSimple;
		String builderTypeBounds = pojoType.typeBoundsOrNull;
		
		if(supportSubclassing){
			if(pojoType.typeBoundsOrNull==null){
				selfType = builderTypeSimpleRaw + "<TSelf>";
				builderTypeBounds = "<TSelf extends " + builderTypeSimpleRaw + "<TSelf>>";		
			} else {
				String typeBounds = pojoType.typeBoundsOrNull;
				typeBounds = typeBounds.substring(1, typeBounds.length()-1);//remove trailing/leading '<>'
				selfType = builderTypeSimpleRaw + "<TSelf," + pojoType.typeBoundsNamesOrNull + ">";
				builderTypeBounds = "<TSelf extends " + builderTypeSimpleRaw + "<TSelf," + pojoType.typeBoundsNamesOrNull + ">," + typeBounds+ ">";
			}
			self = "self()";
		}
		this.builderTypeBoundsOrNull = builderTypeBounds;
		this.builderSelfAccessor = self;
		this.builderSelfType = selfType;
    }
    
    void addProperty(BuilderPropertyModel field){
        if (hasPropertyNamed(field.propertyName)) {
            throw new JMutateException("More than one property with the same name '%s' on %s", field.propertyName, pojoType.fullName);
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
}