package org.codemucker.jmutate.generate.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.google.common.base.Joiner;

public class ModelObject {

	private Map<Object, Object> properties;

	
	public void set(Object key,Object val){
		if(properties == null){
			properties = new HashMap<Object, Object>();
		}
		properties.put(key,val);
	}
	
	public boolean has(Object key){
		return properties != null && properties.containsKey(key);
	}
	
	/**
	 * @See {@link #getOrFail(Object)}
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getOrFail(Class<T> key){
		return (T)getOrFail((Object)key);
	}
	
	/**
	 * Get the object associated with the given key or fail if no such value with the given key or the value is null
	 * @param key
	 * @return
	 * @throws NoSuchElementException if no element with the given key exists
	 */
	public Object getOrFail(Object key) throws NoSuchElementException {
		Object val = getOrNull(key);
		if(val == null){
			String have = properties==null?"":Joiner.on(',').join(properties.keySet());
			throw new NoSuchElementException("Couldn't find object with key '" + key + "'. Instead have keys:[" + have + "]");
		}
		return val;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getOrNull(Class<T> key){
		return (T)getOrNull((Object)key);
	}
	
	public Object getOrNull(Object key){
		return properties==null?null:properties.get(key);
	}
	
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		int len = sb.length();
		//Foo@13234[
		sb.append(getClass().getSimpleName()).append('@').append(hashCode()).append('[');
		try {
			toString(sb);
		} catch (Exception e) {
			sb.append("<Error calling '").append(getClass().getName()).append(".toString()', exception=");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			sb.append(sw.toString());
			sb.append(">");
		}
		//properties=[key=value,...]
		if (properties != null && properties.size() > 0) {
			if (len < sb.length()) {
				sb.append(',');
			}
			sb.append("properties=[");
			boolean sep = false;
			for (Entry<Object, Object> entry : properties.entrySet()) {
				if (sep) {
					sb.append(',');
				}
				sb.append("\n\t");
				sb.append(entry.getKey());
				sb.append('=');
				sb.append(entry.getValue());

				sep = true;
			}
			sb.append(']');
		}
		sb.append(']');

		return sb.toString();
	}

	/**
	 * Override to add additional body details
	 * 
	 * @param sb
	 */
	protected void toString(StringBuilder sb) throws IOException {
	}
}
