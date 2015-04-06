package org.codemucker.jmutate.generate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;

public class MapConfiguration extends AbstractConfiguration {
	private final Map<String, Object> map;//

	public MapConfiguration(){
		this.map = new HashMap<>();
	}
	
	public MapConfiguration(Map<String,Object> map){
		this.map = new HashMap<>(map);
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}

	@Override
	public Object getProperty(String key) {
		return map.get(key);
	}

	@Override
	public Iterator getKeys() {
		return map.keySet().iterator();
	}

	@Override
	protected void addPropertyDirect(String key, Object value) {
		map.put(key, value);
	}
	
}