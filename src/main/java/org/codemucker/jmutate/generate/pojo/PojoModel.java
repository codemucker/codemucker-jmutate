package org.codemucker.jmutate.generate.pojo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;

public class PojoModel {

	private final PojoModel parent;
	private final Map<String, PojoProperty> properties = new HashMap<>();

	public PojoModel() {
		this(null);
	}

	public PojoModel(PojoModel parent) {
		super();
		this.parent = parent;
	}

	public void addProperty(PojoProperty prop) {
		properties.put(prop.getPropertyName().toLowerCase(), prop);
	}

	public PojoProperty getProperty(String name) {
		PojoProperty p = getDeclaredProperty(name);
		if (p == null && parent != null) {
			p = parent.getProperty(name);
		}
		return p;
	}

	public PojoProperty getDeclaredProperty(String name) {
		name = name.toLowerCase();
		return properties.get(name);
	}

	public Collection<PojoProperty> getAllProperties() {
		Collection<PojoProperty> l = getDeclaredProperties();
		if (parent != null) {
			l.addAll(parent.getAllProperties());
		}
		return l;
	}

	public Collection<PojoProperty> getDeclaredProperties() {
		return Lists.newArrayList(properties.values());
	}
	
	public PojoModel getParent(){
		return parent;
	}
	

}
