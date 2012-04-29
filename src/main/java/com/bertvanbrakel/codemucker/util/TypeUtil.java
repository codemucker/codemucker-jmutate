package com.bertvanbrakel.codemucker.util;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
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
	
	public static boolean isPrimitive(String type){
		return PRIMITIVES.contains(type);
	}
	
	public static boolean typeValueRequiresSingleQuotes(String type){
		return type != null && "char".equals(type) || "java.lang.Character".equals(type);
	}
	
	public static boolean typeValueRequiresDoubleQuotes(String type){
		return type != null && "String".equals(type) || "java.lang.String".equals(type);
	}
	
	//TODO:convert the type into  fqdn
	sdsdf void toFQN(Type t, StringBuilder sb){
	
		t.accept(visitor)
		new JAstFlattener().visit(t);
		
		if (t.isPrimitiveType()) {
			sb.append(((PrimitiveType) t).getPrimitiveTypeCode().toString());
		} else if (t.isSimpleType()) {
			SimpleType st = (SimpleType) t;
			sb.append(JavaNameUtil.getQualifiedName(st.getName()));
		} else if (t.isQualifiedType()) {
			QualifiedType qt = (QualifiedType) t;
			sb.append(JavaNameUtil.getQualifiedName(qt.getName()));
		} else if (t.isArrayType()) {
			ArrayType at = (ArrayType) t;
			toFQN(at.getComponentType(), sb);
			sb.append("[]");
		} else if(t.isParameterizedType()){
			ParameterizedType pt = (ParameterizedType)t;
			
			toFQN(pt.getType(),sb);
		} else {
			throw new CodemuckerException("Currently don't know how to handle type:" + t);
		}
	}
	
}
