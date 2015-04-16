package org.codemucker.jmutate.generate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.AbstractConfiguration;

public class MapConfiguration extends AbstractConfiguration {
	
	/**
	 * Debug/logging name to use for this configuration
	 */
	private String name = "default";
	
	private final Map<String, Object> map;

	public MapConfiguration(){
		this((String)null);
	}
	
	public MapConfiguration(Map<String,Object> map){
		this(map,null);
	}
	
	public MapConfiguration(String name){
		this.map = new HashMap<>();
		this.name = name;
	}
	
	public MapConfiguration(Map<String,Object> map, String name){
		this.map = new HashMap<>(map);
		this.name = name;
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
	
	public void addAll(Map<String,Object> map){
		for(Entry<String, Object> entry:map.entrySet()){
			addProperty(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void addPropertyDirect(String key, Object value) {
		map.put(key, value);
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append('@').append(hashCode()).append('[');
		sb.append("name=").append(name).append(",values=");
		for(Entry<String, Object> entry:map.entrySet()){
			sb.append("\n\t").append(entry.getKey()).append("=").append(entry.getValue());
		}
		sb.append(']');
		return sb.toString();
	}

	public String getName() {
		return name;
	}

}