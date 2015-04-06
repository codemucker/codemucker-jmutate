package org.codemucker.jmutate.generate.bean;

public class ConfigValue<T> {

	private final String name;
	private final String desc;
	
	private final T defaultValue;
	
	public ConfigValue(String name,String desc) {
		this(name,desc,null);
	}
	
	public ConfigValue(String name, String desc, T defaultValue) {
		super();
		this.name = name;
		this.desc = desc;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public String getDesc() {
		return desc;
	}
	
}
