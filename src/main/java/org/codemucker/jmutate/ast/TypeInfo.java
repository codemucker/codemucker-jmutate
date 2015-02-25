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

	public final String pkg;
	
	public final String fullName;
	public final String fullNameRaw;

	/**
	 * if this type is a primitive, this is the equivalent version. For all
	 * other types this is the same as the full name
	 */
	public final String objectTypeFullName;
	public final String objectTypeFullNameRaw;
	public final String genericPartOrNull;
	public final String genericPartOrEmpty;

	public final String typeBoundsOrNull;
	public final String typeBoundsOrEmpty;
	public final String typeBoundsNamesOrNull;
	
	public final String simpleName;
	public final String simpleNameRaw;
	public final String indexedKeyTypeNameOrNull;
	public final String indexedKeyTypeNameRaw;
	public final String indexedValueTypeNameOrNull;
	public final String indexedValueTypeNameRaw;
	public final boolean isIndexed;
	public final boolean isCollection;
	public final boolean isList;
	public final boolean isMap;
	public final boolean isArray;
	public final boolean isKeyed;
	public final boolean isPrimitive;
	public final boolean isString;
	public final boolean isGeneric;

	public static TypeInfo newFromFullNameAndTypeBounds(String fullType, String typeBounds){
		return new TypeInfo(fullType, typeBounds);
	}
	
	public TypeInfo(String fullType, String typeBounds) {
		this.typeBoundsOrNull = Strings.emptyToNull(typeBounds);
		this.typeBoundsOrEmpty = Strings.nullToEmpty(typeBoundsOrNull);
		this.typeBoundsNamesOrNull = typeBoundsOrNull == null?null:Joiner.on(",").join(extractTypeNames(typeBoundsOrNull));
		
		this.isGeneric = fullType.contains("<");
		
		this.fullName = NameUtil.compiledNameToSourceName(fullType);
		this.fullNameRaw = NameUtil.removeGenericOrArrayPart(fullName);
		this.genericPartOrNull = NameUtil.extractGenericPartOrNull(fullType);
		this.genericPartOrEmpty = Strings.nullToEmpty(genericPartOrNull);
		
		this.pkg = ClassNameUtil.extractPkgPartOrNull(fullType);
		
		this.simpleNameRaw = ClassNameUtil.extractSimpleClassNamePart(fullNameRaw);
		this.simpleName = simpleNameRaw + (genericPartOrNull == null ? "" : genericPartOrNull);

		this.objectTypeFullName = NameUtil.primitiveToObjectType(fullName);
		this.objectTypeFullNameRaw = NameUtil.removeGenericOrArrayPart(objectTypeFullName);

		this.isPrimitive = NameUtil.isPrimitive(fullName);
		this.isString = fullName.equals("String") || fullName.equals("java.lang.String");
		this.isMap = REGISTRY.isMap(fullNameRaw);
		this.isList = REGISTRY.isList(fullNameRaw);
		this.isCollection = REGISTRY.isCollection(fullNameRaw);
		this.isIndexed = isMap || isList;
		// this.propertyConcreteType =
		// REGISTRY.getConcreteTypeFor(propertyTypeRaw);
		this.isArray = fullName.endsWith("]");
		this.isKeyed = isMap;
		this.indexedValueTypeNameOrNull = isCollection ? BeanNameUtil.extractIndexedValueType(fullName) : null;
		this.indexedValueTypeNameRaw = NameUtil.removeGenericOrArrayPart(indexedValueTypeNameOrNull);
		this.indexedKeyTypeNameOrNull = isMap ? BeanNameUtil.extractIndexedKeyType(fullName) : null;
		this.indexedKeyTypeNameRaw = NameUtil.removeGenericOrArrayPart(indexedKeyTypeNameOrNull);
	}

	public List<String> getTypeParamNames(){
		return extractTypeNames(typeBoundsOrNull);
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
		return fullName.equals(type);
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
		return genericPartOrNull;
	}

	public String getTypeBounds() {
		return typeBoundsOrNull;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getSimpleNameRaw() {
		return simpleNameRaw;
	}

	public String getIndexedKeyTypeName() {
		return indexedKeyTypeNameOrNull;
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public String getIndexedKeyTypeNameRaw() {
		return indexedKeyTypeNameRaw;
	}

	public String getIndexedValueTypeName() {
		return indexedValueTypeNameOrNull;
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