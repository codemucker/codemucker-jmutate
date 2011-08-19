package com.bertvanbrakel.test.bean.builder;

import java.lang.reflect.Type;

import com.bertvanbrakel.lang.MapBuilder;
import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.lang.interpolator.Interpolator;

@NotThreadSafe
public abstract class AbstractClassWriter {
	
	private StringBuilder sb = new StringBuilder();

	protected void addImport(Class<?> type){
		println( "import " + type.getName() + ";" );
	}
	
	protected void addStaticImport(Class<?> type, String method){
		println( "import static " + type.getName() + "." + method + ";" );
	}

	public void println(String s){
		println(s, map());
	}
	
	public void println(String s, MapBuilder<String, Object> map){
		CharSequence out = Interpolator.interpolate(s, map.create());
		append(out);
	}
	
	private void append(CharSequence s){
		sb.append(s);
		sb.append("\n");
	}
	
	public String toJavaClassString(){
		return sb.toString();
	}
	
	public abstract MapBuilder<String, Object> map();
	
	protected static MapBuilder<String, Object> emptyMap(){
		return new MapBuilder<String, Object>();
	}
	
	protected static String safeToClassName(Class<?> type){
		return type==null?null:type.getName();
	}

	protected static String extractPkgPart(String className){
		int dot = className.lastIndexOf('.');
		if( dot != -1 ){
			return className.substring(0, dot);
		}
		return "";
	}

	protected static String extractShortClassNamePart(String className){
		int dot = className.lastIndexOf('.');
		if( dot != -1 ){
			return className.substring(dot+1);
		}
		return className;
	}
	
	protected static String safeToClassName(Type type){
		return type==null?null:type.getClass().getName();
	}
	
}