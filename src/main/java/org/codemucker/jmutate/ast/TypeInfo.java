package org.codemucker.jmutate.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.lang.BeanNameUtil;
import org.codemucker.lang.ClassNameUtil;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class TypeInfo {

	// TODO:add all the concurrent ones in. Or better yet, for util types auto
	// detect!
	private static final IndexedTypeRegistry REGISTRY = new IndexedTypeRegistry();
	private static final List<String> EMPTY_STRING_LIST = Collections.emptyList();

	private final String pkg;
	
	private final String fullName;
	private final String fullNameRaw;

	/**
	 * if this type is a primitive, this is the equivalent version. For all
	 * other types this is the same as the full name
	 */
	private final String objectTypeFullName;
	private final String objectTypeFullNameRaw;
	private final String genericPartOrNull;
	private final String genericPartOrEmpty;

	private final String typeBoundsOrNull;
	private final String typeBoundsOrEmpty;
	private final String typeBoundsNamesOrNull;
	
	private final String simpleName;
	private final String simpleNameRaw;
	private final String indexedKeyTypeNameOrNull;
	private final String indexedKeyTypeNameRaw;
	private final String indexedValueTypeNameOrNull;
	private final String indexedValueTypeNameRaw;
	private final boolean isIndexed;
	private final boolean isCollection;
	private final boolean isList;
	private final boolean isMap;
	private final boolean isArray;
	private final boolean isKeyed;
	private final boolean isPrimitive;
	private final boolean isString;
	private final boolean isGeneric;

	public static TypeInfo newFromFullNameAndTypeBounds(String fullType, String typeBounds){
		return new TypeInfo(fullType, typeBounds);
	}
	
	public TypeInfo(String fullType, String typeBounds) {
		this.typeBoundsOrNull = Strings.emptyToNull(typeBounds);
		this.typeBoundsOrEmpty = Strings.nullToEmpty(getTypeBoundsOrNull());
		this.typeBoundsNamesOrNull = getTypeBoundsOrNull() == null?null:Joiner.on(",").join(extractTypeNames(getTypeBoundsOrNull()));
		
		this.isGeneric = fullType.contains("<");
		
		this.fullName = NameUtil.compiledNameToSourceName(fullType);
		this.fullNameRaw = NameUtil.removeGenericOrArrayPart(getFullName());
		this.genericPartOrNull = NameUtil.extractGenericPartOrNull(fullType);
		this.genericPartOrEmpty = Strings.nullToEmpty(getGenericPartOrNull());
		
		this.pkg = ClassNameUtil.extractPkgPartOrNull(fullType);
		
		this.simpleNameRaw = ClassNameUtil.extractSimpleClassNamePart(getFullNameRaw());
		this.simpleName = getSimpleNameRaw() + (getGenericPartOrNull() == null ? "" : getGenericPartOrNull());

		this.objectTypeFullName = NameUtil.primitiveToObjectType(getFullName());
		this.objectTypeFullNameRaw = NameUtil.removeGenericOrArrayPart(getObjectTypeFullName());

		this.isPrimitive = NameUtil.isPrimitive(getFullName());
		this.isString = getFullName().equals("String") || getFullName().equals("java.lang.String");
		this.isMap = REGISTRY.isMap(getFullNameRaw());
		this.isList = REGISTRY.isList(getFullNameRaw());
		this.isCollection = REGISTRY.isCollection(getFullNameRaw());
		this.isIndexed = isMap() || isList();
		// this.propertyConcreteType =
		// REGISTRY.getConcreteTypeFor(propertyTypeRaw);
		this.isArray = getFullName().endsWith("]");
		this.isKeyed = isMap();
		this.indexedValueTypeNameOrNull = isCollection() ? BeanNameUtil.extractIndexedValueType(getFullName()) : null;
		this.indexedValueTypeNameRaw = NameUtil.removeGenericOrArrayPart(getIndexedValueTypeNameOrNull());
		this.indexedKeyTypeNameOrNull = isMap() ? BeanNameUtil.extractIndexedKeyType(getFullName()) : null;
		this.indexedKeyTypeNameRaw = NameUtil.removeGenericOrArrayPart(getIndexedKeyTypeNameOrNull());
	}

	public List<String> getTypeParamNames(){
		return extractTypeNames(getTypeBoundsOrNull());
	}
	
	/**
	 * Given <name1,name2,name3 extends Foo<Bar>,name4> return name1,name2,name3,name4
	 * @param typeBounds
	 * @return
	 */
    protected static List<String> extractTypeNames(String typeBounds){
    	if( Strings.isNullOrEmpty(typeBounds)){
    		return EMPTY_STRING_LIST;
    	}
    	
    	List<String> names = new ArrayList<>();
    	boolean inName = false;
    	int depth = 0;
    	
    	StringBuilder sb = new StringBuilder();
    	
    	for(int i = 0; i < typeBounds.length();i++){
    		char c = typeBounds.charAt(i);
    		if( c == '<'){
    			depth ++;
    		} else if (c == '>'){
    			depth --;
    		}
    		
    		if(depth == 1){
    			if( c == '<'){
    				inName = true;
    			} else if( c ==  ','){
    				if(sb.length() > 0){
    	    			names.add(sb.toString());
    	    			sb.setLength(0);
    				}
    				inName = true;
    			} else if(Character.isWhitespace(c)){
    				if(sb.length() > 0){ //was in a name, whitespace marks the end of it. If no name yet, gobble whitespace
    	    			names.add(sb.toString());
    	    			sb.setLength(0);
    	    			inName = false;
    	    		}
	    		} else if(Character.isJavaIdentifierPart(c)){
	    			if(inName){
	    				sb.append(c);
	    			}
	    		} else {
	    			if(sb.length() > 0){
    	    			names.add(sb.toString());
    	    			sb.setLength(0);
	    			}
	    			inName = false;
	    		}
    		}
    		
    	}
    	if( sb.length() > 0){
			names.add(sb.toString());
		}
    	return names;
    }
    
    private static int readTill(String s, int offset, char terminal){
    	for(int i = offset;i < s.length();i++){
    		char c = s.charAt(i);
    		if(c == terminal){
    			return i;
    		}
    	}
    	return -1;
    }
    
	public static IndexedTypeRegistry getRegistry() {
		return REGISTRY;
	}
	
	public boolean is(String type){
		return getFullName().equals(type);
	}

	public String getFullName() {
		return fullName;
	}

	public String getFullNameRaw() {
		return fullNameRaw;
	}

	public String getObjectTypeFullName() {
		return objectTypeFullName;
	}

	public String getObjectTypeFullNameRaw() {
		return objectTypeFullNameRaw;
	}

	public String getGenericPart() {
		return getGenericPartOrNull();
	}

	public String getTypeBounds() {
		return getTypeBoundsOrNull();
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getSimpleNameRaw() {
		return simpleNameRaw;
	}

	public String getIndexedKeyTypeName() {
		return getIndexedKeyTypeNameOrNull();
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public String getIndexedKeyTypeNameRaw() {
		return indexedKeyTypeNameRaw;
	}

	public String getIndexedValueTypeName() {
		return getIndexedValueTypeNameOrNull();
	}

	public String getIndexedValueTypeNameRaw() {
		return indexedValueTypeNameRaw;
	}

	public boolean isCollection() {
		return isCollection;
	}

	public boolean isMap() {
		return isMap;
	}

	public boolean isArray() {
		return isArray;
	}

	public boolean isKeyed() {
		return isKeyed;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public boolean isString() {
		return isString;
	}

	public String getPkg() {
		return pkg;
	}

	public String getGenericPartOrNull() {
		return genericPartOrNull;
	}

	public String getGenericPartOrEmpty() {
		return genericPartOrEmpty;
	}

	public String getTypeBoundsOrNull() {
		return typeBoundsOrNull;
	}

	public String getTypeBoundsOrEmpty() {
		return typeBoundsOrEmpty;
	}

	public String getTypeBoundsNamesOrNull() {
		return typeBoundsNamesOrNull;
	}

	public String getIndexedKeyTypeNameOrNull() {
		return indexedKeyTypeNameOrNull;
	}

	public String getIndexedValueTypeNameOrNull() {
		return indexedValueTypeNameOrNull;
	}

	public boolean isList() {
		return isList;
	}

	public boolean isGeneric() {
		return isGeneric;
	}

	static class IndexedTypeRegistry {
		private Map<String, String> defaultTypes = new HashMap<>();

		private static final List<String> collectionTypes = new ArrayList<>();
		private static final List<String> listTypes = new ArrayList<>();
		private static final List<String> mapTypes = new ArrayList<>();

		public IndexedTypeRegistry() {
			addList("java.util.List", "java.util.ArrayList");
			addList("java.util.ArrayList");
			addList("java.util.LinkedList");

			addCollection("java.util.Collection", "java.util.ArrayList");
			addCollection("java.util.Vector");

			addMap("java.util.TreeSet");
			addMap("java.util.Set", "java.util.HashSet");
			addMap("java.util.HashSet");
			addMap("java.util.Map", "java.util.HashMap");
			addMap("java.util.HashMap");
			addMap("java.util.Hashtable");

		}

		private void addList(String fullName) {
			addList(fullName, fullName);
		}

		private void addCollection(String fullName) {
			addCollection(fullName, fullName);
		}
		
		private void addList(String fullName, String defaultType) {
			listTypes.add(fullName);
			addCollection(fullName, defaultType);
		}

		private void addCollection(String fullName, String defaultType) {
			collectionTypes.add(fullName);
			defaultTypes.put(fullName, defaultType);
		}
		
		private void addMap(String fullName) {
			addMap(fullName, fullName);
		}

		private void addMap(String fullName, String defaultType) {
			mapTypes.add(fullName);
			defaultTypes.put(fullName, defaultType);
		}

		public boolean isCollection(String fullName) {
			return collectionTypes.contains(fullName);
		}

		public boolean isList(String fullName) {
			return listTypes.contains(fullName);
		}

		public boolean isMap(String fullName) {
			return mapTypes.contains(fullName);
		}

		public String getConcreteTypeFor(String fullName) {
			return defaultTypes.get(fullName);
		}
	}
}