package org.codemucker.jmutate.generate.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexedTypeRegistry {
	
	private static final List<String> collectionTypes = new ArrayList<>();
	private static final List<String> mapTypes = new ArrayList<>();

	public static final IndexedTypeRegistry INSTANCE = new IndexedTypeRegistry();
	
	private Map<String, String> defaultTypes = new HashMap<>();

	private IndexedTypeRegistry() {
		addCollection("java.util.List", "java.util.ArrayList");
		addCollection("java.util.ArrayList");
		addCollection("java.util.Collection", "java.util.ArrayList");
		addCollection("java.util.LinkedList");
		addCollection("java.util.Vector");

		addMap("java.util.TreeSet");
		addMap("java.util.Set", "java.util.HashSet");
		addMap("java.util.HashSet");
		addMap("java.util.Map", "java.util.HashMap");
		addMap("java.util.HashMap");
		addMap("java.util.Hashtable");

	}

	private void addCollection(String fullName) {
		addCollection(fullName, fullName);
	}

	private void addCollection(String fullName, String defaultType) {
		collectionTypes.add(fullName);
		defaultTypes.put(fullName, defaultType);
	}

	private void addMap(String fullName) {
		addMap(fullName, fullName);
	}

	private void addMap(String fullName, String defaultType) {
		mapTypes.add(fullName);
		defaultTypes.put(fullName, defaultType);
	}

	public boolean isCollection(String fullName) {
		return collectionTypes.contains(fullName);
	}

	public boolean isMap(String fullName) {
		return mapTypes.contains(fullName);
	}

	public String getConcreteTypeFor(String fullName) {
		return defaultTypes.get(fullName);
	}
}