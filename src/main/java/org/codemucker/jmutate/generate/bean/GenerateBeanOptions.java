package org.codemucker.jmutate.generate.bean;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jmutate.generate.GeneratorConfig;
import org.codemucker.jmutate.generate.ModelUtils;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateBean;

public class GenerateBeanOptions {   

	private final ClashStrategy clashStrategy;
	private final String fieldNames;
	
	private final boolean markGenerated;
	private final boolean markCtorArgsAsProperties;
	private final JAccess fieldAccess;
	private final boolean generateHashCodeEquals;
	private final boolean generateCloneMethod;
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
	
    public GenerateBeanOptions(JType pojoType,GenerateBean options) {
    	this(pojoType,ModelUtils.getEmptyCfg(),options);
    }
    
    public GenerateBeanOptions(JType pojoType,GeneratorConfig cfg) {
        this(pojoType,cfg.getConfig(),getDefaultOptions());
    }
    
    private GenerateBeanOptions(JType pojoType,Configuration cfg, GenerateBean def) {
    	this.type = TypeInfo.newFromFullNameAndTypeBounds(pojoType.getFullGenericName(), pojoType.getTypeBoundsExpressionOrNull());
        this.markGenerated = cfg.getBoolean("markGenerated",def.markGenerated());
    	this.markCtorArgsAsProperties = cfg.getBoolean("markCtorArgsAsProperties",def.markCtorArgsAsProperties());
    	this.fieldAccess = ModelUtils.getJAccess(cfg,"fieldAccess",def.fieldAccess());
    	this.generateHashCodeEquals = cfg.getBoolean("generateHashCodeAndEqualsMethod",def.generateHashCodeAndEqualsMethod());
    	this.generateAddRemoveMethodsForIndexedProperties = cfg.getBoolean("generateAddRemoveMethodsForIndexProperties",def.generateAddRemoveMethodsForIndexProperties());
    	this.generateToString = cfg.getBoolean("generateToString",def.generateToString());
    	this.makeReadonly = cfg.getBoolean("readonlyProperties",def.readonlyProperties());
    	this.generateStaticPropertyNameFields = cfg.getBoolean("generateStaticPropertyNameFields",def.generateStaticPropertyNameFields());
    	this.generateNoArgCtor = cfg.getBoolean("generateNoArgCtor",def.generateNoArgCtor());
    	this.generateAllArgCtor = cfg.getBoolean("generateAllArgCtor",def.generateAllArgCtor());
    	this.generateCloneMethod = cfg.getBoolean("generateCloneMethod",def.generateCloneMethod());
    	this.inheritSuperClassProperties = cfg.getBoolean("inheritSuperClassProperties",def.inheritSuperClassProperties());
    	this.bindable = cfg.getBoolean("bindable",def.bindable());
    	this.vetoable = cfg.getBoolean("vetoable",def.vetoable());
    	
    	this.clashStrategy = ModelUtils.getEnum(cfg,"clashStrategy", def.clashStrategy());
    	this.fieldNames = cfg.getString("fieldNames", def.fieldNames());        
    }
    
    
    private static GenerateBean getDefaultOptions(){
    	return Defaults.class.getAnnotation(GenerateBean.class);
    }
    
    @GenerateBean
    private static class Defaults{}

    public ClashStrategy getClashStrategy() {
		return clashStrategy;
	}

	public String getFieldNames() {
		return fieldNames;
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

	public boolean isGenerateHashCodeEquals() {
		return generateHashCodeEquals;
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
}