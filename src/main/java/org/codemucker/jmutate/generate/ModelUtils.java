package org.codemucker.jmutate.generate;

import java.util.List;

import javax.management.RuntimeErrorException;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.ast.JAccess;
import org.codemucker.jpattern.generate.Access;

import com.google.common.collect.Lists;

public class ModelUtils {

	public static Configuration getEmptyCfg(){
    	return new BaseConfiguration();
    }

	@SuppressWarnings("unchecked")
	public static List<String> getList(Configuration cfg,String key, String[] def){
    	List<String> defList = Lists.newArrayList(def);
    	return cfg.getList(key, defList);
    }
	
	public static JAccess getJAccess(Configuration cfg, String key, Access def) {
		Access access = getEnum(cfg, key, def);
		switch (access) {
		case PACKAGE:
			return JAccess.PACKAGE;
		case PRIVATE:
			return JAccess.PRIVATE;
		case PROTECTED:
			return JAccess.PROTECTED;
		case PUBLIC:
			return JAccess.PUBLIC;
		default:
			throw new IllegalArgumentException("unknown value:" + access.name());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<?>> T getEnum(Configuration cfg,String key, T def){
    	Object val = cfg.getProperty(key);
    	
    	if(val != null){
    		if(def.getClass().isAssignableFrom(val.getClass())){
    			return (T)val;
    		}
    		if(!(val instanceof String)){
        		throw new RuntimeException("expected type of String or " + def.getClass().getName() + ", but got " + val.getClass().getName());
        	}
    		String s = (String)val;
    		int lastDot = s.lastIndexOf('.');
    		if(lastDot != -1 && s.startsWith(def.getClass().getName().replace('$', '.'))){
    			s = s.substring(lastDot + 1);
    		} 
    		return (T) Enum.valueOf(def.getClass(),s);
    	}
		return def;
    }
	
}
