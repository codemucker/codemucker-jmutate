package org.codemucker.jmutate.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.Type;

public class TypeUtils {


	//none string primitives
	private static Map<String,PrimitiveType.Code> PRIMITIVES_TO_CODE = new HashMap<>();
	//all primitives including strings mapped to object type
	private static Map<String,String> TO_OBJECT_TYPE = new HashMap<>();
	//object versions of primitives
	private static Collection<String> PRIMITIVE_OBJECTS = new ArrayList<>();
	private static Collection<String> VALUE_TYPES = new ArrayList<>();

	static {
		addPrimitive(PrimitiveType.BOOLEAN,"Boolean");
		addPrimitive(PrimitiveType.BYTE,"Byte");
		addPrimitive(PrimitiveType.CHAR,"Character");
		addPrimitive(PrimitiveType.DOUBLE,"Double");
		addPrimitive(PrimitiveType.FLOAT,"Float");
		addPrimitive(PrimitiveType.INT,"Integer");
		addPrimitive(PrimitiveType.LONG,"Long");
		addPrimitive(PrimitiveType.SHORT,"Short");
		
		TO_OBJECT_TYPE.put("String", "java.lang.String");
		VALUE_TYPES.add("String");
		VALUE_TYPES.add("java.lang.String");
	}

	private static void addPrimitive(PrimitiveType.Code code, String objectTypeShort){
		PRIMITIVES_TO_CODE.put(code.toString(), code);
		TO_OBJECT_TYPE.put(code.toString(), "java.lang." + objectTypeShort);
		PRIMITIVE_OBJECTS.add("java.lang." + objectTypeShort);
		
		VALUE_TYPES.add(code.toString());
		VALUE_TYPES.add(objectTypeShort);
		VALUE_TYPES.add("java.lang." + objectTypeShort);
	}

	public static boolean isBoolean(final String shortOrFullTypeName) {
	    return "boolean".equals(shortOrFullTypeName) || "java.lang.Boolean".equals(shortOrFullTypeName) || "Boolean".equals(shortOrFullTypeName);
	}

	/**
	 * If this type is a boolean,char,int,etc.. (but not a String, Integer,...)
	 * @param shortTypeName
	 * @return
	 */
	public static boolean isPrimitive(final String shortTypeName) {
		return PRIMITIVES_TO_CODE.containsKey(shortTypeName);
	}

	/**
	 * If this type is a java.lang.Boolean,Boolean,java.lang.Character,Character,.... (but not a string)
	 * @param shortOrFullTypeName
	 * @return
	 */
	public static boolean isPrimitiveObject(final String shortOrFullTypeName) {
		return PRIMITIVE_OBJECTS.contains(shortOrFullTypeName);
	}

	public static boolean isString(final String shortOrFullTypeName) {
		return "java.lang.String".equals(shortOrFullTypeName) || "String".equals(shortOrFullTypeName);
	}

	public static boolean isValueType(final String shortOrFullTypeName) {
		return VALUE_TYPES.contains(shortOrFullTypeName);
	}

	/**
	 * Convert the primitive type to the object version. E.g. boolean --&gt;java.lang.Boolean
	 * @param shortOrFullTypeName
	 * @return
	 */
	public static String toObjectVersionType(final String shortOrFullTypeName){
		String objectType = TO_OBJECT_TYPE.get(shortOrFullTypeName);
		return objectType==null?shortOrFullTypeName:objectType;
	}

	/**
	 * Return the primitive code (excludes string)
	 * @param name
	 * @return
	 */
	public static PrimitiveType.Code getPrimitiveCodeForOrNull(String name){
		return PRIMITIVES_TO_CODE.get(name);
	}
	
	
	public static Type newType(AST ast,String fullName){
		Code code = getPrimitiveCodeForOrNull(fullName);
		if(code != null){
			return ast.newPrimitiveType(code);
		}
	
		String genericPart = null;
		int firstAngle = fullName.indexOf('<');
		if( firstAngle !=-1){//strip start/en generic brackets
			genericPart = fullName.substring(firstAngle + 1,fullName.length()-1);
			fullName = fullName.substring(0,firstAngle);
		}
		int dot = fullName.lastIndexOf('.');
		if(dot ==-1){
			return wrapIfGeneric(ast.newSimpleType(ast.newSimpleName(fullName)),genericPart);
		}
		String pkgPart = fullName.substring(0,dot);
		String namePart = fullName.substring(dot+1);
		return wrapIfGeneric(ast.newNameQualifiedType(ast.newName(pkgPart),ast.newSimpleName(namePart)),genericPart);
	}
	
	/**
	 * 
	 * @param t
	 * @param genericTypeOrNull generic part without the angle brackets
	 * @return
	 */
	private static Type wrapIfGeneric(Type t, String genericTypeOrNull) {
		if (genericTypeOrNull == null) {
			return t;
		}
		ParameterizedType ptype = t.getAST().newParameterizedType(t);

		// e.g.: Foo,Bar,Alice<F<T,L>>,Bar<X>
		int last = 0;
		int genericsCount = 0;
		for (int i = 0; i < genericTypeOrNull.length(); i++) {
			char c = genericTypeOrNull.charAt(i);
			if (c == '<') {
				genericsCount++;
			} else if (c == '>') {
				genericsCount--;
			}
			if(genericsCount == 0){
				if (c == ',') {
					ptype.typeArguments().add(newType(t.getAST(),genericTypeOrNull.substring(last, i)));
					last = i + 1;
				} else if (i == genericTypeOrNull.length() - 1){//end of line
					ptype.typeArguments().add(newType(t.getAST(),genericTypeOrNull.substring(last)));
				}
			}
		}
		return ptype;
	}

}
