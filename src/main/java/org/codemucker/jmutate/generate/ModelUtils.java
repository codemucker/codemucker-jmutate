package org.codemucker.jmutate.generate;

import java.util.List;

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
    	String val = cfg.getString(key,def.name());
    	if(val != null){
    		int lastDot = val.lastIndexOf('.');
    		if(lastDot != -1 && val.startsWith(def.getClass().getName().replace('$', '.'))){
    			val = val.substring(lastDot);
    		} 
    	}
		return (T) Enum.valueOf(def.getClass(), val);
    }
	
}
