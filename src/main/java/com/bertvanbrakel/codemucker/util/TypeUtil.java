package com.bertvanbrakel.codemucker.util;

import java.util.Collection;

import org.eclipse.jdt.core.dom.Type;

import com.bertvanbrakel.codemucker.ast.JAstFlattener;
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

	public static boolean isPrimitive(final String type){
		return PRIMITIVES.contains(type);
	}

	public static boolean typeValueRequiresSingleQuotes(final String type){
		return type != null && "char".equals(type) || "java.lang.Character".equals(type);
	}

	public static boolean typeValueRequiresDoubleQuotes(final String type){
		return type != null && "String".equals(type) || "java.lang.String".equals(type);
	}

	public static String toTypeSignature(final Type t){
        //TODO:return the actual FQDN, instead of the bad implementation of TypeUtils
        final StringBuilder sb = new StringBuilder();
        toTypeSignature(t, sb);
        return sb.toString();
    }

	public static void toTypeSignature(final Type t, final StringBuilder sb){
		//TODO:may need to look up the imports to convert to FQDN?
		final JAstFlattener flattener = new JAstFlattener(sb);
		t.accept(flattener);
	}
}
