package org.codemucker.jmutate.generate.model.pojo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codemucker.jmutate.generate.model.ModelObject;

import com.google.common.collect.Lists;

public class PojoModel extends ModelObject {

	private static final Comparator<String> PROP_NAME_COMPARATOR = new Comparator<String>() {

		@Override
		public int compare(String left, String right) {
			return left.compareTo(right);
		}
	};

	private static final Comparator<PropertyModel> PROP_COMPARATOR = new Comparator<PropertyModel>() {

		@Override
		public int compare(PropertyModel left, PropertyModel right) {
			return left.getName().compareTo(right.getName());
		}
	};

	private final PojoModel parent;
	private final Map<String, PropertyModel> properties = new TreeMap<>(
			PROP_NAME_COMPARATOR);

	/**
	 * How far up the ancestor chain this model is from the type we are
	 * processing
	 */
	private final int level;

	public PojoModel(int level) {
		this(level, null);
	}

	public PojoModel(int level, PojoModel parent) {
		super();
		this.level = level;
		this.parent = parent;
	}

	public boolean hasAnyProperties() {
		return hasDeclaredProperties()
				|| (parent != null && parent.hasAnyProperties());
	}

	public boolean hasDeclaredProperties() {
		return properties.size() > 0;
	}

	public int getLevel() {
		return level;
	}

	public void addProperty(PropertyModel prop) {
		properties.put(prop.getName().toLowerCase(), prop);
	}

	public PropertyModel getProperty(String name) {
		PropertyModel p = getDeclaredProperty(name);
		if (p == null && parent != null) {
			p = parent.getProperty(name);
		}
		return p;
	}

	public PropertyModel getDeclaredProperty(String name) {
		name = name.toLowerCase();
		return properties.get(name);
	}

	public List<PropertyModel> getAllProperties() {
		List<PropertyModel> list = getDeclaredProperties();
		if (parent != null) {
			list.addAll(parent.getAllProperties());
		}
		Collections.sort(list, PROP_COMPARATOR);
		return list;
	}

	public List<PropertyModel> getDeclaredProperties() {
		return Lists.newArrayList(properties.values());
	}

	public PojoModel getParent() {
		return parent;
	}

	public boolean hasDirectFinalProperties() {
		for (PropertyModel p : properties.values()) {
			if (p.hasField() && p.isFinalField()) {
				return true;
			}
		}
		return false;
	}

}
