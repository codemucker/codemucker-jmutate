package com.bertvanbrakel.codemucker.util;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JAstFlattener;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
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
	
	public static boolean isPrimitive(String type){
		return PRIMITIVES.contains(type);
	}
	
	public static boolean typeValueRequiresSingleQuotes(String type){
		return type != null && "char".equals(type) || "java.lang.Character".equals(type);
	}
	
	public static boolean typeValueRequiresDoubleQuotes(String type){
		return type != null && "String".equals(type) || "java.lang.String".equals(type);
	}
	
	public static void toName(Type t, StringBuilder sb){
		//TODO:may need to look up the imports to convert to FQDN?
		JAstFlattener flattener = new JAstFlattener(sb);
		t.accept(flattener);
	}
}
