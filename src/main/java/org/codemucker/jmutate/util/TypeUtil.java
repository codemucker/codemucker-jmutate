package org.codemucker.jmutate.util;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

public class TypeUtil {

	private static final Collection<String> PRIMITIVES = ImmutableList.of(
			"boolean"
			,"short"
			, "char"
			, "byte"
			, "int"
			, "float"
			, "double"
			, "String"
			, "java.lang.String"
			, "long"
	);
	
	/**
	 * Use short name if the type is a primitive, or it's package is one of the default
	 * packages imported
	 * @param type
	 * @return
	 */
	public static String toShortNameIfDefaultImport(String type){
		if(isPrimitive(type) || type.startsWith("java.lang.")){
			int idx = type.lastIndexOf(".");
			if( idx != -1){
				return type.substring(idx + 1);
			}
		}
		return type;
	}

	public static boolean isPrimitive(final String type){
		return PRIMITIVES.contains(type);
	}
	
	
}
