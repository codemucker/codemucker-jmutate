package com.bertvanbrakel.test.util;

import java.lang.reflect.Type;

import com.bertvanbrakel.test.bean.ClassUtils;

public class ClassNameUtil {

	public static String pathToClassName(String relFilePath) {
    	String classPath = stripExtension(relFilePath);
    	String className = ClassUtils.convertFilePathToClassPath(classPath);
    	return className;
    }

	public static String stripExtension(String path) {
    	int dot = path.lastIndexOf('.');
    	if (dot != -1) {
    		return path.substring(0, dot);
    	}
    	return path;
    }

	public static String lowerFirstChar(String name) {
    	if (name.length() > 1) {
    		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    	} else {
    		return Character.toLowerCase(name.charAt(0)) + "";
    	}
    }

	public static String upperFirstChar(String name) {
    	if (name.length() > 1) {
    		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    	} else {
    		return Character.toUpperCase(name.charAt(0)) + "";
    	}
    }

	public static String safeToClassName(Class<?> type){
    	return type==null?null:type.getName();
    }

	public static String insertBeforeClassName(String fqClassName, String shortClassNamePrefix) {
    	return extractPkgPart(fqClassName) + "." + shortClassNamePrefix + extractShortClassNamePart(fqClassName);
    }

	public static String extractPkgPart(String className){
    	int dot = className.lastIndexOf('.');
    	if( dot != -1 ){
    		return className.substring(0, dot);
    	}
    	return "";
    }

	public static String extractShortClassNamePart(String className){
    	int dot = className.lastIndexOf('.');
    	if( dot != -1 ){
    		return className.substring(dot+1);
    	}
    	return className;
    }

	public static String safeToClassName(Type type){
    	return type==null?null:type.getClass().getName();
    }

}
