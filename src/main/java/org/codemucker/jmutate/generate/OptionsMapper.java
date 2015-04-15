package org.codemucker.jmutate.generate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.util.NameUtil;
import org.codemucker.jpattern.bean.Property;
import org.codemucker.lang.BeanNameUtil;

import com.google.common.base.Strings;

public class OptionsMapper {

	private final ConvertUtilsBean CONVERTER = new ConvertUtilsBean();
	
	private final Logger LOG = LogManager.getLogger(OptionsMapper.class);
	
	/**
	 * Cache the defaults per annotation type. Keyed by annotation type
	 */
	private Map<String, Map<String,Object>> defaultsByAnnotation = new HashMap<String, Map<String,Object>>();
	
	private boolean makeFieldsAccesible  = true;
	
	public static final OptionsMapper INSTANCE = new OptionsMapper();
	
	public <T> T mapFromTo(Annotation fromAnnotation,Class<T> options) {
		return mapFromTo(new AnnotationConfiguration(fromAnnotation),fromAnnotation.getClass(), options);
	}
	
	public <T> T mapFromTo(Annotation fromAnnotation,T options) {
		return mapFromTo(new AnnotationConfiguration(fromAnnotation),fromAnnotation.getClass(), options);
	}
	
	public <T> T mapFromTo(Configuration config, Class<? extends Annotation> fromAnnotation,Class<T> options) {
		Map<String, Object> defaults = getDefaultsFor(fromAnnotation);
		List<String> propsSet = new ArrayList<>();
		
		T obj = newInstanceUsingAllArgCtorOrDefault(options,config, defaults, propsSet);
		populateProperties(obj,config,fromAnnotation, propsSet);
		
		logPropsSet(options,config,propsSet);
		
		return obj;
	}

	public <T> T mapFromTo(Configuration config, Class<? extends Annotation> fromAnnotation,T options) {
		List<String> propsSet = new ArrayList<>();
		populateProperties(options,config,fromAnnotation,propsSet);
		
		logPropsSet(options,config,propsSet);
		return options;
	}

	private <T> T newInstanceUsingAllArgCtorOrDefault(Class<T> beanClass, Configuration map, Map<String,Object> defaults,List<String> propsSet){
		//could be string,enum, multiple.. Find the best?
		Constructor<T> allArgCtor = findAllArgCtor(beanClass);
		if(allArgCtor != null){
			return invokeAllArgCtor(beanClass, map, allArgCtor, defaults, propsSet);
		} else {
			try{
				return beanClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Error calling no arg bean constructor on " + beanClass.getName(), e);
			}
		}
	}

	private <T> Constructor<T> findAllArgCtor(Class<T> beanClass) {
		Constructor<?>[] ctors = beanClass.getConstructors();
		Constructor<T> allArgCtor = null;
		ctors:for(Constructor<?> c:ctors){
			if(c.getParameterTypes().length > 0){
				for(java.lang.annotation.Annotation[] as:c.getParameterAnnotations()){
					for(java.lang.annotation.Annotation a:as){
						if(a instanceof Property){
							//use this ctor;
							if(allArgCtor == null || allArgCtor.getParameterCount()<c.getParameterCount()){
								allArgCtor = (Constructor<T>) c;
							}
							continue ctors;
						}
					}
				}
				
			}
		}
		return allArgCtor;
	}
	
	private <T> T invokeAllArgCtor(Class<T> beanClass, Configuration map,Constructor<T> allArgCtor, Map<String,Object> defaults,List<String> propsSet) {
		List<String> ctorArgNamesInOrder = new ArrayList<>();
		int idx = 0;
		for(java.lang.annotation.Annotation[] paramAnotations:allArgCtor.getParameterAnnotations()){
			String propertyName = getPropertyName(paramAnotations, idx);
			ctorArgNamesInOrder.add(propertyName);
			idx++;
		}
		Object[] args = new Object[allArgCtor.getParameterCount()];
		for(int i = 0;i < args.length;i++){
			Class<?> propertyType = allArgCtor.getParameters()[i].getType();
			
			String propertyName = ctorArgNamesInOrder.get(i);
			Object val = map.getProperty(propertyName);
			if (val == null) {
				val = defaults.get(propertyName);
				if (val == null) {
					val = getHardCodedDefaultForType(propertyType);
				}
			}
			propsSet.add(propertyName);
			Object convertedValue = convertTo(propertyName,val, propertyType);
			args[i] = convertedValue;
		}
		try {
			return allArgCtor.newInstance(args);
		} catch (Exception e) {
			String argsString = toString(allArgCtor,args);
			throw new RuntimeException("Error calling all arg bean constructor on " + beanClass.getName() + ". Arguments:\n" + argsString + "\nConfig:\n" + this, e);
		}
	}
	
	private String toString(Constructor<?> ctor,Object[] args){
		StringBuilder sb = new StringBuilder();
		
		Class<?>[] params = ctor.getParameterTypes();
		Annotation[][] paramAnons = ctor.getParameterAnnotations();
		
		boolean sep = false;
		for(int i = 0; i < args.length;i++){
			if(sep){
				sb.append(',');
			}
			
			String name = getPropertyName(paramAnons[i], i);
			sb.append("\n\t ").append(" [").append(i).append("] '").append(name).append("' (").append(params[i].getName()).append(") = ").append(args[i]);
			sep = true;
		}
		return sb.toString();
	}
	
