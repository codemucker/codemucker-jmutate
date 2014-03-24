package org.codemucker.jmutate.bean;

import org.codemucker.jpattern.IsGenerated;
import org.codemucker.jpattern.Pattern;
import org.codemucker.jpattern.PatternType;
import org.codemucker.jtest.bean.BeanDefinition;
import org.codemucker.jtest.bean.PropertyDefinition;
import org.codemucker.lang.MapBuilder;
import org.codemucker.lang.annotation.NotThreadSafe;


@NotThreadSafe
@Deprecated
public class BeanBuilderWriter extends AbstractBeanWriter {

	private final String beanTypeName;
	
	public BeanBuilderWriter(String beanClassName) {
		this(new BeanBuilderOptions(), beanClassName);
	}
	
	public BeanBuilderWriter(BeanBuilderOptions options, String beanClassName){
		super(options, beanClassName + "Builder");
		this.beanTypeName = beanClassName;
	}

	@Override
	public void generate(BeanDefinition def){
		generatePkgDecl();
		generateImports();
		generateClassOpen();
		
		if( options.isCacheProperties() ){
			generateBuilderPropertyFields(def);
			generateBuilderSetters(def);
			generateBuilderCreateMethod(def);
			generateFieldEquals(def);
		} else {
			generateBeanFieldDeclaration(def);
			generateBuilderDirectSetters(def);
			generateBuilderDirectCreateMethod(def);			
		}
		
		generateFieldClone(def);
		
		if( options.isGenerateBeanCopy() ){
			generateAccessorCopy(def);
		}
		if (options.isGenerateBeanEquals()) {
			generateAccessorEquals(def);
		}
		generateClassClose();	
	}

	private void generateBeanFieldDeclaration(BeanDefinition def){
		if (options.isMarkeGeneratedFields()) {
			annotate(IsGenerated.class);
		}
		println("private ${this.beanType} bean = new ${this.beanType}();");
	}

	private void generateBuilderPropertyFields(BeanDefinition def) {
        //fields
		for (PropertyDefinition p : def.getProperties()) {
			if (!p.isIgnore()) {
				if (options.isMarkeGeneratedFields()) {
					annotate(IsGenerated.class);
				}
				println("private ${property.type} ${property.field};", p);
			}
		}
    }

	private void generateBuilderDirectSetters(BeanDefinition def) {
        // methods
		for (PropertyDefinition p : def.getProperties()) {
			if (!p.isIgnore()) {
				if (options.isMarkeGeneratedMethods()) {
					annotate(IsGenerated.class);
				}
				println("public ${this.shortClassName} ${property.safeName}(${property.type} ${property.field}){ this.bean.${property.setter}(${property.field}); return this;}",
				        p);
			}
		}
    }
	
	private void generateBuilderSetters(BeanDefinition def) {
        // methods
		for (PropertyDefinition p : def.getProperties()) {
			if (!p.isIgnore()) {
				if (options.isMarkeGeneratedMethods()) {
					annotate(IsGenerated.class);
				}
				println("public ${this.shortClassName} ${property.safeName}(${property.type} ${property.field}){ this.${property.field} = ${property.field}; return this;}",
				        p);
			}
		}
    }

	private void generateBuilderCreateMethod(BeanDefinition def) {
        //bean create method
		if( options.isMarkeGeneratedMethods() ){
			annotate(IsGenerated.class);
		}
		if( options.isMarkPatternOnMethod() ){
			annotate(Pattern.class, emptyMap().put("type", PatternType.BuilderCreate));
		}
		println("public ${this.beanType} create(){ " );
		println("${indent}${this.beanType} bean = new ${this.beanType}();");
		
		for (PropertyDefinition p : def.getProperties()) {
			if( !p.isIgnore()){
				println( "${indent}bean.${property.setter}(this.${property.field});", p);
			}
		}
		println("${indent}return bean;");
		println( "}");
    }

	private void generateBuilderDirectCreateMethod(BeanDefinition def) {
        //bean create method
		if( options.isMarkeGeneratedMethods() ){
			annotate(IsGenerated.class);
		}
		if( options.isMarkPatternOnMethod() ){
			annotate(Pattern.class, emptyMap().put("type", PatternType.BuilderCreate));
		}
		println("public ${this.beanType} create(){ " );
		println("${indent}return bean;");
		println( "}");
    }

	private void generateAccessorEquals(BeanDefinition def) {
		println("/** Test if two beans are equal */");		
		if (options.isMarkeGeneratedMethods()) {
			annotate(IsGenerated.class);
		}
        println("public boolean equals(${this.beanType} left, ${this.beanType} right){");
		println("if( left == right ) return true;");
		println("if( left == null && right == null ) return true;");
		println("if( left == null || right == null ) return false;");
		
		for (PropertyDefinition p : def.getProperties()) {
			if (!p.isIgnore()) {
				println("${indent}if(left.${property.getter}() == null ){", p);
				println("${indent}${indent}if( right.${property.getter}() != null ){ return false; }", p);
				println("${indent}} else if ( !left.${property.getter}().equals( right.${property.getter}() ){ return false; }", p);
			} 
		}
		println("${indent}return true;");
		println("}");
    }
	
	public  MapBuilder<String, Object> map(){
		MapBuilder<String, Object> map = super.map();		
		map.put("this.beanType", beanTypeName);
		return map;
	}
}