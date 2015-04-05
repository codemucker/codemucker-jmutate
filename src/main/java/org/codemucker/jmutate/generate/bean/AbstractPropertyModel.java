package org.codemucker.jmutate.generate.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codemucker.jmutate.ast.TypeInfo;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.BeanNameUtil;

public abstract class AbstractPropertyModel {
	public final String propertyName;
	public final String propertyNameSingular;
	public String propertyConcreteType;
	public boolean readOnly;
	public boolean hasField;
	public boolean finalField;
	
	public String propertyGetterName;
	public String propertySetterName;
    public String propertyAddName;
    public String propertyRemoveName;
    
	public final TypeInfo type;
	
    //TODO:add all the concurrent ones in. Or better yet, for util types auto detect!
    private static final IndexedTypeRegistry REGISTRY = new IndexedTypeRegistry();
    private static final PluralToSingularConverter PLURALS = new PluralToSingularConverter();
    
    /**
     * The object version of the property type if a primitive, else just the same as the property type
     */
    
    public AbstractPropertyModel(String fieldName, String propertyType) {
        
    	this.propertyName = fieldName;
    	this.propertyNameSingular = PLURALS.pluralToSingle(fieldName);
        
    	this.type = TypeInfo.newFromFullNameAndTypeBounds(propertyType, null);
    	this.propertyGetterName = BeanNameUtil.toGetterName(fieldName, NameUtil.isBoolean(propertyType));
        this.propertySetterName = BeanNameUtil.toSetterName(fieldName);
        this.propertyConcreteType = REGISTRY.getConcreteTypeFor(type.getFullNameRaw());
        this.propertyAddName = type.isIndexed()?BeanNameUtil.addPrefixName("add",propertyNameSingular):null;
        this.propertyRemoveName = type.isIndexed()?BeanNameUtil.addPrefixName("remove",propertyNameSingular):null;
    }  
    
    static class IndexedTypeRegistry {
    	private Map<String, String> defaultTypes = new HashMap<>();
    
    	private static final List<String> collectionTypes = new ArrayList<>();
        private static final List<String> mapTypes = new ArrayList<>();
        
        public IndexedTypeRegistry(){
           	addCollection("java.util.List","java.util.ArrayList");
        	addCollection("java.util.ArrayList");
        	addCollection("java.util.Collection","java.util.ArrayList");
        	addCollection("java.util.LinkedList");
        	addCollection("java.util.Vector");
        	
        	addMap("java.util.TreeSet");
        	addMap("java.util.Set","java.util.HashSet");
        	addMap("java.util.HashSet");
           	addMap("java.util.Map","java.util.HashMap");
        	addMap("java.util.HashMap");
        	addMap("java.util.Hashtable");
        	
        }
        private void addCollection(String fullName){
        	addCollection(fullName, fullName);
        }
        
        private void addCollection(String fullName, String defaultType){
        	collectionTypes.add(fullName);
        	defaultTypes.put(fullName, defaultType);
        }

        private void addMap(String fullName){
        	addMap(fullName, fullName);
        }
        
        private void addMap(String fullName, String defaultType){
        	mapTypes.add(fullName);
        	defaultTypes.put(fullName, defaultType);	
        }

        public boolean isCollection(String fullName){
        	return collectionTypes.contains(fullName);
        }
        
        public boolean isMap(String fullName){
        	return mapTypes.contains(fullName);
        }
        
        public String getConcreteTypeFor(String fullName){
        	return defaultTypes.get(fullName);
        }
    }

    static class PluralToSingularConverter {
    	private Map<String, String> pluralToSingular = new HashMap<>();
    	
    	public PluralToSingularConverter(){
    		pluralToSingular.put("people","person");
    		pluralToSingular.put("fish","fish");
    		
    	}
    	
		public String pluralToSingle(String plural) {
			if (plural == null) {
				return null;
			}
			String singular = this.pluralToSingular.get(plural);
			if (singular == null) {
				singular = plural;
				if (singular.endsWith("s")) {
					singular = singular.substring(0, singular.length() - 1);
				}
			}
			return singular;
		}
    	
    }
    

}