package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codemucker.jmutate.util.NameUtil;

public class SmartConfig {

	private static final Logger LOG = LogManager.getLogger(SmartConfig.class);
	
	private static final String NODE_PROPERTY_KEY = SmartConfig.class.getName();
	
	private Map<String, Configuration> configsByAnnotationType = new HashMap<>();
	
	public SmartConfig() {
		super();
	}

	public static void set(org.eclipse.jdt.core.dom.ASTNode node,SmartConfig cfg){
		node.setProperty(NODE_PROPERTY_KEY, cfg);
	}
	
	public static SmartConfig get(org.eclipse.jdt.core.dom.ASTNode node){
		return (SmartConfig) node.getProperty(NODE_PROPERTY_KEY);
	}
	
	public List<String> getAnnotations(){
		return new ArrayList<>(configsByAnnotationType.keySet());
	}

	public void addParentConfigsFrom(SmartConfig templateConfig){
		for(String key:templateConfig.getAnnotations()){
			addParentConfigFor(key, templateConfig.getConfigFor(key));
		}
	}
	
	public void addParentConfigFor(String annotationName,Configuration child){
		Configuration existing = configsByAnnotationType.get(annotationName);
		if(existing ==null){
			configsByAnnotationType.put(annotationName, child);
		} else {
			if( existing instanceof CompositeConfiguration){//add as parent to end
				CompositeConfiguration composite = (CompositeConfiguration)existing;
				//first one wins!
				composite.addConfiguration(child);
			} else { //add as parent usign a new config
				CompositeConfiguration composite = new CompositeConfiguration();
				//first on wins!
				composite.addConfiguration(existing);
				composite.addConfiguration(child);
				//config.addProperty(annotationName, composite);
				
				configsByAnnotationType.put(annotationName, composite);
			}
			
		}
	}
	
	public void addNodeConfigFor(Class<?> annotation,Configuration child){
		addNodeConfigFor(NameUtil.compiledNameToSourceName(annotation), child);
		
	}
	public void addNodeConfigFor(String annotationName,Configuration child){
		Configuration existing = configsByAnnotationType.get(annotationName);
		if(existing ==null){
			configsByAnnotationType.put(annotationName, child);
		} else {
			if( existing instanceof CompositeConfiguration){
				CompositeConfiguration composite = (CompositeConfiguration)existing;
				int num = composite.getNumberOfConfigurations();
				
				//first one wins
				CompositeConfiguration compositeNew = new CompositeConfiguration();
				compositeNew.addConfiguration(child);
				for(int i = 0;i < num;i++){
					compositeNew.addConfiguration(composite.getConfiguration(i));
				}
				configsByAnnotationType.put(annotationName, compositeNew);
			} else {
				CompositeConfiguration composite = new CompositeConfiguration();
				//first on wins!
				composite.addConfiguration(child);
				composite.addConfiguration(existing);
				configsByAnnotationType.put(annotationName, composite);
			}
		}
	}

	public boolean hasMappingFor(Class<? extends Annotation> fromAnnotation) {
		return hasMappingFor(getMapKey(fromAnnotation));
	}
	
	public boolean hasMappingFor(String fromAnnotation) {
		return configsByAnnotationType.containsKey(fromAnnotation);
	}

	public <T> T mapFromTo(Class<? extends Annotation> fromAnnotation,Class<T> optionsClass) {
		Configuration map = getConfigFor(fromAnnotation);
		T options = OptionsMapper.INSTANCE.mapFromTo(map, fromAnnotation, optionsClass);
		return options;
	}

	public <T> void mapFromTo(Class<? extends Annotation> fromAnnotation,T options) {
		Configuration map = getConfigFor(fromAnnotation);
	
		OptionsMapper.INSTANCE.mapFromTo(map, fromAnnotation, options);
	}
	
	public Configuration getConfigFor(Class<? extends Annotation> fromAnnotation){
		return getConfigFor(getMapKey(fromAnnotation));
	}
	
	public Configuration getConfigFor(String forAnnotation){
		Configuration map = configsByAnnotationType.get(forAnnotation);
		if(map == null){
			map = new BaseConfiguration();
		}
		return map;
	}
	
	private String getMapKey(Class<? extends Annotation> fromAnnotation) {
		String annotationName = NameUtil.compiledNameToSourceName(fromAnnotation);
		return annotationName;
	}
			
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(getClass().getName()).append("@").append(hashCode()).append("[");
		for(String annoationType:configsByAnnotationType.keySet()){
			
			Configuration cfg = configsByAnnotationType.get(annoationType);
			
			sb.append("\nfor ").append(annoationType).append(":");
			
			//print config
			for(Iterator<?> keys = cfg.getKeys();keys.hasNext();){
				String key = (String) keys.next();
				Object val = cfg.getProperty(key);
				sb.append("\n\t").append(key).append("=");
				if( val == cfg){
					sb.append("<circular reference>").append(val.hashCode());
				} else if( val instanceof ASTNode ){
					ASTNode node = (ASTNode)val;
					sb.append(node.getClass().getName()).append("@").append(node.hashCode());
				} else {
					sb.append(val);
				}
			}	
		}
		
		sb.append("]");
		return sb.toString();
	}

}