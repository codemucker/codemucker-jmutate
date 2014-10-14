package org.codemucker.jmutate.util;

import org.codemucker.jtest.ClassNameUtil;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

public class BeanNameUtils {

    public static String toSetterName(String name) {
        return "set" + ClassNameUtil.upperFirstChar(name);
    }

    public static String toGetterName(String name, java.lang.reflect.Type type) {
        boolean isBool = "boolean".equals(type.toString()) || "java.lang.Boolean".equals(type.toString());
        return toGetterName(name, isBool);
    }

    public static String toGetterName(String name, Class<?> type) {
        return toGetterName(name, boolean.class.equals(type) || Boolean.class.equals(type));
    }

    public static String toGetterName(String name, Type typeNode) {
        boolean isBool = typeNode.isPrimitiveType() && ((PrimitiveType) typeNode).getPrimitiveTypeCode() == PrimitiveType.BOOLEAN;
        isBool = isBool || JavaNameUtil.resolveQualifiedName(typeNode) == "java.lang.Boolean";
        return toGetterName(name, isBool);
    }

    public static String toGetterName(String name, boolean isBoolean) {
        return (isBoolean ? "is" : "get") + ClassNameUtil.upperFirstChar(name);
    }

}
