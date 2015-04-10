package org.codemucker.jmutate.generate.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ast.JType;

public class BeanModel {   

	private static final Logger LOG = LogManager.getLogger(BeanModel.class);
	
	public static final Comparator<BeanPropertyModel> PROP_COMPARATOR = new Comparator<BeanPropertyModel>() {

		@Override
		public int compare(BeanPropertyModel left,BeanPropertyModel right) {
			return left.getPropertyName().compareTo(right.getPropertyName());
		}
	};
	
	public final GenerateBeanOptions options;
    private final Map<String, BeanPropertyModel> properties = new LinkedHashMap<>();
    
    public BeanModel(JType pojoType,Configuration cfg) {
    	this.options = new GenerateBeanOptions(pojoType, cfg);
    }

    public boolean hasDirectFinalProperties(){
    	for(BeanPropertyModel p:properties.values()){
    		if(!p.isFromSuperClass() && p.hasField() && p.isFinalField()){
    			return true;
    		}
    	}
    	return false;
    }

	void addProperty(BeanPropertyModel property){
        if (hasNamedField(property.getPropertyName())) {
            throw new JMutateException("More than one property with the same name '%s' on %s", property.getPropertyName(), options.getType().getFullName());
        }
        if(LOG.isDebugEnabled()){
			LOG.debug("adding property '" + property.getPropertyName() + "', " + property);
		}
        properties.put(property.getPropertyName(), property);
    }
    
    boolean hasNamedField(String name){
        return properties.containsKey(name);
    }
    
    BeanPropertyModel getNamedField(String name){
        return properties.get(name);
    }
    
    Collection<BeanPropertyModel> getProperties(){
    	List<BeanPropertyModel> ordered = new ArrayList<>(properties.values());
    	Collections.sort(ordered,PROP_COMPARATOR);
        return ordered;
    }

	Map<String, BeanPropertyModel> getPropertiesMap() {
		return properties;
	}
	
	
}