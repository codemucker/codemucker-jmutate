package com.bertvanbrakel.test.bean;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

public class CtorDefinition {

	private final Constructor<?> ctor;
	private final Collection<CtorArgDefinition> args;
	private boolean makeAccessible = false;

	public CtorDefinition(Constructor<?> ctor) {
		this.ctor = ctor;
		args = new ArrayList<CtorArgDefinition>(ctor.getParameterTypes().length);
	}

	public boolean isMakeAccessible() {
		return makeAccessible;
	}

	public void setMakeAccessible(boolean makeAccessible) {
		this.makeAccessible = makeAccessible;
	}

	public Constructor<?> getCtor() {
		return ctor;
	}

	public void addArg(CtorArgDefinition arg) {
		args.add(arg);
	}

	public Collection<CtorArgDefinition> getArgs() {
		return args;
	}

	public CtorArgDefinition getArgNamed(String name) {
		for (CtorArgDefinition arg : args) {
			if (name.equals(arg.getName())) {
				return arg;
			}
		}
		return null;
	}

}
