package org.codemucker.jmutate.generate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.ast.JAnnotation;
import org.eclipse.jdt.core.dom.Annotation;

class InternalGeneratorConfig implements GeneratorConfig {
	private static final Object[] NO_ARGS = new Object[]{};
	
	private Configuration config;
	private final String annotationType;
	
	public InternalGeneratorConfig(Annotation a){
		JAnnotation annotation = JAnnotation.from(a);
		this.annotationType = annotation.getQualifiedName();
		this.config = new MapConfiguration(annotation.getAttributeMap());
	}

	public InternalGeneratorConfig(java.lang.annotation.Annotation a){
		this.annotationType = a.getClass().getName();
		this.config = extractConfig(a);
	}

	private static Configuration extractConfig(java.lang.annotation.Annotation a) {
		Configuration cfg = new MapConfiguration();
		
		Method[] methods = a.getClass().getDeclaredMethods();
		for(Method m:methods){
			if(m.getReturnType() == Void.class || !Modifier.isPublic(m.getModifiers()) || m.isSynthetic()){
				continue;
			}
			String name = m.getName();
			try {
    			Object val = m.invoke(a, NO_ARGS);
    			if(val instanceof Enum<?>){ //from source code expands to full name 
    				Enum<?> en = (Enum<?>)val;
    				val = en.getClass().getName() + "." + en.name();
    			} else if(val instanceof Class<?>){//in source code expanded to fully qualified name
    				val = ((Class<?>)val).getName();
    			}
				cfg.addProperty(name, val);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				//should never be thrown
				throw new RuntimeException("Error extracting value from annotation method '" + name + "'",e);
			}
		}
		return cfg;
	}
	
	public void addParentConfig(Configuration parent){
		CompositeConfiguration combined = new CompositeConfiguration();
		//first one wins
		combined.addConfiguration(config);
		combined.addConfiguration(parent);
		
		this.config = combined;
	}
	
	@Override
	public Configuration getConfig() {
		return config;
	}

	public String getAnnotationType() {
		return annotationType;
	}
}