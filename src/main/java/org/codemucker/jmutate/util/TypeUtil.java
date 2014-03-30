package org.codemucker.jmutate.util;

import java.util.Collection;

import org.codemucker.jmutate.ast.JAstFlattener;
import org.eclipse.jdt.core.dom.Type;

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