	private String getPropertyName(Annotation[] annotations, int idx){
		String name = null;
		
		for(Annotation a:annotations){
			if(a instanceof Property){
				Property p = (Property)a;
				if( p != null){
					name = p.name();
					if(Strings.isNullOrEmpty(name)){
						throw new RuntimeException("No property name provided for arg at position " + idx);
					}
					break;
				}		
			}
		}
		return name;
	}

	private <T> void populateProperties(T mapToBean,Configuration map,Class<? extends Annotation> fromAnnotation, List<String> propsSet) {
		Class<? extends Object> beanClass = mapToBean.getClass();
		
		Map<String, Object> defaults = getDefaultsFor(fromAnnotation);
		
		for(Method m : fromAnnotation.getDeclaredMethods()){
			if(!AnnotationConfiguration.includeAnnotationMethod(m)){
				continue;
			}
			//extract the named prop from map, apply to bean
			String propertyName = m.getName();
			if(propsSet.contains(propertyName)){
				continue;
			}
			//find suitable setter
			String setterName = BeanNameUtil.toSetterName(propertyName);
			
			//TODO:use PropertiesExtractor?
			Class<?> propertyType = m.getReturnType();
			Method setter = null;
			try {
				setter = beanClass.getMethod(setterName, new Class<?>[]{ propertyType });
			} catch (NoSuchMethodException | SecurityException e) {
				try {
					setter = beanClass.getMethod(propertyName, new Class<?>[]{ propertyType });
				} catch (NoSuchMethodException | SecurityException e2) {
					//ignore
				}
			}
			if(setter == null && LOG.isDebugEnabled()){
				LOG.debug("couldn't find either setter/builder method " + setterName + "/" + propertyName + "(" + propertyType.getName() + ") on " + beanClass.getName());
			}
			
			boolean set = false;
			if(setter !=null){
				Object value = getValueFrom(propertyName, propertyType, map, defaults);
				try {
					setter.invoke(mapToBean, new Object[]{value});
					if(LOG.isDebugEnabled()){
						LOG.debug("set property " + beanClass.getName()  + "." + setterName + "");
					}
					set = true;
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					throw new RuntimeException("Error invoking setter + " + mapToBean.getClass().getName() + "." + setterName + "(" + propertyType.getName() + ")",e);	
				}
			}
			if(!set){ //try direct field access
				
				try {
					Field f = beanClass.getField(propertyName);
					if (!f.isAccessible() && makeFieldsAccesible) {
						f.setAccessible(true);
					}
					if(f.isAccessible() && !Modifier.isFinal(f.getModifiers())){
						Object value = getValueFrom(propertyName, propertyType, map, defaults);
						
						try {
							f.set(mapToBean, value);
							if(LOG.isDebugEnabled()){
								LOG.debug("set field " + beanClass.getName()  + "." + propertyName + " direct");
							}
						} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
							throw new RuntimeException("Error performing direct field assignment on " + mapToBean.getClass().getName() + "." + propertyName,e);	
						}
					}
				} catch (NoSuchFieldException | SecurityException e) {
					//no matter, ignore. Either can't make accessible or field doesn't exist
				}
				
			}
		}
	}
	
	private Object getValueFrom(String propertyName, Class<?> propertyType,Configuration map, Map<String, Object> defaults) {
		Object val = map.getProperty(propertyName);
		if (val == null) {
			val = defaults.get(propertyName);
			if (val == null) {
				val = getHardCodedDefaultForType(propertyType);
			}
		}
		Object convertedVal = convertTo(propertyName, val, propertyType);
		return convertedVal;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object convertTo(String propertyName, Object val, Class<?> toType) {
		LOG.debug("converting property '" + propertyName + "' from " + val + " to " + toType.getName() );
		if(val == null){
			return null;
		}
		//same type
		if(toType.isAssignableFrom(val.getClass())){
			return val;
		}
		if(Enum.class.isAssignableFrom(toType)){
			Class<Enum> enumType = (Class<Enum>) toType; 
			return ModelUtils.toEnum(val, enumType);
		}
		Object convertedVal = CONVERTER.convert(val, toType);
		return convertedVal;
	}
	
	private Map<String,Object> getDefaultsFor(Class<? extends Annotation> anonType){
		String key = getKeyFor(anonType);
		
		Map<String, Object> defaults = defaultsByAnnotation.get(key);
		if(defaults == null){
			defaults = new HashMap<>();
			for(Method m : anonType.getDeclaredMethods()){
				if(!AnnotationConfiguration.includeAnnotationMethod(m)){
					continue;
				}	
				defaults.put(m.getName(), m.getDefaultValue());
			}
			//doesn't matter if we set this multiple times
			synchronized (defaultsByAnnotation) {
				defaultsByAnnotation.put(key, defaults);			
			}
		}
		return defaults;
	}
	
	private String getKeyFor(Class<?> klass){
		return NameUtil.compiledNameToSourceName(klass);
	}
	
	private Object getHardCodedDefaultForType(Class<?> type){
		if(boolean.class.isAssignableFrom(type)){
			return false;
		}
		
		if(Number.class.isAssignableFrom(type)){
			return 0;
		}
		return null;
	}

	private void logPropsSet(Object bean, Configuration map,List<String> propsSet){
		if(LOG.isDebugEnabled()){
			LOG.debug("set properties on class " + bean.getClass().getName());
			for(String name:propsSet){
				LOG.debug("set " + name + "=" + map.getProperty(name));
			}
		}
	}

	
}
