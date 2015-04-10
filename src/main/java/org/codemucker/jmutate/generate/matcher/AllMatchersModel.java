package org.codemucker.jmutate.generate.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.ModelUtils;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateMatchers;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * Global details about what is being generated
 */
public class AllMatchersModel {
	private final Configuration cfg;
    
	private final String pojoDependencies;
	private final String  pojoNames;
	private final String pojoTypes;
	private final ClashStrategy clashStrategy;
	
	private final String defaultPackage;
    private final boolean keepInSync;
	private final boolean markGenerated;
	private final boolean scanSources;
	private final boolean scanDependencies;
	private final Set<String> staticBuilderMethodNames;
	
    private final List<MatcherModel> matchers = new ArrayList<>();
   
    public AllMatchersModel(JType declaredInNode, Configuration cfg) {
        this(declaredInNode,cfg, getDefaultOptions());
    }
   
    private AllMatchersModel(JType declaredInNode, Configuration cfg, GenerateMatchers def) {
        this.defaultPackage = Strings.emptyToNull(cfg.getString("generateToPackage",def.generateToPackage()));
        this.cfg = cfg;
        this.keepInSync = cfg.getBoolean("keepInSync",def.keepInSync());
        this.markGenerated = cfg.getBoolean("markGenerated",def.markGenerated()); 
        this.staticBuilderMethodNames = Sets.newHashSet(ModelUtils.getList(cfg,"builderMethodNames",def.builderMethodNames()));
        
        this.pojoDependencies = cfg.getString("pojoDependencies",def.pojoDependencies());
        this.pojoNames = cfg.getString("pojoNames",def.pojoNames());
        this.pojoTypes = cfg.getString("pojoTypes",def.pojoTypes());
        
        this.scanSources = cfg.getBoolean("scanSources",def.scanSources());
        this.scanDependencies = cfg.getBoolean("scanDependencies",def.scanDependencies());
        
        this.clashStrategy = ModelUtils.getEnum(cfg, "clashStrategy", def.clashStrategy());
    }
    
    private static GenerateMatchers getDefaultOptions(){
    	return Defaults.class.getAnnotation(GenerateMatchers.class);
    }
    
    @GenerateMatchers
    private static class Defaults {}

    protected Configuration getConfig(){
    	return cfg;
    }
    
    void addMatcher(MatcherModel model){
        this.matchers.add(model);
    }

	public String getDefaultPackage() {
		return defaultPackage;
	}

	List<MatcherModel> getMatchers() {
		return matchers;
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

	public String getPojoDependencies() {
		return pojoDependencies;
	}

	public String getPojoNames() {
		return pojoNames;
	}

	public String getPojoTypes() {
		return pojoTypes;
	}

	public ClashStrategy getClashStrategy() {
		return clashStrategy;
	}

	public boolean isScanSources() {
		return scanSources;
	}

	public boolean isScanDependencies() {
		return scanDependencies;
	}
	
}