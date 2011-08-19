package com.bertvanbrakel.test.bean.builder;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.bertvanbrakel.lang.MapBuilder;
import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.ClassUtils;
import com.bertvanbrakel.test.bean.PropertyDefinition;

@NotThreadSafe
public class BeanBuilderWriter extends AbstractClassWriter {
	
	private final BuilderOptions options;

	private final String pkgName;
	private final String beanTypeName;
	private final String shortClassName;
	private final String fullClassName;
	
	private String indent = "   ";
	
	private static final Map<String, String> primitiveToName = new HashMap<String, String>();
	
	static {
		primitiveToName.put("long", "Long");
		primitiveToName.put("int", "Int");
		primitiveToName.put("short", "Short");
		primitiveToName.put("double", "Double");
		primitiveToName.put("float", "Float");
		primitiveToName.put("byte", "Byte");
		primitiveToName.put("boolean", "Boolean");
		primitiveToName.put("char", "Char");
		primitiveToName.put("if", "If");
		primitiveToName.put("else", "Else");
	}
	
	
	BeanBuilderWriter(BuilderOptions options, String beanClassName){
		this.options = options;
		this.beanTypeName = beanClassName;
		this.fullClassName = beanClassName + "Builder";
		this.shortClassName = extractShortClassNamePart(this.fullClassName);
		this.pkgName = extractPkgPart(this.fullClassName);
	}
	
	public String getSourceFilePath(){
		return fullClassName.replace('.', '/') + ".java";
	}
	public void generate(BeanDefinition def){
		println("package ${this.pkgName};");
		generateImports();
		generateClassOpen();
		generateFields(def);
		generateSetters(def);
		generateBeanCreateMethod(def);
		generateClassClose();	
	}

	private void generateImports() {
        if (options.isMarkGeneratedAnything()) {
			addImport(Generated.class);
		}
		if (options.isMarkPatternAnything()) {
			addImport(Pattern.class);
			addImport(PatternType.class);
		}
    }

	private void generateClassClose() {
        println("}");
    }

	private void generateClassOpen() {
        if (options.isMarkPatternOnClass()) {
			annotate(Generated.class);
		}
		if (options.isMarkPatternOnClass()) {
			annotate(Pattern.class, emptyMap().put("type", PatternType.Builder));
		}
		println("public class ${this.shortClassName}{");
    }

	private void generateBeanCreateMethod(BeanDefinition def) {
        //bean create method
		if( options.isMarkeGeneratedMethods() ){
			annotate(Generated.class);
		}
		if( options.isMarkPatternOnMethod() ){
			annotate(Pattern.class, emptyMap().put("type", PatternType.BuilderCreate));
		}
		println("public ${this.beanType} create(){ " );
		println("${indent}${this.beanType} bean = new ${this.beanType}();");
		
		for (PropertyDefinition p : def.getProperties()) {
			if( !p.isIgnore()){
				println( "${indent}bean.${property.setter}(this.${property.name});", p);
			}
		}
		println("${indent}return bean;");
		generateClassClose();
    }

	private void generateSetters(BeanDefinition def) {
        // methods
		for (PropertyDefinition p : def.getProperties()) {
			if (!p.isIgnore()) {
				if (options.isMarkeGeneratedMethods()) {
					annotate(Generated.class);
				}
				println("public ${this.shortClassName} ${property.name}(${property.type} ${property.name}){ this.${property.name} = ${property.name}; return this;}",
				        p);
			}
		}
    }

	private void generateFields(BeanDefinition def) {
        //fields
		for (PropertyDefinition p : def.getProperties()) {
			if (!p.isIgnore()) {
				if (options.isMarkeGeneratedFields()) {
					annotate(Generated.class);
				}
				println("private ${property.type} ${property.name};", p);
			}
		}
    }
	
	public <T extends Annotation> void annotate(Class<T> annon) {
		annotate(annon, null);
	}
	
	public <T extends Annotation> void annotate(Class<T> annon, MapBuilder<String, Object> map){
		String line = "@" + annon.getSimpleName();
		if (map != null && map.size() > 0) {
			line = line + "(" + mapToString(map) + ")";
		}
		println(line);
	}
	
	private String mapToString(MapBuilder<String, Object> map){
		if( map == null ){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Map<String, Object> m = map.create();
		boolean comma = false;
		for( String key:m.keySet()){
			if( comma ){
				sb.append(',');
			}
			comma = true;
			sb.append(key);
			sb.append("=");
			Object val = m.get(key);
			if( val instanceof Enum<?>){
				Enum<?> e = (Enum<?>)val;
				sb.append(val.getClass().getSimpleName() + "." + e.name() );
			} else if( val instanceof Class ){
				sb.append( ((Class)val).getSimpleName());
			} else {
				sb.append(val);
			}
		}
		
		return sb.toString();		
	}
	
	private String mapName(String name) {
		String mappedName = primitiveToName.get(name);
		return mappedName == null ? name : mappedName;
	}
	
	public void println(String s, PropertyDefinition def){
		MapBuilder<String, Object> map = map();
		if( def != null ){
			String upperName = ClassUtils.upperFirstChar(def.getName());
			map		
			.put("property.name", mapName(def.getName()))
			.put("property.type", safeToClassName(def.getType()))
			.put("property.genericType", safeToClassName(def.getGenericType()))
			.put("property.upperName",upperName )
			.put("property.setter","set" + upperName)
			.put("property.getter","get" + upperName)
			;
		
		}
		println(s, map);
	}
	
	public  MapBuilder<String, Object> map(){
		MapBuilder<String, Object> map = emptyMap();
		map.put("this.type", fullClassName);
		map.put("this.beanType", beanTypeName);
		map.put("this.shortClassName", shortClassName);
		map.put("this.pkgName", pkgName);

		map.put("indent", indent);
		map.put("methodIndent", "${indent}");

		return map;
	}

	
}