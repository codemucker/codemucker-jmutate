package org.codemucker.jmutate.generate.bean;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jmutate.generate.ModelUtils;
import org.codemucker.jpattern.generate.Access;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateBean;

public class GenerateBeanOptions {   

	//these should match the annotation methods names on GenrateBean
	public static final String PROP_FIELDNAMES = "fieldNames";
	public static final String PROP_INHERIT_PROPERTIES = "inheritParentProperties";
	public static final String PROP_MARK_GENERATED = "markGenerated";
	public static final String PROP_CLASH_STRATEGY= "clashStrategy";
	public static final String PROP_ENABLED= "enabled";
	
	private final ClashStrategy clashStrategy;
	private final String fieldNames;

	private final boolean enabled;
	private final boolean markGenerated;
	private final boolean markCtorArgsAsProperties;
	private final JAccess fieldAccess;
	private final boolean generateHashCodeMethod;
	private final boolean generateEqualsMethod;
	private final boolean generateCloneMethod;
	private final String cloneMethodName;
	private final boolean makeReadonly;
	private final boolean generateStaticPropertyNameFields;
	private final boolean generateNoArgCtor;
	private final boolean generateAllArgCtor;
	private final boolean generateToString;
	private final boolean generateAddRemoveMethodsForIndexedProperties;
	private final boolean inheritSuperClassProperties;
	private final String  propertyChangeSupportFieldName = "_propertyChangeSupport";
	private final String  vetoableChangeSupportFieldName = "_vetoableSupport";
	
	private final boolean bindable;
	private final boolean vetoable;

	private final TypeInfo type;
	
    public GenerateBeanOptions(JType pojoType,Configuration cfg) {
    	this.type = TypeInfo.newFromFullNameAndTypeBounds(pojoType.getFullGenericName(), pojoType.getTypeBoundsExpressionOrNull());
    	this.enabled = cfg.getBoolean(PROP_ENABLED,true);
        this.markGenerated = cfg.getBoolean(PROP_MARK_GENERATED,true);
        this.clashStrategy = ModelUtils.getEnum(cfg,PROP_CLASH_STRATEGY, ClashStrategy.SKIP);
    	this.fieldNames = cfg.getString(PROP_FIELDNAMES, "");
    	this.inheritSuperClassProperties = cfg.getBoolean(PROP_INHERIT_PROPERTIES,false);
    	
    	//TODO:move the following into their own models
    	this.markCtorArgsAsProperties = cfg.getBoolean("markCtorArgsAsProperties",false);
    	this.fieldAccess = ModelUtils.getJAccess(cfg,"fieldAccess",Access.PRIVATE);
    	this.generateHashCodeMethod = cfg.getBoolean("generateHashCode",false);
    	this.generateEqualsMethod = cfg.getBoolean("generateEquals",false);
    	
    	this.generateAddRemoveMethodsForIndexedProperties = cfg.getBoolean("generateAddRemoveMethodsForIndexProperties",false);
    	this.generateToString = cfg.getBoolean("generateToString",false);
    	this.makeReadonly = cfg.getBoolean("readonlyProperties",false);
    	this.generateStaticPropertyNameFields = cfg.getBoolean("generateStaticPropertyNameFields",false);
    	this.generateNoArgCtor = cfg.getBoolean("generateNoArgCtor",false);
    	this.generateAllArgCtor = cfg.getBoolean("generateAllArgCtor",false);
    	this.generateCloneMethod = cfg.getBoolean("generateCloneMethod",false);
    	this.bindable = cfg.getBoolean("bindable",false);
    	this.vetoable = cfg.getBoolean("vetoable",false);
    	this.cloneMethodName = cfg.getString("cloneMethodName","clone");
    }
    
    @GenerateBean
    private static class Defaults{}

    public ClashStrategy getClashStrategy() {
		return clashStrategy;
	}

	public String getFieldNames() {
		return fieldNames;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isMarkGenerated() {
		return markGenerated;
	}

	public boolean isMarkCtorArgsAsProperties() {
		return markCtorArgsAsProperties;
	}

	public JAccess getFieldAccess() {
		return fieldAccess;
	}

	public boolean isGenerateHashCodeMethod() {
		return generateHashCodeMethod;
	}

	public boolean isGenerateEqualsMethod() {
		return generateEqualsMethod;
	}
	
	public boolean isGenerateCloneMethod() {
		return generateCloneMethod;
	}

	public boolean isMakeReadonly() {
		return makeReadonly;
	}

	public boolean isGenerateStaticPropertyNameFields() {
		return generateStaticPropertyNameFields;
	}

	public boolean isGenerateNoArgCtor() {
		return generateNoArgCtor;
	}

	public boolean isGenerateAllArgCtor() {
		return generateAllArgCtor;
	}

	public boolean isGenerateToString() {
		return generateToString;
	}

	public boolean isGenerateAddRemoveMethodsForIndexedProperties() {
		return generateAddRemoveMethodsForIndexedProperties;
	}

	public boolean isInheritSuperClassProperties() {
		return inheritSuperClassProperties;
	}

	public String getPropertyChangeSupportFieldName() {
		return propertyChangeSupportFieldName;
	}

	public String getVetoableChangeSupportFieldName() {
		return vetoableChangeSupportFieldName;
	}

	public boolean isBindable() {
		return bindable;
	}

	public boolean isVetoable() {
		return vetoable;
	}

	public TypeInfo getType() {
		return type;
	}

	public String getCloneMethodName() {
		return cloneMethodName;
	}
}