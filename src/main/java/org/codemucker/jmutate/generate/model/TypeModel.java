package org.codemucker.jmutate.generate.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jmutate.util.TypeUtils;
import org.codemucker.lang.BeanNameUtil;
import org.codemucker.lang.ClassNameUtil;
import org.codemucker.lang.annotation.Immutable;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 * Single place to merge all type meta information from both source and compiled code
 */
public class TypeModel extends ModelObject {

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
	/**
	 * The object type without the generic part
	 */
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
	private final boolean isSet;
	private final boolean isArray;
	private final boolean isKeyed;
	private final boolean isString;
	private final boolean isGeneric;
	private final boolean isPrimitive;
	private final boolean isPrimitiveObject;
	private final boolean isImmutable;
	private final boolean isInterface;

	public TypeModel(Class<?> type){
		this(NameUtil.compiledNameToSourceName(type), null, type.isInterface(), type.isEnum()  || type.isAnnotationPresent(Immutable.class));
	}
	
	public TypeModel(JType type){
		this(type.getFullGenericName(), type.getTypeBoundsExpressionOrNull(),type.isInterface(), type.isEnum() || type.getAnnotations().contains(Immutable.class));
	}
	
	public TypeModel(String fullType) {
		this(fullType,null, false,false /*??? load type?*/);
	}
	
	public TypeModel(String fullType, String typeBounds) {
		this(fullType,typeBounds, false, false);
	}
	
	private TypeModel(String fullType, String typeBounds, boolean isInterface,boolean immutable) {
		this.typeBoundsOrNull = Strings.emptyToNull(typeBounds);
		this.typeBoundsOrEmpty = Strings.nullToEmpty(typeBoundsOrNull);
		this.typeBoundsNamesOrNull = getTypeBoundsOrNull() == null?null:Joiner.on(",").join(extractTypeNames(typeBoundsOrNull));
		this.pkg = ClassNameUtil.extractPkgPartOrNull(fullType);
		this.fullName = NameUtil.compiledNameToSourceName(fullType);
		this.fullNameRaw = NameUtil.removeGenericOrArrayPart(fullName);
		this.genericPartOrNull = NameUtil.extractGenericPartOrNull(fullType);
		this.genericPartOrEmpty = Strings.nullToEmpty(genericPartOrNull);
		
		//TODO:integer flag for all?
		this.isPrimitive = TypeUtils.isPrimitive(fullName);
		this.isPrimitiveObject = TypeUtils.isPrimitiveObject(fullName);
		this.isString = TypeUtils.isString(fullName);
		this.isImmutable = immutable || TypeUtils.isValueType(fullName);
		this.isInterface = isInterface;
		
		this.isGeneric = fullType.contains("<");
		this.isSet = REGISTRY.isSet(fullNameRaw);
		this.isList = REGISTRY.isList(fullNameRaw);
		this.isMap = REGISTRY.isMap(fullNameRaw);
		this.isCollection = isSet || isList ||  REGISTRY.isCollection(fullNameRaw);
		this.isKeyed = isMap;
		this.isIndexed = isMap || isList;
		this.isArray = getFullName().endsWith("]");
		
		this.indexedKeyTypeNameOrNull = wildcardToObject(isMap ? BeanNameUtil.extractIndexedKeyType(fullName) : null);
		this.indexedKeyTypeNameRaw = wildcardToObject(isMap ? NameUtil.removeGenericOrArrayPart(indexedKeyTypeNameOrNull) : null);
		this.indexedValueTypeNameOrNull = wildcardToObject(isCollection || isMap ? BeanNameUtil.extractIndexedValueType(fullName) : null);
		this.indexedValueTypeNameRaw = wildcardToObject(isCollection || isMap ? NameUtil.removeGenericOrArrayPart(indexedValueTypeNameOrNull) : null);
		
		this.simpleNameRaw = ClassNameUtil.extractSimpleClassNamePart(fullNameRaw);
		this.simpleName = getSimpleNameRaw() + (genericPartOrNull == null ? "" : genericPartOrNull);
		this.objectTypeFullName = isPrimitive ? TypeUtils.toObjectVersionType(fullName):fullName;
		this.objectTypeFullNameRaw = isPrimitive ? NameUtil.removeGenericOrArrayPart(objectTypeFullName):fullNameRaw;
	}

	private static String wildcardToObject(String s){
		return s == null?null:("?".equals(s)?"java.lang.Object":s);
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

	public String getSimpleName() {
		return simpleName;
	}

	public String getSimpleNameRaw() {
		return simpleNameRaw;
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public String getIndexedKeyTypeNameRaw() {
		return indexedKeyTypeNameRaw;
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

	public boolean isSet() {
		return isSet;
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

	public boolean isNullable() {
		return !isPrimitive;
	}

	public boolean isPrimitiveObject() {
		return isPrimitiveObject;
	}

	public boolean isImmutable() {
		return isImmutable;
	}

	public boolean isInterface() {
		return isInterface;
	}

	static class IndexedTypeRegistry {
		private Map<String, String> defaultTypes = new HashMap<>();

		private static final Set<String> collectionTypes = new HashSet<>();
		private static final Set<String> listTypes = new HashSet<>();
		private static final Set<String> setTypes = new HashSet<>();
		private static final Set<String> mapTypes = new HashSet<>();

		public IndexedTypeRegistry() {
			addList("java.util.List", "java.util.ArrayList");
			addList("java.util.ArrayList");
			addList("java.util.LinkedList");

			addCollection("java.util.Collection", "java.util.ArrayList");
			addCollection("java.util.Vector");
			addCollection("java.util.ArrayList");

			addSet("java.util.Set", "java.util.HashSet");
			addSet("java.util.TreeSet");
			addSet("java.util.HashSet");
			
			addMap("java.util.Map", "java.util.HashMap");
			addMap("java.util.HashMap");
			addMap("java.util.TreeMap");
			addMap("java.util.Hashtable");
		}

		private void addSet(String fullName) {
			addSet(fullName, fullName);
		}

		private void addSet(String fullName, String defaultType) {
			setTypes.add(fullName);
			setTypes.add(defaultType);
			addCollection(fullName, defaultType);
		}
		
		private void addList(String fullName) {
			addList(fullName, fullName);
		}

		private void addCollection(String fullName) {
			addCollection(fullName, fullName);
		}
		
		private void addList(String fullName, String defaultType) {
			listTypes.add(fullName);
			listTypes.add(defaultType);
			addCollection(fullName, defaultType);
		}

		private void addCollection(String fullName, String defaultType) {
			collectionTypes.add(fullName);
			collectionTypes.add(defaultType);			
			defaultTypes.put(fullName, defaultType);
		}
		
		private void addMap(String fullName) {
			addMap(fullName, fullName);
		}

		private void addMap(String fullName, String defaultType) {
			mapTypes.add(fullName);
			mapTypes.add(defaultType);
			defaultTypes.put(fullName, defaultType);
		}

		public boolean isCollection(String fullName) {
			return collectionTypes.contains(fullName);
		}

		public boolean isList(String fullName) {
			return listTypes.contains(fullName);
		}

		public boolean isSet(String fullName) {
			return setTypes.contains(fullName);
		}

		public boolean isMap(String fullName) {
			return mapTypes.contains(fullName);
		}
		
		public String getConcreteTypeFor(String fullName) {
			return defaultTypes.get(fullName);
		}
	}
}