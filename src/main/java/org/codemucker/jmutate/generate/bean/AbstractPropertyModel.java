package org.codemucker.jmutate.generate.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.BeanNameUtil;

public abstract class AbstractPropertyModel {
	public final String propertyName;
	public final String propertyGetterName;
	public final String propertySetterName;
    
	public final String propertyType;
	public final String propertyTypeRaw;
	public final String propertyTypeGenericPart;
	
    public final boolean isPrimitive;
    public final boolean isString;
    public final boolean isCollection;
    public final boolean isMap;
    public final boolean isArray;
    public final boolean isIndexed;
    public  final String propertyTypeAsObject;
	public final String propertyConcreteType;
    public final String propertyAddName;
    public final String propertyRemoveName;
    public final String propertyIndexedValueType;
    public final String propertyIndexedKeyType;
	public final String propertyNameSingular;
	
    //TODO:add all the concurrent ones in. Or better yet, for util types auto detect!
    private static final IndexedTypeRegistry REGISTRY = new IndexedTypeRegistry();
    private static final PluralToSingularConverter PLURALS = new PluralToSingularConverter();
    
    /**
     * The object version of the property type if a primitive, else just the same as the property type
     */
    
    public AbstractPropertyModel(String fieldName, String propertyType) {
        
    	this.propertyName = fieldName;
    	this.propertyNameSingular = PLURALS.pluralToSingle(fieldName);
        
        this.propertyType = NameUtil.compiledNameToSourceName(propertyType);
        this.propertyTypeRaw = NameUtil.removeGenericOrArrayPart(propertyType);
        this.propertyTypeGenericPart = NameUtil.extractGenericPartOrNull(propertyType);
        
        this.propertyTypeAsObject = NameUtil.compiledNameToSourceName(NameUtil.primitiveToObjectType(propertyType));
        this.isPrimitive = NameUtil.isPrimitive(propertyType);
        this.isString = propertyType.equals("String") || propertyType.equals("java.lang.String");  
        this.propertyGetterName = BeanNameUtil.toGetterName(fieldName, NameUtil.isBoolean(propertyType));
        this.propertySetterName = BeanNameUtil.toSetterName(fieldName);
        
        this.isMap = REGISTRY.isMap(propertyTypeRaw);
        this.isCollection = REGISTRY.isCollection(propertyTypeRaw);
        
        this.isIndexed = isMap || isCollection;
        this.propertyConcreteType = REGISTRY.getConcreteTypeFor(propertyTypeRaw);
        this.isArray = propertyType.endsWith("]");
        
        this.propertyAddName = isIndexed?BeanNameUtil.addPrefixName("add",propertyNameSingular):null;
        this.propertyRemoveName = isIndexed?BeanNameUtil.addPrefixName("remove",propertyNameSingular):null;
        
        this.propertyIndexedValueType = isCollection?BeanNameUtil.extractIndexedValueType(propertyType):null;
        this.propertyIndexedKeyType = isMap?BeanNameUtil.extractIndexedKeyType(propertyType):null;
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