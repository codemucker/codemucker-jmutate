package org.codemucker.jmutate.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.Type;

public class TypeUtils {
	public static Type newType(AST ast,String fullName){
		Code code = NameUtil.getPrimitiveCodeForOrNull(fullName);
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
